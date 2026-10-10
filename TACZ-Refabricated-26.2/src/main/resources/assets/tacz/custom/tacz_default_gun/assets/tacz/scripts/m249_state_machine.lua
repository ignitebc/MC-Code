-- 스크립트 위치가 "{네임스페이스}:{경로}"이면 require 형식은 "{네임스페이스}_{경로}"다
-- 주의! require로 얻은 내용은 고치지 말고 호출만 해야 한다
local default = require("tacz_default_state_machine")
local STATIC_TRACK_LINE = default.STATIC_TRACK_LINE
local BOLT_CAUGHT_TRACK = default.BOLT_CAUGHT_TRACK
local bolt_caught_states = default.bolt_caught_states

local normal_state = setmetatable({}, {__index = bolt_caught_states.normal})


-- 지금 탄약이 남아 있는지 확인한다
local function isNoAmmo(context)
    -- 여기서 총열과 탄창을 함께 확인했다
    return (not context:hasBulletInBarrel()) and (context:getAmmoCount() <= 0)
end

-- "고정 안 함" 상태 갱신
function normal_state.update(this, context)
    -- 탄약 수가 0이 되면 곧바로 "고정" 상태로 가는 입력을 한 번 직접 일으킨다
    if (isNoAmmo(context)) then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, BOLT_CAUGHT_TRACK))
        context:trigger(this.INPUT_BOLT_CAUGHT)
    else
        local a = context:getAmmoCount()
        if (a < 9) then
            context:setAnimationProgress(context:getTrack(STATIC_TRACK_LINE, BOLT_CAUGHT_TRACK),0.1+(8-a)*0.5,false)
        else
            context:setAnimationProgress(context:getTrack(STATIC_TRACK_LINE, BOLT_CAUGHT_TRACK),0.1,false)
        end
    end
end

-- "고정 안 함" 상태 진입
function normal_state.entry(this, context)
    context:runAnimation("static_ammo_display", context:getTrack(STATIC_TRACK_LINE, BOLT_CAUGHT_TRACK), false, PLAY_ONCE_STOP, 0)
    this.bolt_caught_states.normal.update(this, context)
end


-- 메타테이블 방식으로 기본 상태 기계의 속성을 상속한다
local M = setmetatable({
    bolt_caught_states = setmetatable({
        normal = normal_state,
    }, {__index = bolt_caught_states}),
}, {__index = default})
function M:initialize(context)
    default.initialize(self, context)
end
-- 기본 상태 기계를 상속하면 상태를 다시 초기화해야 한다
function M:states()
    return {
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