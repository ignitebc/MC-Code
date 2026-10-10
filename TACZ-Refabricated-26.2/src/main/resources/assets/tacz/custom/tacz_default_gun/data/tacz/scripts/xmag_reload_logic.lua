-- 논리 기계 정의. 정해진 형식
local M = {}

-- 재장전을 시작할 때 한 번 호출된다
function M.start_reload(api)
    return true
end

-- 총 data 파일에서 장전 관련 애니메이션 시점을 가져오는 lua 함수다. lua 안의 시간은 밀리초라서 1000을 곱해야 한다
local function getReloadTimingFromParam(param)
    local reload_feed = {param.reload_feed, param.reload_xmag_1_feed, param.reload_xmag_2_feed, param.reload_xmag_3_feed}
    local reload_cooldown = {param.reload_cooldown, param.reload_xmag_1_cooldown, param.reload_xmag_2_cooldown, param.reload_xmag_3_cooldown}
    local empty_feed = {param.empty_feed, param.empty_xmag_1_feed, param.empty_xmag_2_feed, param.empty_xmag_3_feed}
    local empty_cooldown = {param.empty_cooldown, param.empty_xmag_1_cooldown, param.empty_xmag_2_cooldown, param.empty_xmag_3_cooldown}
    for i = 1, 4 do
        -- param의 시점을 밀리초로 바꾼다
        -- nil이 있으면 바로 nil을 돌려준다
        if (reload_feed[i] == nil or reload_cooldown[i] == nil or empty_feed[i] == nil or empty_cooldown[i] == nil) then
            return nil, nil, nil, nil
        end
        reload_feed[i] = reload_feed[i] * 1000
        reload_cooldown[i] = reload_cooldown[i] * 1000
        empty_feed[i] = empty_feed[i] * 1000
        empty_cooldown[i] = empty_cooldown[i] * 1000
    end

    -- 가져온 배열 4개를 차례로 돌려준다
    return reload_feed, reload_cooldown, empty_feed, empty_cooldown
end

-- 이 상태가 빈 탄창 재장전 과정의 한 단계인지 판단한다. 빈 탄창 재장전의 마무리 단계도 포함한다
local function isReloadingEmpty(stateType)
    return stateType == EMPTY_RELOAD_FEEDING or stateType == EMPTY_RELOAD_FINISHING
end

-- 이 상태가 전술 재장전 과정의 한 단계인지 판단한다. 전술 재장전의 마무리 단계도 포함한다
local function isReloadingTactical(stateType)
    return stateType == TACTICAL_RELOAD_FEEDING or stateType == TACTICAL_RELOAD_FINISHING
end

-- 이 상태가 어떤 재장전 과정의 한 단계인지 판단한다. 모든 재장전의 마무리 단계도 포함한다
local function isReloading(stateType)
    return isReloadingEmpty(stateType) or isReloadingTactical(stateType)
end

-- 이 상태가 어떤 재장전 과정의 마무리 단계인지 판단한다
local function isReloadFinishing(stateType)
    return stateType == EMPTY_RELOAD_FINISHING or stateType == TACTICAL_RELOAD_FINISHING
end

local function finishReload(api, is_tactical)
    local needAmmoCount = api:getNeededAmmoAmount();
    if (api:isReloadingNeedConsumeAmmo()) then
        -- 탄약을 소모해야 하면(서바이벌이나 모험) 재장전에 필요한 탄약을 소모하고 소모한 수만큼 탄창에 채운다
        api:putAmmoInMagazine(api:consumeAmmoFromPlayer(needAmmoCount))
    else
        -- 탄약을 소모하지 않아도 되면(크리에이티브) 탄창을 바로 가득 채운다
        api:putAmmoInMagazine(needAmmoCount)
    end
    if not is_tactical then
        local i = api:removeAmmoFromMagazine(1);
        if i ~= 0 then
            api:setAmmoInBarrel(true)
        end
    end
end

function M.tick_reload(api)
    -- 총 data 파일에서 논리 기계에 넘길 매개변수를 모두 가져온다. 이때 param은 목록이라 아직 바로 쓸 수 없다
    local param = api:getScriptParams();
    -- 방금 만든 lua 함수를 호출해 param에 든 매개변수 여덟 개를 새로 정의한 변수에 차례로 넣는다
    local reload_feed, reload_cooldown, empty_feed, empty_cooldown = getReloadTimingFromParam(param)
    -- 늘 하던 대로 빠진 매개변수가 있는지 확인한다
    if (reload_feed == nil or reload_cooldown == nil or empty_feed == nil or empty_cooldown == nil) then
        return NOT_RELOADING, -1
    end

    -- 현재 탄창 등급을 가져온다. 최대 3등급이라고 가정한다
    local mag_level = math.min(api:getMagExtentLevel(), 3) + 1

    local countDown = -1
    local stateType = NOT_RELOADING
    local oldStateType = api:getReloadStateType()

    -- 재장전 시간을 가져온다. 플레이어가 R을 누른 순간이 0이며 단위는 밀리초다. 플레이어가 1초 전에 R을 눌렀다면 지금 이 시간은 1000이다
    local progressTime = api:getReloadTime()

    if isReloadingEmpty(oldStateType) then
        local feed_time = empty_feed[mag_level]
        local finishing_time = empty_cooldown[mag_level]
        if progressTime < feed_time then
            stateType = EMPTY_RELOAD_FEEDING
            countDown = feed_time - progressTime
        elseif progressTime < finishing_time then
            stateType = EMPTY_RELOAD_FINISHING
            countDown = finishing_time - progressTime
        else
            stateType = NOT_RELOADING;
            countDown = -1
        end
    elseif isReloadingTactical(oldStateType) then
        local feed_time = reload_feed[mag_level]
        local finishing_time = reload_cooldown[mag_level]
        if progressTime < feed_time then
            stateType = TACTICAL_RELOAD_FEEDING
            countDown = feed_time - progressTime
        elseif progressTime < finishing_time then
            stateType = TACTICAL_RELOAD_FINISHING
            countDown = finishing_time - progressTime
        else
            stateType = NOT_RELOADING;
            countDown = -1
        end
    else
        stateType = NOT_RELOADING;
        countDown = -1
    end

    if oldStateType == EMPTY_RELOAD_FEEDING and oldStateType ~= stateType then
        finishReload(api,false);
    end

    if oldStateType == TACTICAL_RELOAD_FEEDING and oldStateType ~= stateType then
        finishReload(api, true);
    end

    return stateType, countDown
end

-- 모드에 논리 기계 전체를 돌려준다. 정해진 형식
return M