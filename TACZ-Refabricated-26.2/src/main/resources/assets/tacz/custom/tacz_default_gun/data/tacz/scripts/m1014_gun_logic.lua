local M = {}

function M.start_reload(api)
    -- 캐시에서 쓸 매개변수를 초기화한다
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
    -- 시간을 밀리초 단위로 바꾼다
    local intro_empty = param.intro_empty * 1000
    local intro = param.intro * 1000
    local loop = param.loop * 1000
    local loop_2 = param.loop_2 * 1000
    local ending = param.ending * 1000
    local intro_empty_feed = param.intro_empty_feed * 1000
    local loop_feed = param.loop_feed * 1000
    local loop_feed_2 = param.loop_feed_2 * 1000
    -- 시간 값 중 비어 있는 것이 있는지 확인한다
    if (intro_empty == nil or intro == nil or loop == nil or loop_2 == nil or ending == nil or intro_empty_feed == nil or loop_feed == nil or loop_feed_2 == nil) then
        return nil
    end
    -- 시간을 차례로 돌려준다
    return intro_empty, intro, loop, loop_2, ending, intro_empty_feed, loop_feed, loop_feed_2
end

function M.tick_reload(api)
    -- 총의 data 파일에서 스크립트에 필요한 매개변수 값을 모두 가져온다
    local param = api:getScriptParams();
    local intro_empty, intro, loop, loop_2, ending, intro_empty_feed, loop_feed, loop_feed_2 = getReloadTimingFromParam(param)
    -- 시간에 빈 값이 있는지 형식상 한 번 확인한다
    if (intro_empty == nil) then
        return NOT_RELOADING, -1
    end
    -- 재장전 시간(R을 누른 때부터 지금까지)을 가져온다
    local reload_time = api:getReloadTime()
    -- 미리 캐시한 매개변수를 가져온다
    local cache = api:getCachedScriptData()
    local interrupted_time = cache.interrupted_time
    -- 재장전 끊기
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
        -- 플레이어 인벤토리에 소모할 탄약이 더 없으면 재장전을 끊는다
        if (not api:hasAmmoToConsume()) then
            interrupted_time = api:getReloadTime()
        end
    end
    -- 빈 탄창 재장전이면 총열에 1발 넣는다
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
    -- 반복 재장전
    if (reloaded_count > 0) then
        local base_time = 0
        -- 필요한 탄약 수가 1이면 loop를 한 번만 부른다
        if (cache.needed_count == 1) then
            base_time = 0 + loop_feed
        -- 필요한 탄약 수가 1보다 크면 loop_2를 x번, loop를 0/1번 불러야 한다
        elseif (cache.needed_count > 1) then
            -- 장전 수에 따라 두 경우의 다음 feed까지 재장전 시작점부터의 시간을 따로 계산한다(짝수는 loop_2 x-1회, 홀수는 loop_2 y회와 loop 1회의 시간)
            if (reloaded_count % 2 == 0) then
                base_time = ((reloaded_count - 2) / 2) * loop_2 + loop_feed_2
            else
                base_time = ((reloaded_count - 1) / 2) * loop_2 + loop_feed
            end
        end
        -- 다음 feed까지의 시간에 시작 시간을 더한다
        if (not cache.is_tactical) then
            base_time = base_time + intro_empty
        else
            base_time = base_time + intro
        end
        -- 재장전 시간이 다음 feed 시점에 이르렀을 때
        while (base_time < reload_time) do
            -- 재장전 필요량을 채웠으면 반복을 끝낸다
            if (reloaded_count > cache.needed_count) then
                break
            end
            -- 필요량이 2 이상이면 두 발씩 장전한다
            if (cache.needed_count - reloaded_count >= 1) then
                reloaded_count = reloaded_count + 2
                base_time = base_time + loop_2
                -- 플레이어의 게임 모드를 판단한다
                if (api:isReloadingNeedConsumeAmmo()) then
                    api:putAmmoInMagazine(api:consumeAmmoFromPlayer(2))
                else
                    api:putAmmoInMagazine(2)
                end
            -- 필요량이 1이면 한 발 장전한다
            elseif (cache.needed_count - reloaded_count < 1) then
                reloaded_count = reloaded_count + 1
                base_time = base_time + loop
                -- 플레이어의 게임 모드를 판단한다
                if (api:isReloadingNeedConsumeAmmo()) then
                    api:putAmmoInMagazine(api:consumeAmmoFromPlayer(1))
                else
                    api:putAmmoInMagazine(1)
                end
            end
        end
    end

    -- 데이터를 캐시에 다시 쓴다
    if (reloaded_count > cache.needed_count) then
        interrupted_time = api:getReloadTime() - loop_feed + loop
    end
    cache.interrupted_time = interrupted_time
    cache.reloaded_count = reloaded_count
    api:cacheScriptData(cache)
    -- 재장전 상태를 돌려준다. 여기서 total_time은 어떤 상태에서든 재장전 총시간이다(ending 시간 제외)
    local total_time = ((cache.needed_count - (cache.needed_count % 2)) / 2) * loop_2 + (cache.needed_count % 2) * loop
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