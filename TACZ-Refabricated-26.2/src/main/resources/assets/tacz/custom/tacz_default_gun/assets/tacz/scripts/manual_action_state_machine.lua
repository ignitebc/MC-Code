-- 스크립트 위치가 "{네임스페이스}:{경로}"이면 require 형식은 "{네임스페이스}_{경로}"다
-- 주의! require로 얻은 내용은 고치지 말고 호출만 해야 한다
local default = require("tacz_default_state_machine")
local STATIC_TRACK_LINE = default.STATIC_TRACK_LINE
local MAIN_TRACK = default.MAIN_TRACK
local main_track_states = default.main_track_states
-- main_track_states.idle은 우리가 다시 쓸 상태다.
local idle_state = setmetatable({}, {__index = main_track_states.idle})
-- bolt_state는 정해진 때에 탄피를 배출하려고 정의한 새 상태다
local bolt_state = {
    timestamp = -1,
    ejection_time = 0,
    popped = false
}
-- idle 상태의 transition 함수를 다시 써서 INPUT_RELOAD 입력을 bolt_state 상태로 돌린다
function idle_state.transition(this, context, input)
    if (input == INPUT_BOLT) then
        context:runAnimation("bolt", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0.2)
        return this.main_track_states.bolt
    end
    if (input == INPUT_SHOOT) then
        -- 수동 장전 총은 자동으로 탄피를 배출하지 않으므로 탄피 배출을 건너뛴다
        return this.main_track_states.idle
    end
    return main_track_states.idle.transition(this, context, input)
end
-- bolt 상태에 들어가 타임스탬프와 매개변수를 초기화한다
function bolt_state.entry(this, context)
    local state = this.main_track_states.bolt
    local ejection_time = context:getStateMachineParams().bolt_shell_ejecting_time
    if (ejection_time) then
        state.ejection_time = ejection_time * 1000
    else
        state.ejection_time = 0
    end
    state.timestamp = context:getCurrentTimestamp()
    state.popped = false
end
-- 알맞은 때에 탄피 배출 로직을 실행한 뒤 상태에서 나간다
function bolt_state.update(this, context)
    local state = this.main_track_states.bolt
    if (state.popped) then
        return
    end
    local base_timestamp = state.timestamp
    local current_timestamp = context:getCurrentTimestamp()
    if (current_timestamp - base_timestamp > state.ejection_time) then
        context:popShellFrom(0)
        state.popped = true
        context:trigger(this.INPUT_BOLT_RETREAT)
    end
end
function bolt_state.transition(this, context, input)
    if (input == this.INPUT_BOLT_RETREAT) then
        return this.main_track_states.idle
    end
    return this.main_track_states.idle.transition(this, context, input)
end
-- 메타테이블 방식으로 기본 상태 기계의 속성을 상속한다
local M = setmetatable({
    main_track_states = setmetatable({
        -- 사용자 정의 idle 상태는 부모 상태 기계의 해당 상태를 덮어써야 하며, 새로 만든 bolt 상태도 넣어야 한다
        idle = idle_state,
        bolt = bolt_state,
    }, {__index = main_track_states}),
    INPUT_BOLT_RETREAT = "bolt_retreat"
}, {__index = default})
-- 상태 기계 내보내기
return M