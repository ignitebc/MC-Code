-- 스크립트 위치가 "{네임스페이스}:{경로}"이면 require 형식은 "{네임스페이스}_{경로}"다
-- 주의! require로 얻은 내용은 고치지 말고 호출만 해야 한다
local default = require("tacz_default_state_machine")
local STATIC_TRACK_LINE = default.STATIC_TRACK_LINE
local GUN_KICK_TRACK_LINE = default.GUN_KICK_TRACK_LINE
local MAIN_TRACK = default.MAIN_TRACK
local main_track_states = default.main_track_states
-- main_track_states.idle은 우리가 다시 쓸 상태다.
local idle_state = setmetatable({}, {__index = main_track_states.idle})

local charge_state = {
    can_charge = true
}

-- 평상시 감지(지연 방아쇠)
function idle_state.update(this, context)
    if (context:isCharging()) then
        if (charge_state.can_charge) then
            context:trigger(this.INPUT_CHARING)
        end
    else
        charge_state.can_charge = true
    end
end

-- idle 상태의 transition 함수를 다시 써서 INPUT_CHARING 입력을 새로 정의한 charge_state 상태로 돌린다
function idle_state.transition(this, context, input)
    -- 지연 방아쇠 상태 진입
    if (input == this.INPUT_CHARING) then
        context:runAnimation("charge_in", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_HOLD, 0)
        return this.main_track_states.charge
    end
    return main_track_states.idle.transition(this, context, input)
end

-- 지연 방아쇠 상태 진입
function charge_state.update(this, context)
    if (not context:isCharging()) then
        charge_state.can_charge = true
        context:trigger(this.INPUT_CHARING_EXIT)
    end
end

-- 지연 방아쇠 상태에서 나감
function charge_state.transition(this, context, input)
    if (input == INPUT_SHOOT) then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))
        charge_state.can_charge = false
        return this.main_track_states.idle
    end
    if (input == this.INPUT_CHARING_EXIT) then
        context:runAnimation("charge_out", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0.3)
        return this.main_track_states.idle
    end
end

-- 메타테이블 방식으로 기본 상태 기계의 속성을 상속한다
local M = setmetatable({
    main_track_states = setmetatable({
        idle = idle_state,
        charge = charge_state
    }, {__index = main_track_states}),
    INPUT_RELOAD_RETREAT = "reload_retreat",
    INPUT_CHARING = "input_charging",
    INPUT_CHARING_EXIT = "input_charging_exit"
}, {__index = default})
-- 먼저 부모 상태 기계의 초기화 함수를 호출한 뒤 자신의 초기화를 한다
function M:initialize(context)
    default.initialize(self, context)
    self.main_track_states.charge.can_charge = true
end
-- 상태 기계 내보내기
return M