-- 스크립트 위치가 "{네임스페이스}:{경로}"이면 require 형식은 "{네임스페이스}_{경로}"다
-- 주의! require로 얻은 내용은 고치지 말고 호출만 해야 한다
local default = require("tacz_default_state_machine")
local STATIC_TRACK_LINE = default.STATIC_TRACK_LINE
local BLENDING_TRACK_LINE = default.BLENDING_TRACK_LINE
local GUN_KICK_TRACK_LINE = default.GUN_KICK_TRACK_LINE
local BOLT_CAUGHT_TRACK = default.BOLT_CAUGHT_TRACK
local SLIDE_TRACK = default.SLIDE_TRACK
local MAIN_TRACK = default.MAIN_TRACK
local bolt_caught_states = default.bolt_caught_states
local main_track_states = default.main_track_states
local main_states = setmetatable({}, {__index = main_track_states.idle})

local normal_state = setmetatable({}, {__index = bolt_caught_states.normal})

local function isEaster()
    local flag = math.random(1, 1000)
    -- 아래 줄의 숫자를 바꿔 확률을 정한다. 확률은 천분의 x다
    if (flag <= 20) then
        return true
    end
    return false
end

-- 총 버리기 애니메이션 재생 메서드
local function runPutAwayAnimation(context)
    local put_away_time = context:getPutAwayTime()
    -- 여기서 얻는 트랙은 주 트랙 줄에 있는 주 트랙이다
    local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
    -- put_away 애니메이션을 재생하고, 전환 시간을 문맥에서 넘어온 put_away_time * 0.75로 둔다
    context:runAnimation("put_away", track, false, PLAY_ONCE_HOLD, put_away_time * 0.75)
    -- 애니메이션 진행도를 마지막 프레임으로 둔다
    context:setAnimationProgress(track, 1, true)
    -- 애니메이션 진행도를 {put_away_time}만큼 앞으로 돌린다
    context:adjustAnimationProgress(track, -put_away_time, false)
end

-- 지금 탄약이 남아 있는지 확인한다
local function isNoAmmo(context)
    -- 여기서 총열과 탄창을 함께 확인했다
    return (not context:hasBulletInBarrel()) and (context:getAmmoCount() <= 0)
end

-- 점검 애니메이션 재생 메서드
local function runInspectAnimation(context)
    -- 여기서 얻는 트랙은 주 트랙 줄에 있는 주 트랙이다
    local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
    -- 지금 총 전체에 탄약이 남아 있는지에 따라 일반 점검을 재생할지 빈 탄창 점검을 재생할지 정한다
    if (isNoAmmo(context)) then
        context:runAnimation("inspect_empty", track, false, PLAY_ONCE_STOP, 0.2)
    else
        context:runAnimation("inspect", track, false, PLAY_ONCE_STOP, 0.2)
    end
end

-- 재장전 애니메이션 재생 메서드
local function runReloadAnimation(context)
    -- 여기서 얻는 트랙은 주 트랙 줄에 있는 주 트랙이다
    local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
    -- 지금 총 전체에 탄약이 남아 있는지에 따라 전술 재장전을 재생할지 빈 탄창 재장전을 재생할지 정한다
    if (isNoAmmo(context)) then
        if (isEaster()) then
            context:runAnimation("reload_empty_easter", track, false, PLAY_ONCE_STOP, 0.2)
        else
            context:runAnimation("reload_empty", track, false, PLAY_ONCE_STOP, 0.2)
        end
    else
        if (isEaster()) then
            context:runAnimation("reload_tactical_easter", track, false, PLAY_ONCE_STOP, 0.2)
        else
            context:runAnimation("reload_tactical", track, false, PLAY_ONCE_STOP, 0.2)
        end
    end
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

local sight_state = {}

function sight_state.update(this, context)
    local track = context:findIdleTrack(GUN_KICK_TRACK_LINE, false)
    if (context:getAttachment("SCOPE") == "tacz:empty") then
        context:runAnimation("sight_on", track, true, PLAY_ONCE_STOP, 0)
    else
        context:runAnimation("sight_off", track, true, PLAY_ONCE_STOP, 0)
    end
end

-- "고정 안 함" 상태 진입
function normal_state.entry(this, context)
    context:runAnimation("static_ammo_display", context:getTrack(STATIC_TRACK_LINE, BOLT_CAUGHT_TRACK), false, PLAY_ONCE_STOP, 0)
    this.bolt_caught_states.normal.update(this, context)
end

local crawl_states = {
    draw = {},
    normal = {},
    crawl = {},
    played_animation = 0
}

function crawl_states.normal.transition(this, context, input)
    -- 엎드리면 엎드린 상태로 바꾼다
    if (context:isCrawl()) then
        return this.crawl_states.crawl
    end
end

function crawl_states.crawl.entry(this, context)
    -- 주 트랙 애니메이션 표시 초기화
    crawl_states.played_animation = 0
end

function crawl_states.crawl.update(this, context)
    -- 주 트랙이 애니메이션을 재생 중이고 엎드리기 트랙에 애니메이션이 없으면 양각대만 펼치는 애니메이션을 재생한다
    if ((not context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))) and context:isStopped(context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK))) then
        context:runAnimation("crawl_bipod", context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK), true, PLAY_ONCE_HOLD, 0.5)
        crawl_states.played_animation = 1
    end
    -- 주 트랙에 애니메이션이 없고 엎드리기 트랙에도 애니메이션이 없으면 엎드리기 시작 동작을 재생한다
    if (context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)) and context:isStopped(context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK))) then
        context:runAnimation("crawl_start", context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK), true, PLAY_ONCE_HOLD, 0.5)
    end
    -- 주 트랙에 애니메이션이 없고 엎드리기 트랙이 걸려 있으면(사실상 시작 동작 재생이 끝난 것) 엎드린 채 유지 동작을 재생한다
    if (context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)) and context:isHolding(context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK))) then
        -- 주 트랙이 애니메이션을 재생한 적이 없으면 엎드린 동작을 계속 재생한다
        if (crawl_states.played_animation == 0) then
            context:runAnimation("crawl", context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK), true, PLAY_ONCE_HOLD, 0.4)
        -- 주 트랙이 애니메이션을 재생한 적이 있으면 따로 된 팔 복귀 애니메이션으로 엎드린 상태로 돌아가고 표시를 초기화한다
        elseif (crawl_states.played_animation == 1) then
            context:runAnimation("crawl_handup", context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK), true, PLAY_ONCE_HOLD, 0.4)
            crawl_states.played_animation = 0
        end
    end
    -- 주 트랙이 애니메이션을 재생 중이고 엎드리기 트랙이 걸려 있으면 엎드리기 겹침 층을 지우고 표시를 1로 둔다
    if ((not context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))) and context:isHolding(context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK))) then
        if (crawl_states.played_animation == 0) then
            context:runAnimation("crawl_handdown", context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK), true, PLAY_ONCE_HOLD, 0.4)
            crawl_states.played_animation = 1
        end
    end
end

function crawl_states.crawl.transition(this, context, input)
    if(not context:isCrawl() or this.main_track_states.final.isfinal == 1) then
        if (not context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))) then
            context:runAnimation("crawl_bipod_end", context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK), true, PLAY_ONCE_STOP, 0.2)
        else
            context:runAnimation("crawl_end", context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK), true, PLAY_ONCE_STOP, 0.2)
        end
        print("exit")
        return this.crawl_states.normal
    end
end

-- 대기 상태에서 빠져나감
function main_states.transition(this, context, input)
    -- 플레이어가 총에서 다른 아이템으로 바꾸면 총 버리기 신호가 자동으로 들어오므로 직접 일으킬 필요 없이 감지만 하면 된다
    if (input == INPUT_PUT_AWAY) then
        runPutAwayAnimation(context)
        this.main_track_states.final.isfinal = 1
        -- 총을 버린 뒤 최종 상태로 간다
        return this.main_track_states.final
    end
    -- 플레이어가 총을 들고 R(또는 직접 지정한 재장전 키)을 누르면 재장전 신호가 자동으로 들어온다
    if (input == INPUT_RELOAD) then
        runReloadAnimation(context)
        -- 재장전 애니메이션이 끝나면 대기 상태로 돌아간다(곧 자기 자신으로 돌아간다)
        return this.main_track_states.idle
    end
    -- 플레이어가 사격하면 shoot 신호가 자동으로 들어온다
    if (input == INPUT_SHOOT) then
        context:popShellFrom(0) -- 기본으로 사격하면 탄피를 배출한다
        -- 대기 상태로 돌아간다(곧 자기 자신으로 돌아간다). 여기서 사격 애니메이션을 재생하지 않는 것은 사격 애니메이션을 gun_kick 상태에서 재생해야 하기 때문이다
        return this.main_track_states.idle
    end
    -- 플레이어가 볼트 액션 무기로 사격한 뒤 노리쇠를 당기면 bolt 신호가 자동으로 들어온다
    if (input == INPUT_BOLT) then
        context:runAnimation("bolt", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0.2)
        -- 노리쇠 당기기 애니메이션이 끝나면 대기 상태로 돌아간다
        return this.main_track_states.idle
    end
    -- 플레이어가 점검 키를 누르면 점검 신호가 들어온다
    if (input == INPUT_INSPECT and context:getAimingProgress() < 1) then
        runInspectAnimation(context)
        -- 점검은 점검 상태로 가야 한다. 점검 중에는 화면 가운데 조준점이 숨겨지므로 조준점을 조절할 점검 상태가 필요하다
        return this.main_track_states.inspect
    end
    -- 플레이어가 근접 무기를 쓸 때 들어오는 근접 신호. 근접 부착물, 개머리판, 밀치기 세 경우로 나뉜다
    -- 근접 부착물은 여러 근접 애니메이션을 쓸 수 있지만, 개머리판과 밀치기는 총 설정 파일에 적힌 "근접 부착물이 없을 때의 근접 공격"이라 애니메이션 하나만 쓸 수 있다
    if (input == INPUT_BAYONET_MUZZLE) then
        -- 여기는 애니메이션을 차례로 재생하는 메서드다. 상태에 저장한 counter로 지금 몇 번째 근접 애니메이션을 재생할지 정하며, animationName은 조합한 문자열이다
        -- 이렇게 쓰면 "melee_bayonet_1" "melee_bayonet_2" "melee_bayonet_3" 세 애니메이션을 차례로 실행하고, 3이 끝난 뒤 다시 근접 공격하면 1로 돌아간다
        local counter = this.main_track_states.bayonet_counter
        local animationName = "melee_bayonet_" .. tostring(counter + 1)
        this.main_track_states.bayonet_counter = (counter + 1) % 3
        context:runAnimation(animationName, context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0.2)
        return this.main_track_states.idle
    end
    -- 개머리판 치기를 마치면 대기 상태로 돌아간다
    if (input == INPUT_BAYONET_STOCK) then
        context:runAnimation("melee_stock", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0.2)
        return this.main_track_states.idle
    end
    -- 밀치기를 마치면 대기 상태로 돌아간다
    if (input == INPUT_BAYONET_PUSH) then
        context:runAnimation("melee_push", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0.2)
        return this.main_track_states.idle
    end
end

-- 메타테이블 방식으로 기본 상태 기계의 속성을 상속한다
local M = setmetatable({
    bolt_caught_states = setmetatable({
        normal = normal_state,
    }, {__index = bolt_caught_states}),
    main_track_states = setmetatable({
        idle = main_states
    }, {__index = main_track_states}),
    crawl_states = crawl_states,
    sight_state = sight_state
}, {__index = default})
function M:initialize(context)
    default.initialize(self, context)
end
-- 기본 상태 기계를 상속하면 상태를 다시 초기화해야 한다
function M:states()
    return {
        self.sight_state,
        self.base_track_state,
        self.bolt_caught_states.normal,
        self.over_heat_states.normal,
        self.main_track_states.start,
        self.gun_kick_state,
        self.movement_track_states.idle,
        self.ADS_states.normal,
        self.slide_states.normal,
        self.crawl_states.normal
    }
end
-- 상태 기계 내보내기
return M