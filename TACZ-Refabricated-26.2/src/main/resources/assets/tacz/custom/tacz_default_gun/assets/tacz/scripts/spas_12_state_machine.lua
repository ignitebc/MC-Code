-- 스크립트 위치가 "{네임스페이스}:{경로}"이면 require 형식은 "{네임스페이스}_{경로}"다
-- 주의! require로 얻은 내용은 고치지 말고 호출만 해야 한다
local default = require("tacz_manual_action_state_machine")
local STATIC_TRACK_LINE = default.STATIC_TRACK_LINE
local MAIN_TRACK = default.MAIN_TRACK
local GUN_KICK_TRACK_LINE = default.GUN_KICK_TRACK_LINE
local main_track_states = default.main_track_states
local gun_kick_state = default.gun_kick_state
local ADS_states = default.ADS_states
local BASE_TRACK = default.BASE_TRACK
-- main_track_states.idle은 우리가 다시 쓸 상태다.
local shoot_state = setmetatable({},{__index = gun_kick_state})
local idle_state = setmetatable({}, {__index = main_track_states.idle})
local start_state = setmetatable({}, {__index = main_track_states.start})
local inspect_state = setmetatable({}, {__index = main_track_states.inspect})
-- reload_state, bolt_state는 한 발씩 장전하려고 정의한 새 상태다

local function runPutAwayAnimation(context)
    local put_away_time = context:getPutAwayTime()
    -- 여기서 얻는 트랙은 주 트랙 줄에 있는 주 트랙이다
    local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
    -- put_away 애니메이션을 재생하고, 전환 시간을 문맥에서 넘어온 put_away_time * 0.75로 둔다
    if(context:getFireMode() == BURST)then
        if ((not context:hasBulletInBarrel()) and context:getAmmoCount() == 0) then
            context:runAnimation("put_away_semi_caught", track, false, PLAY_ONCE_HOLD, put_away_time * 0.75)
        else
            context:runAnimation("put_away_semi", track, false, PLAY_ONCE_HOLD, put_away_time * 0.75)
        end
    else
        context:runAnimation("put_away", track, false, PLAY_ONCE_HOLD, put_away_time * 0.75)
    end
    -- 애니메이션 진행도를 마지막 프레임으로 둔다
    context:setAnimationProgress(track, 1, true)
    -- 애니메이션 진행도를 {put_away_time}만큼 앞으로 돌린다
    context:adjustAnimationProgress(track, -put_away_time, false)
end

function start_state.transition(this, context, input)
    if (input == INPUT_DRAW) then
        -- draw 신호를 받으면 주 트랙 줄의 주 트랙에서 총 꺼내기 애니메이션을 재생하고 대기 상태로 간다
        if(context:getFireMode() == BURST)then
            if ((not context:hasBulletInBarrel()) and context:getAmmoCount() == 0) then
                context:runAnimation("draw_semi_caught", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0)
            else
                context:runAnimation("draw_semi", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0)
            end
        else
            context:runAnimation("draw", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0)
        end
        return this.main_track_states.idle
    end
end

local crosshair_state = {
    ads_ing = 0
}

-- 조준 상태 진입
function crosshair_state.entry(this, context)
    -- 조준을 시작하면 조준 애니메이션을 재생하고 걸어 둔다
end

-- 조준 상태 갱신
function crosshair_state.update(this, context)
    if (context:getAimingProgress() >= 1) then
        crosshair_state.ads_ing = 1
        if (context:getAttachment("SCOPE") == "tacz:empty") then
            context:setShouldHideCrossHair(false)
        else
            context:setShouldHideCrossHair(true)
        end
    else
        if (crosshair_state.ads_ing == 1) then
            crosshair_state.ads_ing = 0
            context:setShouldHideCrossHair(false)
        end
        context:setShouldHideCrossHair(context:shouldHideCrossHair())
    end
    print(context:shouldHideCrossHair())
end

local base_state = {
    mode = 0
}
-- 기본 상태에 들어가면 바로 static_idle을 재생한다
function base_state.entry(this, context)
    if (context:getFireMode() == SEMI) then
        base_state.mode = 0
        context:runAnimation("static_idle", context:getTrack(STATIC_TRACK_LINE, BASE_TRACK), false, PLAY_ONCE_HOLD, 0)
    elseif (context:getFireMode() == BURST) then
        base_state.mode = 1
        if ((not context:hasBulletInBarrel()) and context:getAmmoCount() == 0) then
            context:runAnimation("static_idle_semi_caught", context:getTrack(STATIC_TRACK_LINE, BASE_TRACK), false, PLAY_ONCE_HOLD, 0)
        else
            context:runAnimation("static_idle_semi", context:getTrack(STATIC_TRACK_LINE, BASE_TRACK), false, PLAY_ONCE_HOLD, 0)
        end
    end
end

function base_state.update(this, context)
    local track = context:getTrack(STATIC_TRACK_LINE, BASE_TRACK)
    if (context:isHolding(track)) then
        if (context:getFireMode() == SEMI) then
            if (base_state.mode == 1) then
                if ((not context:hasBulletInBarrel()) and context:getAmmoCount() == 0) then
                    context:runAnimation("switch_pump_empty", context:getTrack(STATIC_TRACK_LINE, BASE_TRACK), false, PLAY_ONCE_HOLD, 0)
                else
                    context:runAnimation("switch_pump", context:getTrack(STATIC_TRACK_LINE, BASE_TRACK), false, PLAY_ONCE_HOLD, 0)
                end
                base_state.mode = 0
            else
                context:runAnimation("static_idle", context:getTrack(STATIC_TRACK_LINE, BASE_TRACK), false, PLAY_ONCE_HOLD, 0)
            end
        elseif (context:getFireMode() == BURST) then
            if (base_state.mode == 0) then
                if ((not context:hasBulletInBarrel()) and context:getAmmoCount() == 0) then
                    context:runAnimation("switch_semi_empty", context:getTrack(STATIC_TRACK_LINE, BASE_TRACK), false, PLAY_ONCE_HOLD, 0)
                else
                    context:runAnimation("switch_semi", context:getTrack(STATIC_TRACK_LINE, BASE_TRACK), false, PLAY_ONCE_HOLD, 0)
                end
                base_state.mode = 1
            else
                if ((not context:hasBulletInBarrel()) and context:getAmmoCount() == 0) then
                    context:runAnimation("static_idle_semi_caught", context:getTrack(STATIC_TRACK_LINE, BASE_TRACK), false, PLAY_ONCE_HOLD, 0)
                else
                    context:runAnimation("static_idle_semi", context:getTrack(STATIC_TRACK_LINE, BASE_TRACK), false, PLAY_ONCE_HOLD, 0)
                end
            end
        end
    end
end

function shoot_state.transition(this, context, input)
    if (input == INPUT_SHOOT) then
        local track = context:findIdleTrack(GUN_KICK_TRACK_LINE, false)
        if (context:getFireMode() == SEMI) then
            context:runAnimation("shoot", track, true, PLAY_ONCE_STOP, 0)
        elseif (context:getFireMode() == BURST) then
            if (context:getAmmoCount() == 0) then
                context:runAnimation("shoot_semi_last", track, true, PLAY_ONCE_STOP, 0)
            else
                context:runAnimation("shoot_semi", track, true, PLAY_ONCE_STOP, 0)
            end
            context:popShellFrom(0)
        end
    end
    return nil
end

local reload_state = {
    need_ammo = 0,
    loaded_ammo = 0
}
local function get_ejection_time(context)
    local ejection_time = context:getStateMachineParams().intro_shell_ejecting_time
    if (ejection_time) then
        ejection_time = ejection_time * 1000
    else
        ejection_time = 0
    end
    return ejection_time
end

local function runInspectAnimation(context)
    local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
    if (not context:hasBulletInBarrel() and context:getAmmoCount() <= 0 and context:getFireMode() == SEMI) then
        context:runAnimation("inspect_empty", track, false, PLAY_ONCE_STOP, 0.2)
    elseif (not context:hasBulletInBarrel() and context:getAmmoCount() <= 0 and context:getFireMode() == BURST) then
        context:runAnimation("inspect_empty_semi", track, false, PLAY_ONCE_STOP, 0.2)
    elseif (context:getAmmoCount() <= 0 and context:getFireMode() == SEMI) then
        context:runAnimation("inspect_1", track, false, PLAY_ONCE_STOP, 0.2)
    elseif (context:getAmmoCount() <= 0 and context:getFireMode() == BURST) then
        context:runAnimation("inspect_1_semi", track, false, PLAY_ONCE_STOP, 0.2)
    elseif (context:getFireMode() == BURST) then
        context:runAnimation("inspect_semi", track, false, PLAY_ONCE_STOP, 0.2)
    else
        context:runAnimation("inspect", track, false, PLAY_ONCE_STOP, 0.2)
    end
end

-- idle 상태의 transition 함수를 다시 써서 INPUT_RELOAD 입력을 새로 정의한 reload_state 상태로 돌린다
function idle_state.transition(this, context, input)
    if (input == INPUT_PUT_AWAY) then
        runPutAwayAnimation(context)
        -- 총을 버린 뒤 최종 상태로 간다
        return this.main_track_states.final
    end
    if (input == INPUT_RELOAD) then
        return this.main_track_states.reload
    end
    if (input == INPUT_INSPECT) then
        runInspectAnimation(context)
        return this.main_track_states.inspect
    end
    return main_track_states.idle.transition(this, context, input)
end
-- entry 함수에서는 상황에 따라 'reload_intro_empty'나 'reload_intro' 애니메이션을 골라 재생하고,
-- 필요한 탄약 수와 장전한 탄약 수를 초기화한다. 이것이 이후 'loop' 애니메이션의 반복 횟수를 정한다.
function reload_state.entry(this, context)
    local state = this.main_track_states.reload
    local isNoAmmo = not context:hasBulletInBarrel()
    if (isNoAmmo) then
        -- reload_intro_empty에서 탄피를 내보내려고 재장전을 시작한 타임스탬프를 기록한다
        state.timestamp = context:getCurrentTimestamp()
        state.ejection_time = get_ejection_time(context)
        if (context:getFireMode() == BURST) then
            context:runAnimation("reload_empty_intro_semi", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_HOLD, 0.2)
        else
            context:runAnimation("reload_empty_intro", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_HOLD, 0.2)
        end
    else
        state.timestamp = -1
        state.ejection_time = 0
        if (context:getFireMode() == BURST) then
            context:runAnimation("reload_intro_semi", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_HOLD, 0.2)
        else
            context:runAnimation("reload_intro", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_HOLD, 0.2)
        end
    end
    state.need_ammo = context:getMaxAmmoCount() - context:getAmmoCount()
    state.loaded_ammo = 0
end
-- update 함수에서는 loop를 반복 재생하며 loaded_ammo 변수를 1씩 늘린다.
function reload_state.update(this, context)
    local state = this.main_track_states.reload
    -- reload_intro_empty의 탄피 배출 처리
    if (state.timestamp ~= -1 and context:getCurrentTimestamp() - state.timestamp > state.ejection_time) then
        if (context:getFireMode() == SEMI) then
            context:popShellFrom(0)
        end
        state.timestamp = -1
    end
    if (state.loaded_ammo > state.need_ammo or not context:hasAmmoToConsume()) then
        context:trigger(this.INPUT_RELOAD_RETREAT)
    else
        local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
        if (context:isHolding(track)) then
            if (context:getFireMode() == BURST) then
                context:runAnimation("reload_loop_semi", track, false, PLAY_ONCE_HOLD, 0)
            else
                context:runAnimation("reload_loop", track, false, PLAY_ONCE_HOLD, 0)
            end
            state.loaded_ammo = state.loaded_ammo + 1
        end
    end
end
-- loop 반복이 끝나거나 재장전이 끊기면 idle 상태로 나간다. 아니면 idle의 transition 함수가 다음 상태를 정한다.
function reload_state.transition(this, context, input)
    if (input == this.INPUT_RELOAD_RETREAT or input == INPUT_CANCEL_RELOAD) then
        if (context:getFireMode() == BURST) then
            context:runAnimation("reload_end_semi", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0.2)
        else
            context:runAnimation("reload_end", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0.2)
        end
        return this.main_track_states.idle
    end
    return this.main_track_states.idle.transition(this, context, input)
end

-- 발사 방식 전환 입력을 감지하면 바로 애니메이션을 멈추고 대기 상태로 돌아가야 한다
function inspect_state.transition(this, context, input)
    if (input == INPUT_FIRE_SELECT) then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))
        return this.main_track_states.idle
    end
    return main_track_states.inspect.transition(this, context, input)
end

-- 메타테이블 방식으로 기본 상태 기계의 속성을 상속한다
local M = setmetatable({
    main_track_states = setmetatable({
        idle = idle_state,
        start = start_state,
        reload = reload_state,
        inspect = inspect_state,
    }, {__index = main_track_states}),
    crosshair_state = crosshair_state,
    INPUT_RELOAD_RETREAT = "reload_retreat",
    gun_kick_state = setmetatable({},{__index = shoot_state}),
    base_track_state = setmetatable({},{__index = base_state})
}, {__index = default})
-- 먼저 부모 상태 기계의 초기화 함수를 호출한 뒤 자신의 초기화를 한다
function M:initialize(context)
    default.initialize(self, context)
    self.main_track_states.reload.need_ammo = 0
    self.main_track_states.reload.loaded_ammo = 0
end

function M:states()
    return {
        self.base_track_state,
        self.bolt_caught_states.normal,
        self.over_heat_states.normal,
        self.main_track_states.start,
        self.gun_kick_state,
        self.movement_track_states.idle,
        self.ADS_states.normal,
        self.slide_states.normal,
        self.crosshair_state
    }
end
-- 상태 기계 내보내기
return M