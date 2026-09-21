-- DBS: 총열이 두 개인 펌프 산탄총. 두 발을 연달아 쏜 뒤에 펌프질을 한다.
-- 장전과 펌프 동작은 m870_gun_logic 과 같고 발사만 다르다.
local M = {}

function M.shoot(api)
    api:shootOnce(api:isShootingNeedConsumeAmmo())
    -- 관형 탄창에 남은 탄이 홀수면 한 쌍의 첫 발을 쏜 것이므로 펌프 없이 다음 탄을 약실로 올린다.
    -- 짝수면 둘째 발까지 쏜 것이므로 약실을 비워 둔 채 펌프 동작(tick_bolt)에 맡긴다.
    -- 스크립트 캐시는 재장전 때 덮어써지므로 상태를 따로 두지 않고 잔탄 홀짝으로 판단한다.
    local is_first_of_pair = api:getAmmoAmount() % 2 == 1
    if (is_first_of_pair and not api:hasAmmoInBarrel()) then
        if (api:removeAmmoFromMagazine(1) ~= 0) then
            api:setAmmoInBarrel(true)
        end
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
    elseif (not api:hasAmmoToConsume()) then
        interrupted_time = api:getReloadTime()
    end
    -- 빈 총이면 첫 탄을 약실에 먼저 넣는다
    local reloaded_count = cache.reloaded_count;
    if (reloaded_count == 0) then
        if (not cache.is_tactical) then
            if (reload_time > intro_empty_feed) then
                api:consumeAmmoFromPlayer(1)
                api:setAmmoInBarrel(true)
                reloaded_count = reloaded_count + 1
            end
        else
            reloaded_count = reloaded_count + 1
        end
    end
    -- 나머지는 관형 탄창에 한 발씩 넣는다
    if (reloaded_count > 0) then
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
            reloaded_count = reloaded_count + 1
            base_time = base_time + loop
            api:consumeAmmoFromPlayer(1)
            api:putAmmoInMagazine(1)
        end
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
