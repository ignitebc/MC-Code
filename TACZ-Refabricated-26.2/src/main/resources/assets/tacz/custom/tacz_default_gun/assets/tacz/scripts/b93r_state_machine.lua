-- 스크립트 위치가 "{네임스페이스}:{경로}"이면 require 형식은 "{네임스페이스}_{경로}"다
-- 주의! require로 얻은 내용은 고치지 말고 호출만 해야 한다
local default = require("tacz_default_state_machine")
local STATIC_TRACK_LINE = default.STATIC_TRACK_LINE
local MAIN_TRACK = default.MAIN_TRACK
local main_track_states = default.main_track_states
local static_track_top = default.static_track_top

local inspect_state = setmetatable({}, {__index = main_track_states.inspect})
local idle_state = setmetatable({}, {__index = main_track_states.idle})


-- obj.value++와 같다
local function increment(obj)
    obj.value = obj.value + 1
    return obj.value - 1
end

local FIRE_MODE_TRACK = increment(static_track_top)
local SWITCH_MODE_TRACK = increment(static_track_top)

-- 지금 탄약이 남아 있는지 확인한다
local function isNoAmmo(context)
    -- 여기서 총열과 탄창을 함께 확인했다
    return (not context:hasBulletInBarrel()) and (context:getAmmoCount() <= 0)
end

local function runReloadAnimation(context)
    -- 여기서 얻는 트랙은 주 트랙 줄에 있는 주 트랙이다
    local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
    local ext = context:getMagExtentLevel()
    -- 지금 총 전체에 탄약이 남아 있는지에 따라 전술 재장전을 재생할지 빈 탄창 재장전을 재생할지 정한다
    if (isNoAmmo(context)) then
        -- "ext"는 확장 부품 등급이며, "0"은 확장 부품을 달지 않았다는 뜻이다
        if (ext == 0) then
            context:runAnimation("reload_empty", track, false, PLAY_ONCE_STOP, 0.2)
        elseif (ext == 1) then
            context:runAnimation("reload_empty_xmag_1", track, false, PLAY_ONCE_STOP, 0.2)
        elseif (ext == 2) then
            context:runAnimation("reload_empty_xmag_2", track, false, PLAY_ONCE_STOP, 0.2)
        elseif (ext == 3) then
            context:runAnimation("reload_empty_xmag_3", track, false, PLAY_ONCE_STOP, 0.2)
        else
            context:runAnimation("reload_empty", track, false, PLAY_ONCE_STOP, 0.2)
        end
    else
        if (ext == 0) then
            context:runAnimation("reload_tactical", track, false, PLAY_ONCE_STOP, 0.2)
        elseif (ext == 1) then
            context:runAnimation("reload_tactical_xmag_1", track, false, PLAY_ONCE_STOP, 0.2)
        elseif (ext == 2) then
            context:runAnimation("reload_tactical_xmag_2", track, false, PLAY_ONCE_STOP, 0.2)
        elseif (ext == 3) then
            context:runAnimation("reload_tactical_xmag_3", track, false, PLAY_ONCE_STOP, 0.2)
        else
            context:runAnimation("reload_tactical", track, false, PLAY_ONCE_STOP, 0.2)
        end
    end
end

local function runInspectAnimation(context)
    -- 여기서 얻는 트랙은 주 트랙 줄에 있는 주 트랙이다
    local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
    local ext = context:getMagExtentLevel()
    -- 지금 총 전체에 탄약이 남아 있는지에 따라 일반 점검을 재생할지 빈 탄창 점검을 재생할지 정한다
    if (isNoAmmo(context)) then
        -- "ext"는 확장 부품 등급이며, "0"은 확장 부품을 달지 않았다는 뜻이다
        if (ext == 0) then
            context:runAnimation("inspect_empty", track, false, PLAY_ONCE_STOP, 0.2)
        elseif (ext == 1) then
            context:runAnimation("inspect_empty_xmag_1", track, false, PLAY_ONCE_STOP, 0.2)
        elseif (ext == 2) then
            context:runAnimation("inspect_empty_xmag_2", track, false, PLAY_ONCE_STOP, 0.2)
        elseif (ext == 3) then
            context:runAnimation("inspect_empty_xmag_3", track, false, PLAY_ONCE_STOP, 0.2)
        else
            context:runAnimation("inspect_empty", track, false, PLAY_ONCE_STOP, 0.2)
        end
    else
        if (ext == 0) then
            context:runAnimation("inspect", track, false, PLAY_ONCE_STOP, 0.2)
        elseif (ext == 1) then
            context:runAnimation("inspect_xmag_1", track, false, PLAY_ONCE_STOP, 0.2)
        elseif (ext == 2) then
            context:runAnimation("inspect_xmag_2", track, false, PLAY_ONCE_STOP, 0.2)
        elseif (ext == 3) then
            context:runAnimation("inspect_xmag_3", track, false, PLAY_ONCE_STOP, 0.2)
        else
            context:runAnimation("inspect", track, false, PLAY_ONCE_STOP, 0.2)
        end
    end
end

function idle_state.transition(this, context, input)
    if (input == INPUT_RELOAD) then
        runReloadAnimation(context)
        return this.main_track_states.idle
    end
    if (input == INPUT_PUT_AWAY) then
        local put_away_time = context:getPutAwayTime()
        -- 여기서 얻는 트랙은 주 트랙 줄에 있는 주 트랙이다
        local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
        -- 모드 전환 애니메이션을 멈춘다
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, SWITCH_MODE_TRACK))
        -- put_away 애니메이션을 재생하고, 전환 시간을 문맥에서 넘어온 put_away_time * 0.75로 둔다
        context:runAnimation("put_away", track, false, PLAY_ONCE_HOLD, put_away_time * 0.75)
        -- 애니메이션 진행도를 마지막 프레임으로 둔다
        context:setAnimationProgress(track, 1, true)
        -- 애니메이션 진행도를 {put_away_time}만큼 앞으로 돌린다
        context:adjustAnimationProgress(track, -put_away_time, false)
        return this.main_track_states.final
    end
    if (input == INPUT_INSPECT) then
        runInspectAnimation(context)
        return inspect_state
    end
    return main_track_states.idle.transition(this, context, input)
end

-- 여기는 발사 방식 트랙의 상태이며 조정간 애니메이션 전용이다. 다른 동작과 호환되며 필요하면 세 가지 사격 방식(semi, burst, auto)을 넣을 수 있다
local fire_mode_state = {
    -- 반자동 상태
    semi = {},
    -- 점사 상태
    burst = {},
    -- 총 꺼내기 상태
    draw = {}
}
-- 이 부분은 총을 꺼낼 때(draw 재생) 총이 어떤 사격 방식인지 확인해 그 방식의 idle로 바꾸는 데만 쓴다
function fire_mode_state.draw.update(this, context)
    context:trigger(this.INPUT_MODE_DRAW)
end

function fire_mode_state.draw.transition(this, context,input)
    if (input == this.INPUT_MODE_DRAW) then
        if (context:getFireMode() == SEMI) then
            context:runAnimation("static_semi", context:getTrack(STATIC_TRACK_LINE, FIRE_MODE_TRACK), true, PLAY_ONCE_HOLD, 0)
            return fire_mode_state.semi
        elseif (context:getFireMode() == BURST) then
            context:runAnimation("static_burst", context:getTrack(STATIC_TRACK_LINE, FIRE_MODE_TRACK), true, PLAY_ONCE_HOLD, 0)
            return fire_mode_state.burst
        end
    end
end
-- 주의! 뒤에 나오는 발사 방식별 상태 사이의 전환은 data에 적은 순서대로 해야 한다
function fire_mode_state.semi.update(this, context)
    -- 특정 발사 방식 상태에 들어가면 애니메이션을 걸어 둔다
    local track = context:getTrack(STATIC_TRACK_LINE, FIRE_MODE_TRACK)
    if (context:isHolding(track)) then
        context:runAnimation("static_semi", track, true, PLAY_ONCE_HOLD, 0)
    end
    -- 특정 발사 방식의 입력 상태를 정의한다
    if (context:getFireMode() == BURST) then
        context:trigger(this.INPUT_MODE_BURST)
    end
end
-- 해당 입력 상태를 감지하면 해당 조정간 애니메이션을 재생한다
function fire_mode_state.semi.transition(this, context,input)
    if(input == this.INPUT_MODE_BURST)then
        context:runAnimation("switch_burst", context:getTrack(STATIC_TRACK_LINE, SWITCH_MODE_TRACK), false, PLAY_ONCE_STOP, 0)
        return fire_mode_state.burst
    end
-- 발사 입력, 재장전 입력, 점검 입력을 감지하면 애니메이션 재생을 멈춘다. 아니면 두 애니메이션이 서로 다른 트랙에서 재생되어 애니메이션 이음 문제가 생긴다
    if(input == INPUT_SHOOT)then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, SWITCH_MODE_TRACK))
    end
    if(input == INPUT_RELOAD)then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, SWITCH_MODE_TRACK))
    end
    if(input == INPUT_INSPECT)then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, SWITCH_MODE_TRACK))
    end
end
-- 위와 같은 이유다
function fire_mode_state.burst.update(this, context)
    local track = context:getTrack(STATIC_TRACK_LINE, FIRE_MODE_TRACK)
    if (context:isHolding(track)) then
        context:runAnimation("static_burst", track, true, PLAY_ONCE_HOLD, 0)
    end
    if (context:getFireMode() == SEMI) then
        context:trigger(this.INPUT_MODE_SEMI)
    end
end

function fire_mode_state.burst.transition(this, context,input)
    if(input == this.INPUT_MODE_SEMI)then
        context:runAnimation("switch_semi", context:getTrack(STATIC_TRACK_LINE, SWITCH_MODE_TRACK), false, PLAY_ONCE_STOP, 0)
        return fire_mode_state.semi
    end

    if(input == INPUT_SHOOT)then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, SWITCH_MODE_TRACK))
    end
    if(input == INPUT_RELOAD)then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, SWITCH_MODE_TRACK))
    end
    if(input == INPUT_INSPECT)then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, SWITCH_MODE_TRACK))
    end
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
        inspect = inspect_state
    }, {__index = main_track_states}),
    FIRE_MODE_TRACK = FIRE_MODE_TRACK,
    SWITCH_MODE_TRACK = SWITCH_MODE_TRACK,
    INPUT_MODE_SEMI = "input_mode_semi",
    INPUT_MODE_BURST = "input_mode_burst",
    INPUT_MODE_AUTO = "input_mode_auto",
    INPUT_MODE_DRAW = "input_mode_draw",
    fire_mode_state = fire_mode_state
}, {__index = default})
function M:initialize(context)
    default.initialize(self, context)
end
-- 기본 상태 기계를 상속하면 상태를 다시 초기화해야 한다
function M:states()
    return {
        self.base_track_state,
        self.bolt_caught_states.normal,
        self.main_track_states.start,
        self.gun_kick_state,
        self.movement_track_states.idle,
        self.ADS_states.normal,
        self.slide_states.normal,
        self.fire_mode_state.draw
    }
end
-- 상태 기계 내보내기
return M