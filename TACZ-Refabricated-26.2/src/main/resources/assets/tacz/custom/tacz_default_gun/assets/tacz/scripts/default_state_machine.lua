-- 이 스택 꼭대기 포인터들은 새 트랙 줄과 트랙을 할당할 때 쓰인다
-- 트랙 줄 스택 꼭대기 포인터
local track_line_top = {value = 0}
-- 주 트랙 줄의 트랙 스택 꼭대기 포인터
local static_track_top = {value = 0}
-- 혼합 트랙 줄의 트랙 스택 꼭대기 포인터
local blending_track_top = {value = 0}
-- 스택 꼭대기 포인터 증가 함수. 새 트랙 줄이나 트랙을 할당하는 데 쓴다
local function increment(obj)
    obj.value = obj.value + 1
    return obj.value - 1
end

-- 주 트랙 줄과 그 안의 트랙
local STATIC_TRACK_LINE = increment(track_line_top)

-- 낮은 트랙 다섯 개. 주 트랙 줄의 다른 상위 트랙에 덮인다
local PRE_PARALLEL_TRACK_1 = increment(static_track_top)
local PRE_PARALLEL_TRACK_2 = increment(static_track_top)
local PRE_PARALLEL_TRACK_3 = increment(static_track_top)
local PRE_PARALLEL_TRACK_4 = increment(static_track_top)
local PRE_PARALLEL_TRACK_5 = increment(static_track_top)

-- 주 트랙 줄 위의 상위 트랙
local BASE_TRACK = increment(static_track_top)
local BOLT_CAUGHT_TRACK = increment(static_track_top)
local SAFETY_TRACK = increment(static_track_top) -- 구현 예정
local ADS_TRACK = increment(static_track_top)
local MAIN_TRACK = increment(static_track_top)
local SPRINT_TRACK = increment(static_track_top)

-- 최상위 트랙 다섯 개. 주 트랙 줄의 다른 트랙을 덮는다
local PARALLEL_TRACK_1 = increment(static_track_top)
local PARALLEL_TRACK_2 = increment(static_track_top)
local PARALLEL_TRACK_3 = increment(static_track_top)
local PARALLEL_TRACK_4 = increment(static_track_top)
local PARALLEL_TRACK_5 = increment(static_track_top)

-- 발사 트랙 줄
local GUN_KICK_TRACK_LINE = increment(track_line_top)

-- 혼합 트랙 줄과 그 안의 트랙. 달리기, 걷기, 점프, 과열 같은 애니메이션을 겹치는 데 쓴다
local BLENDING_TRACK_LINE = increment(track_line_top)

local MOVEMENT_TRACK = increment(blending_track_top)
local SLIDE_TRACK = increment(blending_track_top)
local OVER_HEAT_TRACK = increment(blending_track_top)
local OVER_HEATING_TRACK = increment(blending_track_top)
local LOOP_TRACK = increment(blending_track_top)

-- 혼합 트랙 다섯 개. 다른 트랙 위에 겹쳐야 한다
local BLEND_TRACK_1 = increment(blending_track_top)
local BLEND_TRACK_2 = increment(blending_track_top)
local BLEND_TRACK_3 = increment(blending_track_top)
local BLEND_TRACK_4 = increment(blending_track_top)
local BLEND_TRACK_5 = increment(blending_track_top)

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

-- 재장전 애니메이션 재생 메서드
local function runReloadAnimation(context)
    -- 여기서 얻는 트랙은 주 트랙 줄에 있는 주 트랙이다
    local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
    -- 지금 총 전체에 탄약이 남아 있는지에 따라 전술 재장전을 재생할지 빈 탄창 재장전을 재생할지 정한다
    if (isNoAmmo(context)) then
        context:runAnimation("reload_empty", track, false, PLAY_ONCE_STOP, 0.2)
    else
        context:runAnimation("reload_tactical", track, false, PLAY_ONCE_STOP, 0.2)
    end
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

-- 지금 과열 상태인지 확인한다
local function isOverHeat(context)
    return context:isOverHeat()
end

-- 기본 트랙 위의 상태. 이 상태는 static_idle 애니메이션을 반복 재생하는 데 쓴다.
local base_track_state = {}

-- 기본 상태에 들어가면 바로 static_idle을 재생한다
function base_track_state.entry(this, context)
    -- 주 트랙 줄의 기본 트랙에서 static_idle을 반복 재생한다
    context:runAnimation("static_idle", context:getTrack(STATIC_TRACK_LINE, BASE_TRACK), false, LOOP, 0)
    context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, BOLT_CAUGHT_TRACK))
end

-- 노리쇠 후퇴 고정 부분. 이 부분은 153번째 줄에서 끝난다
-- 노리쇠 후퇴 고정은 "고정"과 "고정 안 함" 두 가지라 여기서는 상태 두 개로 현재 무기를 조절해야 한다
-- 노리쇠 후퇴 고정의 두 상태는 서로 오가므로 하위 상태마다 아래 세 메서드로 조작해야 한다
-- entry 메서드는 그 상태에 들어갈 때 일어나는 일이며 상태에 들어갈 때 한 번만 실행된다
-- update 메서드는 그 상태에 있을 때 렌더링 프레임마다 호출된다. 게임 tick이 아니라 렌더링 프레임마다라는 점에 주의한다. 그래서 이 메서드의 실행 빈도는 tick보다 훨씬 높다(컴퓨터가 게임을 20프레임도 못 돌리는 경우가 아니라면)
-- transition 메서드는 그 상태에서 빠져나가는 것이며 다른 상태로 바뀔 때 한 번만 실행된다
--
-- 이 부분의 일반적인 구현 논리는
-- entry는 update에 들어가기 전에 관련 애니메이션을 재생하고 update로 들어가는 일을 맡는다
-- update는 상태에서 빠져나가야 하는지 실시간으로 확인하고, 필요하면 transition을 일으킨다
-- transition은 update가 보낸 정보를 받아, 일어난 뒤 관련 애니메이션을 멈추고 다른 상태로 넘어간다
-- 이 논리는 이후의 주 상태에서도 똑같이 적용된다
local bolt_caught_states = {
    -- normal은 노리쇠 후퇴 고정이 아닌 일반 상태다
    normal = {},
    -- bolt_caught는 노리쇠 후퇴 고정 상태다
    bolt_caught = {}
}

-- "고정 안 함" 상태 갱신
function bolt_caught_states.normal.update(this, context)
    -- 탄약 수가 0이 되면 곧바로 "고정" 상태로 가는 입력을 한 번 직접 일으킨다
    if (isNoAmmo(context)) then
        context:trigger(this.INPUT_BOLT_CAUGHT)
    end
end

-- "고정 안 함" 상태 진입
function bolt_caught_states.normal.entry(this, context)
    -- 고정 안 함 상태에 들어갈 때 할 일이 없으므로 아무것도 하지 않고 바로 그 상태로 넘어간다
    this.bolt_caught_states.normal.update(this, context)
end

-- "고정 안 함" 상태에서 빠져나감
function bolt_caught_states.normal.transition(this, context, input)
    -- "고정" 입력을 받으면 바로 "고정" 상태로 간다. "'고정' 입력"은 위의 update 메서드에서 나온다
    if (input == this.INPUT_BOLT_CAUGHT) then
        return this.bolt_caught_states.bolt_caught
    end
end

-- "고정" 상태 진입
function bolt_caught_states.bolt_caught.entry(this, context)
    -- 고정에 들어가면 주 트랙 줄의 고정 트랙에서 고정 애니메이션을 재생한다
    context:runAnimation("static_bolt_caught", context:getTrack(STATIC_TRACK_LINE, BOLT_CAUGHT_TRACK), false, LOOP, 0)
end

-- "고정" 상태 갱신
function bolt_caught_states.bolt_caught.update(this, context)
    -- 탄환 수가 0이 아니게 되면(이때는 재장전한 것) "고정 안 함" 상태로 가는 입력을 한 번 직접 일으킨다
    if (not isNoAmmo(context)) then
        context:trigger(this.INPUT_BOLT_NORMAL)
    end
end

-- "고정" 상태에서 빠져나감
function bolt_caught_states.bolt_caught.transition(this, context, input)
    -- 위의 update 메서드에서 입력을 받으면 "고정 안 함" 상태로 간다
    if (input == this.INPUT_BOLT_NORMAL) then
        -- "고정 안 함" 애니메이션이 따로 없으므로 여기서 고정 애니메이션을 멈춰야 "고정 안 함" 상태로 갈 수 있다. 아니면 재장전을 마친 뒤에도 여전히 고정 상태로 남는다
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, BOLT_CAUGHT_TRACK))
        return this.bolt_caught_states.normal
    end
end
-- 노리쇠 후퇴 고정 부분 끝

-- 주 트랙 상태. 이 부분은 271번째 줄에서 끝난다
-- 주 트랙은 무기의 기본 동작을 조절한다. 재장전, 점검, 총검 공격, 총 꺼내기, 총 버리기가 포함된다
-- 점검을 빼면 나머지 동작은 따로 상태로 조절할 필요가 없다.
-- 점검 상태는 사격 입력으로 끊겨야 한다. 또한 점검에 들어갈 때 조준점을 숨기고, 점검에서 나올 때 조준점을 되살려야 한다.
-- 위의 세 메서드 말고도 점검에는 조준점 렌더링을 되살리는 exit 메서드가 필요하다
local main_track_states = {
    -- 시작
    start = {},
    -- 대기. 플레이어가 총을 손에 들고 가만히 서서 아무것도 하지 않을 때가 이 경우다
    idle = {},
    -- 점검
    inspect = {},
    -- 끝
    final = {
        isfinal = -1
    },
    -- 총검 공격 카운터
    bayonet_counter = 0,
    -- 지연 방아쇠
    charge = {}
}

-- start에서 빠져나감(실제로는 총 꺼내기)
function main_track_states.start.transition(this, context, input)
    -- 플레이어가 총을 손에 드는 순간 draw 신호가 자동으로 들어오므로 직접 일으킬 필요가 없다
    if (input == INPUT_DRAW) then
        -- draw 신호를 받으면 주 트랙 줄의 주 트랙에서 총 꺼내기 애니메이션을 재생하고 대기 상태로 간다
        this.main_track_states.final.isfinal = -1
        context:runAnimation("draw", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0)
        return this.main_track_states.idle
    end
end

-- 대기 상태에서 빠져나감
function main_track_states.idle.transition(this, context, input)
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

-- 점검 상태 진입
function main_track_states.inspect.entry(this, context)
    -- 점검할 때는 화면 가운데 조준점을 숨겨야 한다
    context:setShouldHideCrossHair(true)
end

-- 점검 상태에서 나감
function main_track_states.inspect.exit(this, context)
    context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))
    -- 나간 뒤 화면 가운데 조준점을 되살린다
    context:setShouldHideCrossHair(false)
end

-- 점검 상태 갱신
function main_track_states.inspect.update(this, context)
    -- 애니메이션이 멈춘(재생이 끝난) 것을 감지하면 나가기 신호를 한 번 직접 일으킨다
    if (context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))) then
        context:trigger(this.INPUT_INSPECT_RETREAT)
    end
    if (context:isCharging()) then
        context:trigger(this.INPUT_INSPECT_BREAK_OUT)
    end
end

-- 점검 상태에서 빠져나감
function main_track_states.inspect.transition(this, context, input)
    -- update에서 온 나가기 신호를 받으면 대기 상태로 돌아간다. 이때 애니메이션을 멈출 필요가 없는 것은 update가 애니메이션이 이미 멈춘 뒤에야 나가기 신호를 보내기 때문이다
    if (input == this.INPUT_INSPECT_RETREAT) then
        return this.main_track_states.idle
    end
    -- update에서 온 끊기 신호를 받으면 대기 상태로 돌아가고 애니메이션을 멈춘다
    if (input == this.INPUT_INSPECT_BREAK_OUT) then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))
        return this.main_track_states.idle
    end
    -- 특별히 사격과 조준은 점검을 끊어야 한다. 사격 입력을 감지하거나 조준 진행도가 0이 아니면 바로 애니메이션을 멈추고 대기 상태로 돌아가야 한다
    if (input == INPUT_SHOOT or context:getAimingProgress() > 0) then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))
        return this.main_track_states.idle
    end
    return this.main_track_states.idle.transition(this, context, input)
end
-- 주 상태 부분 끝

-- 사격 상태. 조절할 것이 없다
local gun_kick_state = {}

function gun_kick_state.transition(this, context, input)
    -- 플레이어가 발사 키를 누르면 사격 트랙 줄에서 빈 트랙을 찾아 사격 애니메이션을 재생해야 한다(빈 트랙이 없으면 새로 할당한다). 사격 애니메이션은 아래로 혼합해야 한다는 점에 주의한다
    if (input == INPUT_SHOOT) then
        local track = context:findIdleTrack(GUN_KICK_TRACK_LINE, false)
        -- 여기는 혼합 애니메이션이며 보통 겹칠 수 있는 gun kick이다
        context:runAnimation("shoot", track, true, PLAY_ONCE_STOP, 0)
    end
    return nil
end

-- 이동 트랙의 상태. 이 부분은 450번째 줄에서 끝난다
local movement_track_states = {
    -- 가만히 있음(또는 공중에 있음)
    idle = {},
    -- 달리기. -1은 달리지 않음, 0은 달리는 중
    run = {
        mode = -1,
        time = 0
    },
    -- 걷기. -1은 걷지 않음, 0은 공중, 1은 조준 중, 2는 앞으로 걷기, 3은 뒤로 물러나기, 4는 옆으로 걷기
    walk = {
        mode = -1
    },
    -- 전술 질주
    sprint = {
        mode = -1
    }
}

-- 정지 상태 갱신
function movement_track_states.idle.update(this, context)
    -- 여기서 얻는 것은 혼합 트랙 줄의 이동 트랙이다
    local track = context:getTrack(BLENDING_TRACK_LINE, MOVEMENT_TRACK)
    -- 트랙이 비어 있으면 idle 애니메이션을 재생한다
    -- 여기서 idle 애니메이션을 entry에서 재생하도록 쓰지 않은 것은 트랙이 비었는지 실시간으로 확인해야 하기 때문이다
    if (context:isStopped(track) or context:isHolding(track)) then
        context:runAnimation("idle", track, true, LOOP, 0)
    end
end

-- 정지 상태에서 빠져나감
function movement_track_states.idle.transition(this, context, input)
    -- 플레이어가 달리면 달리기 상태로 간다
    if (input == INPUT_RUN) then
        if (context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))) then
            return this.movement_track_states.run
        else
            return this.movement_track_states.walk
        end
    -- 플레이어가 걸으면 걷기 상태로 간다
    elseif (input == INPUT_WALK) then
        return this.movement_track_states.walk
    end
end

-- 달리기 상태 진입
function movement_track_states.run.entry(this, context)
    this.movement_track_states.run.mode = -1
    this.movement_track_states.run.time = context:getCurrentTimestamp()
    -- 여기서 재생하는 트랙은 혼합 트랙 줄의 이동 트랙이고, 재생하는 애니메이션은 달리기 시작 동작이다. 재생이 끝나면 멈추지 않고 애니메이션을 걸어 둔다
    context:runAnimation("run_start", context:getTrack(BLENDING_TRACK_LINE, MOVEMENT_TRACK), true, PLAY_ONCE_HOLD, 0.2)
end

-- 달리기 상태에서 나감
function movement_track_states.run.exit(this, context)
    -- 이때 재생하는 애니메이션은 달리기를 마치고 idle로 돌아가는 애니메이션이며, 마찬가지로 재생 후 걸어 둔다
    context:runAnimation("run_end", context:getTrack(BLENDING_TRACK_LINE, MOVEMENT_TRACK), true, PLAY_ONCE_HOLD, 0.3)
end

-- 달리기 상태 갱신
function movement_track_states.run.update(this, context)
    local track = context:getTrack(BLENDING_TRACK_LINE, MOVEMENT_TRACK)
    local state = this.movement_track_states.run;
    -- run_start가 끝나길 기다린 뒤 run을 반복 재생한다. 여기서 판단 기준은 트랙이 걸려 있는지이며, 그래서 entry에서 애니메이션을 PLAY_ONCE_HOLD 방식으로 재생한다
    if (context:isHolding(track)) then
        context:runAnimation("run", track, true, LOOP, 0.2)
        -- 달리는지 확인하는 표시 0
        state.mode = 0
        context:anchorWalkDist() -- walkDist 기준점을 찍어 run 애니메이션의 시작점을 일정하게 한다
    end
    if (state.mode ~= -1) then
        if (not context:isOnGround()) then
            -- 플레이어가 공중에 있으면 run_hold 애니메이션을 재생해 총몸을 안정시킨다
            if (state.mode ~= 1) then
                state.mode = 1
                context:runAnimation("run_hold", track, true, LOOP, 0.6)
            end
        else
            -- 플레이어가 땅에 있으면 run 애니메이션으로 돌아간다
            if (state.mode ~= 0) then
                state.mode = 0
                context:runAnimation("run", track, true, LOOP, 0.2)
            end
            -- walkDist에 따라 run 애니메이션의 진행도를 설정한다
            context:setAnimationProgress(track, (context:getWalkDist() % 2.0) / 2.0, true)
        end
    end
end

-- 달리기 상태에서 빠져나감
function movement_track_states.run.transition(this, context, input)
    -- 대기 입력을 받으면 대기 상태로 간다
    if (input == INPUT_IDLE) then
        return this.movement_track_states.idle
    -- 걷기 입력을 받으면 걷기 상태로 간다
    elseif (input == INPUT_WALK or not context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))) then
        return this.movement_track_states.walk
    end
end


-- 걷기 상태 진입
function movement_track_states.walk.entry(this, context)
    -- 이때 표시를 -1로 두는 것은 초기화와 같다
    this.movement_track_states.walk.mode = -1
end

-- 걷기 상태에서 나감
function movement_track_states.walk.exit(this, context)
    -- idle 애니메이션을 한 번 직접 재생해 walk 애니메이션의 반복을 끊는다
    context:runAnimation("idle", context:getTrack(BLENDING_TRACK_LINE, MOVEMENT_TRACK), true, PLAY_ONCE_HOLD, 0.4)
end

-- 걷기 상태 갱신
function movement_track_states.walk.update(this, context)
    -- 여기서 얻는 것은 혼합 트랙 줄의 이동 트랙이다
    local track = context:getTrack(BLENDING_TRACK_LINE, MOVEMENT_TRACK)
    -- 여기서 state는 자기 자신을 가리키며 줄여 쓴 것이다
    local state = this.movement_track_states.walk
    if (context:getShootCoolDown() > 0) then
        -- 방금 발사했으면 idle 애니메이션을 재생해 총몸을 안정시킨다
        if (state.mode ~= 0) then
            state.mode = 0
            context:runAnimation("idle", track, true, LOOP, 0.3)
        end
    elseif (not context:isOnGround()) then
        -- 플레이어가 공중에 있으면 idle 애니메이션을 재생해 총몸을 안정시킨다
        if (state.mode ~= 0) then
            state.mode = 0
            context:runAnimation("idle", track, true, LOOP, 0.6)
        end
    elseif (context:getAimingProgress() > 0.5) then
        -- 조준 중이면 walk_aiming 애니메이션을 재생해야 한다
        if (state.mode ~= 1) then
            state.mode = 1
            context:runAnimation("walk_aiming", track, true, LOOP, 0.3)
        end
    elseif (context:isInputUp()) then
        -- 앞으로 걷는 중이면 walk_forward 애니메이션을 재생해야 한다
        if (state.mode ~= 2) then
            state.mode = 2
            context:runAnimation("walk_forward", track, true, LOOP, 0.4)
            context:anchorWalkDist() -- walkDist 기준점을 찍어 걷기 애니메이션의 시작점을 일정하게 한다
        end
    elseif (context:isInputDown()) then
        -- 뒤로 물러나는 중이면 walk_backward 애니메이션을 재생해야 한다
        if (state.mode ~= 3) then
            state.mode = 3
            context:runAnimation("walk_backward", track, true, LOOP, 0.4)
            context:anchorWalkDist() -- walkDist 기준점을 찍어 걷기 애니메이션의 시작점을 일정하게 한다
        end
    elseif (context:isInputLeft() or context:isInputRight()) then
        -- 옆으로 걷는 중이면 walk_sideway 애니메이션을 재생해야 한다
        if (state.mode ~= 4) then
            state.mode = 4
            context:runAnimation("walk_sideway", track, true, LOOP, 0.4)
            context:anchorWalkDist() -- walkDist 기준점을 찍어 걷기 애니메이션의 시작점을 일정하게 한다
        end
    end
    -- walkDist에 따라 걷기 애니메이션의 진행도를 설정한다
    if (state.mode >= 1 and state.mode <= 4) then
        context:setAnimationProgress(track, (context:getWalkDist() % 2.0) / 2.0, true)
    end
end

-- 걷기 상태에서 빠져나감. 이 부분은 달리기 상태에서 빠져나가는 것과 같다
function movement_track_states.walk.transition(this, context, input)
    -- 대기 신호를 받으면 대기 상태로 간다
    if (input == INPUT_IDLE) then
        return this.movement_track_states.idle
    -- 달리기 신호를 받으면 달리기 상태로 간다
    elseif (input == INPUT_RUN) then
        if (context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))) then
            return this.movement_track_states.run
        end
    end
end
-- 이동 트랙 상태 끝

local slide_states = {
    normal = {},
    slide = {}
}

function slide_states.normal.transition(this, context, input)
    if(context:shouldSlide() and context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)) and not context:isAiming()) then
        return this.slide_states.slide
    end
end

function slide_states.slide.entry(this, context)
    context:runAnimation("slide", context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK), true, PLAY_ONCE_HOLD, 0.5)
end

function slide_states.slide.update(this, context)
    if (context:isStopped(context:getTrack(STATIC_TRACK_LINE, BASE_TRACK)) or context:isHolding(context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK))) then
        context:runAnimation("slide_idle", context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK), true, LOOP, 0.4)
    end
end

function slide_states.slide.transition(this, context, input)
    if(not context:shouldSlide() or not context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)) or context:isAiming()) then
        return this.slide_states.normal
    end
end

function slide_states.slide.exit(this, context)
    context:runAnimation("slide_back", context:getTrack(BLENDING_TRACK_LINE, SLIDE_TRACK), true, PLAY_ONCE_HOLD, 0.2)
end

-- 과열 부분. 이 부분은 505번째 줄에서 끝난다
-- 과열 부분 내용은 노리쇠 후퇴 고정 부분을 그대로 따른다

local over_heat_states = {
    -- normal은 과열되지 않은 일반 상태다
    normal = {},
    -- over_heat는 과열 상태다
    over_heat = {}
}

-- "과열 아님" 상태 진입
function over_heat_states.normal.entry(this, context)
    this.over_heat_states.normal.update(this, context)
end

-- "과열 아님" 상태 갱신
function over_heat_states.normal.update(this, context)
    if (isOverHeat(context)) then
        context:trigger(this.INPUT_OVER_HEAT)
    end
end

-- "과열 아님" 상태에서 빠져나감
function over_heat_states.normal.transition(this, context, input)
    if (input == this.INPUT_OVER_HEAT) then
        return this.over_heat_states.over_heat
    end
end

-- "과열" 상태 진입
function over_heat_states.over_heat.entry(this, context)
    -- 과열될 때 한 번만 재생하는 애니메이션. 예: 많은 연기와 경보음
    context:runAnimation("over_heat", context:getTrack(BLENDING_TRACK_LINE, OVER_HEAT_TRACK), true, PLAY_ONCE_STOP, 0.2)
    -- 과열될 때 계속 반복 실행하는 애니메이션. 예: 천천히 피어오르는 연기
    context:runAnimation("static_over_heating", context:getTrack(BLENDING_TRACK_LINE, OVER_HEATING_TRACK), true, LOOP, 0)
end

-- "과열" 상태 갱신
function over_heat_states.over_heat.update(this, context)
    if (not isOverHeat(context)) then
        context:trigger(this.INPUT_COOLING_HEAT)
    end
end

-- "과열" 상태에서 빠져나감
function over_heat_states.over_heat.transition(this, context, input)
    -- 위의 update 메서드에서 입력을 받으면 "과열 아님" 상태로 간다
    if (input == this.INPUT_COOLING_HEAT) then
        -- "과열 아님" 애니메이션이 따로 없으므로 여기서 과열 애니메이션을 멈춰야 "과열 아님" 상태로 갈 수 있다
        context:stopAnimation(context:getTrack(BLENDING_TRACK_LINE, OVER_HEATING_TRACK))
        return this.over_heat_states.normal
    end
end
-- 과열 부분 끝

local ADS_states = {
    aiming_progress = 0,-- 조준 진행도 기록
    normal = {},-- 조준하지 않는 상태
    aiming = {}-- 조준 상태
}

-- 조준하지 않는 상태 진입
function ADS_states.normal.entry(this, context)
    this.ADS_states.normal.update(this, context)
end

-- 조준하지 않는 상태 갱신
function ADS_states.normal.update(this, context)
    -- 조준 진행도가 늘고 있으면 조준 상태로 간다
    if ((context:getAimingProgress() > this.ADS_states.aiming_progress or context:getAimingProgress() == 1) and context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))) then
        context:trigger(this.INPUT_AIM)
    else
        -- 늘지 않았으면 현재 조준 진행도를 기록한다
        this.ADS_states.aiming_progress = context:getAimingProgress()
    end
end

-- 조준하지 않는 상태에서 빠져나감
function ADS_states.normal.transition(this, context, input)
    -- 위의 update 메서드에서 입력을 받으면 조준 상태로 간다
    if (input == this.INPUT_AIM) then
        return this.ADS_states.aiming
    end
end

-- 조준 상태 진입
function ADS_states.aiming.entry(this, context)
    -- 조준을 시작하면 조준 애니메이션을 재생하고 걸어 둔다
    local track = context:getTrack(STATIC_TRACK_LINE, ADS_TRACK)
    context:runAnimation("aim_start", track, false, PLAY_ONCE_HOLD, 0.2)
    -- 점검 애니메이션 끊기
    context:trigger(this.INPUT_INSPECT_RETREAT)
end

-- 조준 상태 갱신
function ADS_states.aiming.update(this, context)
    local track = context:getTrack(STATIC_TRACK_LINE, ADS_TRACK)
    if (context:isHolding(track)) then
        -- 조준 중 애니메이션을 반복 재생한다
        context:runAnimation("aim", track, false, PLAY_ONCE_HOLD, 0.2)
    end
    -- 조준 진행도가 줄고 있으면 조준하지 않는 상태로 간다. 곧 조준을 푸는 것이다
    if (context:getAimingProgress() < this.ADS_states.aiming_progress or not context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))) then
        context:trigger(this.INPUT_AIM_RETREAT)
    else
        -- 줄지 않았으면 현재 조준 진행도를 기록한다
        this.ADS_states.aiming_progress = context:getAimingProgress()
    end
end

-- 조준 상태에서 빠져나감
function ADS_states.aiming.transition(this, context, input)
    local track = context:getTrack(STATIC_TRACK_LINE, ADS_TRACK)
    if (input == this.INPUT_AIM_RETREAT) then
        -- 조준 종료 애니메이션을 재생하고, 조준 애니메이션이 현재 조준 진행도와 맞도록 애니메이션 진행도를 조정한다
        context:runAnimation("aim_end", track, false, PLAY_ONCE_STOP, 0.2)
        context:setAnimationProgress(track, 1 - context:getAimingProgress(), true)
        return this.ADS_states.normal
    end
end

local M = {
    -- 트랙 줄
    track_line_top = track_line_top,
    STATIC_TRACK_LINE = STATIC_TRACK_LINE,
    GUN_KICK_TRACK_LINE = GUN_KICK_TRACK_LINE,
    BLENDING_TRACK_LINE = BLENDING_TRACK_LINE,
    -- 정적 트랙
    static_track_top = static_track_top,
    BASE_TRACK = BASE_TRACK,
    BOLT_CAUGHT_TRACK = BOLT_CAUGHT_TRACK,
    SAFETY_TRACK = SAFETY_TRACK,
    ADS_TRACK = ADS_TRACK,
    MAIN_TRACK = MAIN_TRACK,
    SPRINT_TRACK = SPRINT_TRACK,
    -- 혼합 트랙
    blending_track_top = blending_track_top,
    MOVEMENT_TRACK = MOVEMENT_TRACK,
    SLIDE_TRACK = SLIDE_TRACK,
    OVER_HEAT_TRACK = OVER_HEAT_TRACK,
    OVER_HEATING_TRACK = OVER_HEATING_TRACK,
    LOOP_TRACK = LOOP_TRACK,
    -- 하위 병렬 트랙
    PRE_PARALLEL_TRACK_1 = PRE_PARALLEL_TRACK_1,
    PRE_PARALLEL_TRACK_2 = PRE_PARALLEL_TRACK_2,
    PRE_PARALLEL_TRACK_3 = PRE_PARALLEL_TRACK_3,
    PRE_PARALLEL_TRACK_4 = PRE_PARALLEL_TRACK_4,
    PRE_PARALLEL_TRACK_5 = PRE_PARALLEL_TRACK_5,
    -- 최상위 병렬 트랙
    PARALLEL_TRACK_1 = PARALLEL_TRACK_1,
    PARALLEL_TRACK_2 = PARALLEL_TRACK_2,
    PARALLEL_TRACK_3 = PARALLEL_TRACK_3,
    PARALLEL_TRACK_4 = PARALLEL_TRACK_4,
    PARALLEL_TRACK_5 = PARALLEL_TRACK_5,
    -- 혼합 트랙
    BLEND_TRACK_1 = BLEND_TRACK_1,
    BLEND_TRACK_2 = BLEND_TRACK_2,
    BLEND_TRACK_3 = BLEND_TRACK_3,
    BLEND_TRACK_4 = BLEND_TRACK_4,
    BLEND_TRACK_5 = BLEND_TRACK_5,
    -- 상태
    base_track_state = base_track_state,
    bolt_caught_states = bolt_caught_states,
    over_heat_states = over_heat_states,
    main_track_states = main_track_states,
    gun_kick_state = gun_kick_state,
    movement_track_states = movement_track_states,
    ADS_states = ADS_states,
    slide_states = slide_states,
    -- 입력
    INPUT_BOLT_CAUGHT = "bolt_caught",
    INPUT_BOLT_NORMAL = "bolt_normal",
    INPUT_OVER_HEAT = "over_heat",
    INPUT_COOLING_HEAT = "cooling_heat",
    INPUT_INSPECT_RETREAT = "inspect_retreat",
    INPUT_INSPECT_BREAK_OUT = "inspect_break_out",
    INPUT_CHARING = "input_charging",
    INPUT_CHARING_EXIT = "input_charging_exit",
    INPUT_AIM = "aim",
    INPUT_AIM_RETREAT = "aim_retreat"
}

-- 상태 기계 초기화 함수. 총을 바꿀 때 호출한다
function M:initialize(context)
    context:ensureTrackLineSize(track_line_top.value)
    context:ensureTracksAmount(STATIC_TRACK_LINE, static_track_top.value)
    context:ensureTracksAmount(BLENDING_TRACK_LINE, blending_track_top.value)
    self.main_track_states.charge.can_charge = true
    self.movement_track_states.run.mode = -1
    self.movement_track_states.walk.mode = -1
end

-- 상태 기계 종료 함수. 총을 집어넣을 때 호출한다
function M:exit(context)
    -- 정리 작업을 한다
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
        self.slide_states.normal
    }
end

return M