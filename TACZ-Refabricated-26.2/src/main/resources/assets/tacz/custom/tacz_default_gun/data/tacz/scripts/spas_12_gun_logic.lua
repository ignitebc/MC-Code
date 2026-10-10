local M = {}

function M.shoot(api)
    if (api:getFireMode() == BURST) then
        api:shootOnce(api:isShootingNeedConsumeAmmo())
        if (api:removeAmmoFromMagazine(1) == 1) then
            api:setAmmoInBarrel(true)
        end
    else
        api:shootOnce(api:isShootingNeedConsumeAmmo())
    end

end

function M.start_bolt(api)
    return true
end

function M.tick_bolt(api)
    -- 총기 data의 스크립트 매개변수에서 노리쇠 총시간을 가져온다
    local params = api:getScriptParams()
    local total_bolt_time = params.bolt_time * 1000
    local bolt_feed_time = params.bolt_feed_time * 1000
    if (total_bolt_time == nil or bolt_feed_time == nil) then
        return false
    end
    local bolt_time = api:getBoltTime()
    if (bolt_time < bolt_feed_time) then
        -- 노리쇠 시간이 총시간보다 짧으면 tick을 계속해야 하므로 true를 돌려준다
        return true
    else
        -- 노리쇠 시간이 총시간보다 길면 탄환을
        -- 탄창에서 총열로 넣은 뒤 false를 돌려줘 tick을 끝내야 한다.
        if (not api:hasAmmoInBarrel()) then
            if (api:removeAmmoFromMagazine(1) ~= 0) then
                api:setAmmoInBarrel(true);
            end
        end
        return bolt_time < total_bolt_time
    end
end

function M.start_reload(api)
    -- 재장전 tick에서 쓸 캐시를 초기화한다
    local cache = {
        reloaded_count = 0,
        needed_count = api:getNeededAmmoAmount(),
        is_tactical = api:getReloadStateType() == TACTICAL_RELOAD_FEEDING,
        interrupted_time = -1,
    }
    api:cacheScriptData(cache)
    -- true를 돌려줘 tick을 시작한다
    return true
end

local function getReloadTimingFromParam(param)
    -- 시간을 초에서 밀리초로 바꿔야 한다
    local intro_empty = param.intro_empty * 1000
    local intro_empty_semi = param.intro_empty_semi * 1000
    local intro = param.intro * 1000
    local loop = param.loop * 1000
    local ending = param.ending * 1000
    local intro_empty_feed = param.intro_empty_feed * 1000
    local intro_empty_feed_semi = param.intro_empty_feed_semi * 1000
    local loop_feed = param.loop_feed * 1000
    -- 시간 값 중 nil이 있는지 확인한다
    if (intro_empty == nil or intro_empty_semi == nil or intro == nil or loop == nil or ending == nil or intro_empty_feed == nil or intro_empty_feed_semi == nil or loop_feed == nil) then
        return nil
    end
    return intro_empty, intro_empty_semi, intro, loop, ending, intro_empty_feed, intro_empty_feed_semi, loop_feed
end

function M.tick_reload(api)
    -- 총기 data의 스크립트 매개변수에서 모든 시간 값을 가져온다
    local param = api:getScriptParams();
    local intro_empty, intro_empty_semi, intro, loop, ending, intro_empty_feed, intro_empty_feed_semi, loop_feed = getReloadTimingFromParam(param)
    if (intro_empty == nil or intro_empty_semi == nil) then
        return NOT_RELOADING, -1
    end
    -- api에서 재장전 시간(재장전 시작부터 현재까지의 시간)을 가져온다
    local reload_time = api:getReloadTime()
    -- api에서 캐시를 가져온다. 장전한 탄약 수 세기, 재장전 끊김 표시 등에 쓴다.
    local cache = api:getCachedScriptData()
    local interrupted_time = cache.interrupted_time
    -- 재장전 끊기 처리
    if (interrupted_time ~= -1) then
        local int_time = reload_time - interrupted_time
        if (int_time >= ending) then
            return NOT_RELOADING, -1
        else
            if (cache.is_tactical) then
                return TACTICAL_RELOAD_FINISHING, ending - int_time
            else
                return EMPTY_RELOAD_FINISHING, ending - int_time
            end
        end
    else
        -- 소모할 탄약이 없으면 재장전을 끊는다
        if (not api:hasAmmoToConsume()) then
            interrupted_time = api:getReloadTime()
        end
    end
    -- 먼저 탄약 한 발을 총열에 넣는다
    local reloaded_count = cache.reloaded_count;
    if (reloaded_count == 0) then
        if (not cache.is_tactical) then
            if (api:getFireMode() == BURST) then
                if (reload_time > intro_empty_feed_semi) then
                    api:consumeAmmoFromPlayer(1)
                    api:setAmmoInBarrel(true)
                    reloaded_count = reloaded_count + 1
                end
            else
                if (reload_time > intro_empty_feed) then
                    api:consumeAmmoFromPlayer(1)
                    api:setAmmoInBarrel(true)
                    reloaded_count = reloaded_count + 1
                end
            end
        else
            reloaded_count = reloaded_count + 1
        end
    end
    -- 탄약을 하나씩 탄창에 넣는다
    if (reloaded_count > 0) then
        local base_time = (reloaded_count -1) * loop + loop_feed
        if (not cache.is_tactical) then
            if (api:getFireMode() == BURST) then
                base_time = base_time + intro_empty_semi
            else
                base_time = base_time + intro_empty
            end
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
    -- 캐시 다시 쓰기
    if (reloaded_count > cache.needed_count) then
        interrupted_time = api:getReloadTime() - loop_feed + loop
    end
    cache.interrupted_time = interrupted_time
    cache.reloaded_count = reloaded_count
    api:cacheScriptData(cache)
    -- 재장전 상태 돌려주기
    local total_time = cache.needed_count * loop
    if (not cache.is_tactical) then
        total_time = total_time + intro_empty
        return EMPTY_RELOAD_FEEDING, total_time - reload_time
    else
        total_time = total_time + intro
        return TACTICAL_RELOAD_FEEDING, total_time - reload_time
    end
end

function M.interrupt_reload(api)
    local cache = api:getCachedScriptData()
    if (cache ~= nil and cache.interrupted_time == -1) then
        cache.interrupted_time = api:getReloadTime()
    end
end

return M