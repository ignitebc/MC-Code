-- 스크립트 위치가 "{네임스페이스}:{경로}"이면 require 형식은 "{네임스페이스}_{경로}"다
-- 주의! require로 얻은 내용은 고치지 말고 호출만 해야 한다
local default = require("tacz_default_state_machine")
local GUN_KICK_TRACK_LINE = default.GUN_KICK_TRACK_LINE
local gun_kick_state = default.gun_kick_state
-- main_track_states.idle은 우리가 다시 쓸 상태다.
local shoot_state = setmetatable({}, {__index = gun_kick_state})
-- reload_state, bolt_state는 한 발씩 장전하려고 정의한 새 상태다

function shoot_state.transition(this, context, input)
    -- 플레이어가 발사 키를 누르면 사격 트랙 줄에서 빈 트랙을 찾아 사격 애니메이션을 재생해야 한다(빈 트랙이 없으면 새로 할당한다). 사격 애니메이션은 아래로 혼합해야 한다는 점에 주의한다
    if (input == INPUT_SHOOT) then
        local track = context:findIdleTrack(GUN_KICK_TRACK_LINE, false)
        -- 여기는 혼합 애니메이션이며 보통 겹칠 수 있는 gun kick이다
        if (context:getFireMode() == BURST) then
            context:runAnimation("shoot_burst", track, true, PLAY_ONCE_STOP, 0)
        else
            context:runAnimation("shoot", track, true, PLAY_ONCE_STOP, 0)
        end
    end
    return nil
end

-- 메타테이블 방식으로 기본 상태 기계의 속성을 상속한다
local M = setmetatable({
    gun_kick_state = shoot_state
}, {__index = default})
-- 먼저 부모 상태 기계의 초기화 함수를 호출한 뒤 자신의 초기화를 한다
function M:initialize(context)
    default.initialize(self, context)
end
-- 상태 기계 내보내기
return M