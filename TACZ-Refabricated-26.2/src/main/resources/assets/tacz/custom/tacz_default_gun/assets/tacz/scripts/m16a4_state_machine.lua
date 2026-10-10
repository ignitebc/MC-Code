-- 스크립트 위치가 "{네임스페이스}:{경로}"이면 require 형식은 "{네임스페이스}_{경로}"다
-- 주의! require로 얻은 내용은 고치지 말고 호출만 해야 한다
local default = require("tacz_default_state_machine")
local GUN_KICK_TRACK_LINE = default.GUN_KICK_TRACK_LINE

local function isEaster()
    local flag = math.random(1, 1000)
    -- 아래 줄의 숫자를 바꿔 확률을 정한다. 확률은 천분의 x다
    if (flag <= 100) then
        return true,
        print('eastertrue')
    end
    return false
end

local handle_state = {
    attachment = "1",
    easter = false
}

function handle_state.entry(this, context)
    handle_state.attachment = context:getAttachment("GRIP")
end

function handle_state.update(this, context)
    if (handle_state.attachment ~= context:getAttachment("GRIP")) then
        handle_state.attachment = context:getAttachment("GRIP")
        handle_state.easter = isEaster()
    end
    local track = context:findIdleTrack(GUN_KICK_TRACK_LINE, false)
    if (context:getAttachment("SCOPE") == "tacz:scope_acog_ta31") then

        if ((context:getAttachment("GRIP") ~= "tacz:empty") and handle_state.easter) then
            context:runAnimation("handle_on", track, true, PLAY_ONCE_STOP, 0)
        else
            context:runAnimation("handle_off", track, true, PLAY_ONCE_STOP, 0)
        end
    else
    context:runAnimation("handle_off", track, true, PLAY_ONCE_STOP, 0)
    end
end


-- 메타테이블 방식으로 기본 상태 기계의 속성을 상속한다
local M = setmetatable({
    handle_state = handle_state
}, {__index = default})
function M:initialize(context)
    default.initialize(self, context)
end
-- 기본 상태 기계를 상속하면 상태를 다시 초기화해야 한다
function M:states()
    return {
        self.handle_state,
        self.base_track_state,
        self.bolt_caught_states.normal,
        self.over_heat_states.normal,
        self.main_track_states.start,
        self.gun_kick_state,
        self.movement_track_states.idle,
        self.ADS_states.normal,
        self.slide_states.normal
    }
end
-- 상태 기계 내보내기
return M