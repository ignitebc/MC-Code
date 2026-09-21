-- DBS: 총열이 두 개인 펌프 산탄총. 두 발을 연달아 쏜 뒤에 펌프질을 한다.
-- 장전과 펌프 동작은 m870_gun_logic 과 같고 발사만 다르다.
local M = {}

-- 펌프질 뒤로 쏜 발수(0 또는 1). 총기 아이템에 저장하므로 재장전이나 무기 교체 뒤에도 유지된다.
-- 잔탄 홀짝으로 추론하면 첫 발 뒤에 한 발을 채워 넣었을 때 세 발을 연달아 쏠 수 있게 된다.
-- 스크립트 캐시(cacheScriptData)는 재장전 때 덮어써지고 무기를 꺼낼 때 지워져서 쓸 수 없다.
local SHOTS_SINCE_PUMP = "dbs_shots_since_pump"

function M.shoot(api)
    api:shootOnce(api:isShootingNeedConsumeAmmo())
    -- 탄을 소모하지 않는 사격(크리에이티브 등)은 약실이 그대로라 주기를 따질 필요가 없다
    if (api:hasAmmoInBarrel()) then
        return
    end
    local is_first_of_pair = api:getScriptStateInt(SHOTS_SINCE_PUMP) == 0
    if (not is_first_of_pair) then
        -- 둘째 발까지 쐈다. 약실을 비워 둔 채 펌프 동작(tick_bolt)에 맡긴다
        api:setScriptStateInt(SHOTS_SINCE_PUMP, 0)
        return
    end
    -- 첫 발이다. 둘째 총열의 탄을 펌프 없이 바로 쏠 수 있게 약실로 올린다
    if (api:removeAmmoFromMagazine(1) ~= 0) then
        api:setAmmoInBarrel(true)
        api:setScriptStateInt(SHOTS_SINCE_PUMP, 1)
    end
end

function M.start_bolt(api)
    return true
end

function M.tick_bolt(api)
    local params = api:getScriptParams()
    local total_bolt_time = params.bolt_time * 1000
    local bolt_feed_time = params.bolt_feed_time * 1000
    if (total_bolt_time == nil or bolt_feed_time == nil) then
        return false
    end
    local bolt_time = api:getBoltTime()
    if (bolt_time < bolt_feed_time) then
        return true
    end
    if (not api:hasAmmoInBarrel()) then
        if (api:removeAmmoFromMagazine(1) ~= 0) then
            api:setAmmoInBarrel(true)
            api:setScriptStateInt(SHOTS_SINCE_PUMP, 0)
        end
    end
    return bolt_time < total_bolt_time
end

function M.start_reload(api)
    local cache = {
        reloaded_count = 0,
        needed_count = api:getNeededAmmoAmount(),
        is_tactical = api:getReloadStateType() == TACTICAL_RELOAD_FEEDING,
        interrupted_time = -1,
    }
    api:cacheScriptData(cache)
    return true
end

local function getReloadTimingFromParam(param)
    local intro_empty = param.intro_empty * 1000
    local intro = param.intro * 1000
    local loop = param.loop * 1000
    local ending = param.ending * 1000
    local intro_empty_feed = param.intro_empty_feed * 1000
    local loop_feed = param.loop_feed * 1000
    if (intro_empty == nil or intro == nil or loop == nil or ending == nil or intro_empty_feed == nil or loop_feed == nil) then
        return nil
    end
    return intro_empty, intro, loop, ending, intro_empty_feed, loop_feed
end

-- 한 발을 플레이어에게서 가져온다. 실제로 가져왔을 때만 true 를 돌려준다.
-- consumeAmmoFromPlayer 는 요청량이 아니라 실제로 가져온 수량을 돌려주므로, 반환값을 보지 않고 급탄하면
-- 예비탄이 없어도 총기 안의 탄이 늘어난다. 크리에이티브처럼 탄을 소모하지 않는 경우는 소모 없이 급탄한다.
local function take_one_ammo(api)
    if (not api:isReloadingNeedConsumeAmmo()) then
        return true
    end
    return api:consumeAmmoFromPlayer(1) == 1
end

function M.tick_reload(api)
    local param = api:getScriptParams();
    local intro_empty, intro, loop, ending, intro_empty_feed, loop_feed = getReloadTimingFromParam(param)
    if (intro_empty == nil) then
        return NOT_RELOADING, -1
    end
    local reload_time = api:getReloadTime()
    local cache = api:getCachedScriptData()
    local interrupted_time = cache.interrupted_time
    if (interrupted_time ~= -1) then
        local int_time = reload_time - interrupted_time
        if (int_time >= ending) then
            return NOT_RELOADING, -1
        end
        if (cache.is_tactical) then
            return TACTICAL_RELOAD_FINISHING, ending - int_time
        end
        return EMPTY_RELOAD_FINISHING, ending - int_time
    end
    -- 예비탄이 떨어지면 이번 호출에서는 급탄하지 않고 마무리 단계로 넘긴다
    local has_ammo = api:hasAmmoToConsume()
    if (not has_ammo) then
        interrupted_time = reload_time
    end
    -- 빈 총이면 첫 탄을 약실에 먼저 넣는다
    local reloaded_count = cache.reloaded_count;
    if (reloaded_count == 0 and has_ammo) then
        if (not cache.is_tactical) then
            if (reload_time > intro_empty_feed) then
                if (take_one_ammo(api)) then
                    api:setAmmoInBarrel(true)
                    api:setScriptStateInt(SHOTS_SINCE_PUMP, 0)
                    reloaded_count = reloaded_count + 1
                else
                    has_ammo = false
                end
            end
        else
            reloaded_count = reloaded_count + 1
        end
    end
    -- 나머지는 관형 탄창에 한 발씩 넣는다. 틱이 밀려 급탄 시점을 여러 개 지나쳤어도 매 발 실제로 가져온 뒤에만 넣는다
    if (reloaded_count > 0 and has_ammo) then
        local base_time = (reloaded_count - 1) * loop + loop_feed
        if (not cache.is_tactical) then
            base_time = base_time + intro_empty
        else
            base_time = base_time + intro
        end
        while (base_time < reload_time) do
            if (reloaded_count > cache.needed_count) then
                break
            end
            if (not take_one_ammo(api)) then
                has_ammo = false
                break
            end
            api:putAmmoInMagazine(1)
            reloaded_count = reloaded_count + 1
            base_time = base_time + loop
        end
    end
    if (not has_ammo and interrupted_time == -1) then
        interrupted_time = reload_time
    end
    if (reloaded_count > cache.needed_count) then
        interrupted_time = api:getReloadTime() - loop_feed + loop
    end
    cache.interrupted_time = interrupted_time
    cache.reloaded_count = reloaded_count
    api:cacheScriptData(cache)
    local total_time = cache.needed_count * loop
    if (not cache.is_tactical) then
        total_time = total_time + intro_empty
        return EMPTY_RELOAD_FEEDING, total_time - reload_time
    end
    total_time = total_time + intro
    return TACTICAL_RELOAD_FEEDING, total_time - reload_time
end

function M.interrupt_reload(api)
    local cache = api:getCachedScriptData()
    if (cache ~= nil and cache.interrupted_time == -1) then
        cache.interrupted_time = api:getReloadTime()
    end
end

return M
