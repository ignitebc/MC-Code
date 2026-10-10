local M = {}

function M.shoot(api)
    api:shootOnce(api:isShootingNeedConsumeAmmo())
end

function M.start_bolt(api)
    -- 확인할 것이 없으므로 true를 돌려줘 tick을 시작한다
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
    local intro = param.intro * 1000
    local loop = param.loop * 1000
    local ending = param.ending * 1000
    local loop_feed = param.loop_feed * 1000
    local clip_load = param.clip_load * 1000
    local clip_load_feed = param.clip_load_feed * 1000
    local bullet_lost = param.bullet_lost * 1000
    -- 시간 값 중 nil이 있는지 확인한다
    if (intro_empty == nil or intro == nil or loop == nil or ending == nil or loop_feed == nil or clip_load == nil or clip_load_feed == nil or bullet_lost == nil) then
        return nil
    end
    return intro_empty, intro, loop, ending, loop_feed, clip_load, clip_load_feed, bullet_lost
end

function M.tick_reload(api)
    -- 총기 data의 스크립트 매개변수에서 모든 시간 값을 가져온다
    local param = api:getScriptParams();
    local intro_empty, intro, loop, ending, loop_feed, clip_load, clip_load_feed, bullet_lost = getReloadTimingFromParam(param)
    if (intro_empty == nil) then
        return NOT_RELOADING, -1
    end
    -- api에서 재장전 시간(재장전 시작부터 현재까지의 시간)을 가져온다
    local reload_time = api:getReloadTime()
    -- api에서 캐시를 가져온다. 장전한 탄약 수 세기, 재장전 끊김 표시 등에 쓴다.
    local cache = api:getCachedScriptData()
    local interrupted_time = cache.interrupted_time

    if (not cache.is_tactical and api:getAttachment("SCOPE") == "tacz:empty") then
        if (reload_time < clip_load_feed) then
            return EMPTY_RELOAD_FEEDING, clip_load_feed - reload_time
        elseif (reload_time >= clip_load_feed and reload_time < clip_load) then
            if (cache.reloaded_count == 0) then
                api:setAmmoInBarrel(api:consumeAmmoFromPlayer(1))
                api:putAmmoInMagazine(api:isReloadingNeedConsumeAmmo() and api:consumeAmmoFromPlayer(cache.needed_count) or cache.needed_count)
                cache.reloaded_count = 5
            end
            return EMPTY_RELOAD_FINISHING, clip_load - reload_time
        else
            return NOT_RELOADING, -1
        end
    end

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


    local reloaded_count = cache.reloaded_count;

    if (reloaded_count >= 0) then
        local base_time = reloaded_count * loop + loop_feed
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
            if (not api:hasAmmoInBarrel()) then
                if (api:removeAmmoFromMagazine(1) ~= 0) then
                    api:setAmmoInBarrel(true);
                end
                cache.needed_count = cache.needed_count + 1
            end
        end
    end

    -- 캐시 다시 쓰기
    if (reloaded_count >= cache.needed_count) then
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