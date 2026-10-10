-- 스크립트 위치가 "{네임스페이스}:{경로}"이면 require 형식은 "{네임스페이스}_{경로}"다
-- 주의! require로 얻은 내용은 고치지 말고 호출만 해야 한다
local default = require("tacz_manual_action_state_machine")
local STATIC_TRACK_LINE = default.STATIC_TRACK_LINE
local MAIN_TRACK = default.MAIN_TRACK
local main_track_states = default.main_track_states
-- main_track_states.idle은 우리가 다시 쓸 상태다.
local idle_state = setmetatable({}, {__index = main_track_states.idle})
-- reload_state, bolt_state는 한 발씩 장전하려고 정의한 새 상태다
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
    if (not context:hasBulletInBarrel() and context:getAmmoCount() <= 0) then
        context:runAnimation("inspect_empty", track, false, PLAY_ONCE_STOP, 0.2)
    elseif (context:getAmmoCount() <= 0) then
        context:runAnimation("inspect_01", track, false, PLAY_ONCE_STOP, 0.2)
    else
        context:runAnimation("inspect", track, false, PLAY_ONCE_STOP, 0.2)
    end
end

-- idle 상태의 transition 함수를 다시 써서 INPUT_RELOAD 입력을 새로 정의한 reload_state 상태로 돌린다
function idle_state.transition(this, context, input)
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
        context:runAnimation("reload_intro_empty", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_HOLD, 0.2)
    else
        state.timestamp = -1
        state.ejection_time = 0
        context:runAnimation("reload_intro", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_HOLD, 0.2)
    end
    state.need_ammo = context:getMaxAmmoCount() - context:getAmmoCount()
    state.loaded_ammo = 0
end
-- update 함수에서는 loop를 반복 재생하며 loaded_ammo 변수를 1씩 늘린다.
function reload_state.update(this, context)
    local state = this.main_track_states.reload
    -- reload_intro_empty의 탄피 배출 처리
    if (state.timestamp ~= -1 and context:getCurrentTimestamp() - state.timestamp > state.ejection_time) then
        context:popShellFrom(0)
        state.timestamp = -1
    end
    if (state.loaded_ammo > state.need_ammo or not context:hasAmmoToConsume()) then
        context:trigger(this.INPUT_RELOAD_RETREAT)
    else
        local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
        if (context:isHolding(track)) then
            context:runAnimation("reload_loop", track, false, PLAY_ONCE_HOLD, 0)
            state.loaded_ammo = state.loaded_ammo + 1
        end
    end
end
-- loop 반복이 끝나거나 재장전이 끊기면 idle 상태로 나간다. 아니면 idle의 transition 함수가 다음 상태를 정한다.
function reload_state.transition(this, context, input)
    if (input == this.INPUT_RELOAD_RETREAT or input == INPUT_CANCEL_RELOAD) then
        context:runAnimation("reload_end", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0.2)
        return this.main_track_states.idle
    end
    return this.main_track_states.idle.transition(this, context, input)
end
-- 메타테이블 방식으로 기본 상태 기계의 속성을 상속한다
local M = setmetatable({
    main_track_states = setmetatable({
        -- 사용자 정의 idle 상태는 부모 상태 기계의 해당 상태를 덮어써야 하며, 새로 만든 reload 상태도 넣어야 한다
        idle = idle_state,
        reload = reload_state
    }, {__index = main_track_states}),
    INPUT_RELOAD_RETREAT = "reload_retreat",
}, {__index = default})
-- 먼저 부모 상태 기계의 초기화 함수를 호출한 뒤 자신의 초기화를 한다
function M:initialize(context)
    default.initialize(self, context)
    self.main_track_states.reload.need_ammo = 0
    self.main_track_states.reload.loaded_ammo = 0
end
-- 상태 기계 내보내기
return M