package com.tacz.guns.api.client.animation.statemachine;

import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.DiscreteTrackArray;
import com.tacz.guns.api.client.animation.ObjectAnimation;
import com.tacz.guns.api.client.animation.ObjectAnimationRunner;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class AnimationStateContext {
    private boolean shouldHideCrossHair = false;
    private @Nullable AnimationStateMachine<?> stateMachine;
    private final DiscreteTrackArray trackArray = new DiscreteTrackArray();

    /**
     * 상태 기계 스크립트에서 호출하지 않는다.
     *
     * @return 문맥에 묶인 상태 기계
     */
    public @Nullable AnimationStateMachine<?> getStateMachine() {
        return stateMachine;
    }

    /**
     * 상태 기계 스크립트에서 호출하지 않는다.
     *
     * @return 문맥의 개별 트랙 배열
     */
    public DiscreteTrackArray getTrackArray() {
        return trackArray;
    }

    /**
     * 새 트랙 줄을 할당하고 그 인덱스를 돌려준다.
     *
     * @return 새 트랙 줄 인덱스
     * @throws TrackArrayMismatchException 상태 기계의 track array가 이 context가 지정한 인스턴스가 아닐 때
     */
    public int addTrackLine() {
        checkTrackArray();
        return getTrackArray().addTrackLine();
    }

    /**
     * 트랙 줄 수를 확보한다
     *
     * @param size 확보할 트랙 줄 수
     * @throws TrackArrayMismatchException 상태 기계의 track array가 이 context가 지정한 인스턴스가 아닐 때
     */
    public void ensureTrackLineSize(int size) {
        checkTrackArray();
        getTrackArray().ensureCapacity(size);
    }

    /**
     * 트랙 줄 수를 얻는다
     *
     * @return 트랙 줄 수
     * @throws TrackArrayMismatchException 상태 기계의 track array가 이 context가 지정한 인스턴스가 아닐 때
     */
    public int getTrackLineSize() {
        checkTrackArray();
        return getTrackArray().getTrackLineSize();
    }

    /**
     * 지정한 트랙 줄에 새 트랙을 할당하고 그 인덱스를 돌려준다
     *
     * @param index 트랙 줄 인덱스
     * @return 새 트랙 인덱스
     * @throws TrackArrayMismatchException 상태 기계의 track array가 이 context가 지정한 인스턴스가 아닐 때
     */
    public int assignNewTrack(int index) {
        checkTrackArray();
        return getTrackArray().assignNewTrack(index);
    }

    /**
     * 트랙 줄에서 비어 있는 트랙을 먼저 돌려주고, 없으면 새 트랙을 연다
     *
     * @param index            트랙 줄 인덱스
     * @param interruptHolding holding 상태의 트랙도 빈 트랙으로 볼지
     * @return 컨트롤러 안의 트랙 포인터
     * @throws TrackArrayMismatchException 상태 기계의 track array가 이 context가 지정한 인스턴스가 아닐 때
     * @see AnimationStateContext#assignNewTrack(int)
     */
    public int findIdleTrack(int index, boolean interruptHolding) {
        var stateMachine = checkStateMachine();
        checkTrackArray();
        DiscreteTrackArray trackArray = getTrackArray();
        List<Integer> trackList = trackArray.getByIndex(index);
        AnimationController controller = stateMachine.getAnimationController();
        for (int track : trackList) {
            ObjectAnimationRunner animation = controller.getAnimation(track);
            if (animation == null || animation.isStopped() || (interruptHolding && animation.isHolding())) {
                return track;
            }
        }
        return trackArray.assignNewTrack(index);
    }

    /**
     * 지정한 트랙 줄에 트랙이 충분히 있도록 보장한다
     *
     * @param index  트랙 줄 인덱스
     * @param amount 필요한 트랙 수
     */
    public void ensureTracksAmount(int index, int amount) {
        checkTrackArray();
        getTrackArray().ensureTrackAmount(index, amount);
    }

    /**
     * 트랙 포인터를 얻는다
     *
     * @param trackLineIndex 트랙 줄 인덱스
     * @param trackIndex     트랙 인덱스
     * @return 컨트롤러 안의 트랙 포인터. 트랙이 없으면 -1
     */
    public int getTrack(int trackLineIndex, int trackIndex) {
        checkTrackArray();
        DiscreteTrackArray trackArray = getTrackArray();
        if (trackLineIndex >= trackArray.getTrackLineSize()) {
            return -1;
        }
        List<Integer> tracks = trackArray.getByIndex(trackLineIndex);
        if (trackIndex >= tracks.size()) {
            return -1;
        }
        return tracks.get(trackIndex);
    }

    /**
     * 트랙이 하나만 필요한 트랙 줄에 쓴다. 대상 트랙 줄에 트랙이 없으면 하나를 할당하고,
     * 이미 여러 개 있으면 남는 트랙은 버리지 않고 첫 번째 트랙을 돌려준다.
     *
     * @param index 트랙 줄 인덱스
     * @return 트랙 인덱스
     * @throws TrackArrayMismatchException 상태 기계의 track array가 이 context가 지정한 인스턴스가 아닐 때
     */
    public int getAsSingletonTrack(int index) {
        checkTrackArray();
        DiscreteTrackArray trackArray = getTrackArray();
        List<Integer> trackList = trackArray.getByIndex(index);
        if (trackList.isEmpty()) {
            return trackArray.assignNewTrack(index);
        } else {
            return trackList.get(0);
        }
    }

    /**
     * 지정한 트랙에서 애니메이션을 실행한다. 트랙에 이미 실행 중인 애니메이션이 있으면 중단하고, 입력한 전환 시간에 따라 전환한다.
     * 새 애니메이션은 재생 즉시 실행되며 전환 때문에 멈추지 않는다. 예전 애니메이션은 재생이 시작되는 순간 멈춘다.
     *
     * @param name           애니메이션 이름
     * @param track          컨트롤러 안의 트랙 포인터
     * @param blending       애니메이션을 아래로 섞을지
     * @param playType       애니메이션 재생 상태(열거형의 ordinal 값)
     * @param transitionTime 전환 시간
     * @see AnimationConstant
     */
    public void runAnimation(String name, int track, boolean blending, int playType, float transitionTime) {
        var stateMachine = checkStateMachine();
        ObjectAnimation.PlayType pt = ObjectAnimation.PlayType.values()[playType];
        stateMachine.getAnimationController().runAnimation(track, name, pt, transitionTime);
        stateMachine.getAnimationController().setBlending(track, blending);
    }

    /**
     * 애니메이션을 멈춘다. 멈춘 애니메이션의 키프레임은 더 이상 모델에 영향을 주지 않는다.
     *
     * @param track 컨트롤러 안의 트랙 포인터
     */
    public void stopAnimation(int track) {
        var stateMachine = checkStateMachine();
        ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
        if (runner != null) {
            runner.stop();
        }
    }

    /**
     * 애니메이션 진행을 끝까지 옮기고 걸어 둔다. 걸어 둔 애니메이션은 마지막 프레임에 고정된다.
     *
     * @param track 컨트롤러 안의 트랙 포인터
     */
    public void holdAnimation(int track) {
        var stateMachine = checkStateMachine();
        ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
        if (runner != null) {
            runner.hold();
        }
    }

    /**
     * 애니메이션을 일시 정지한다. 애니메이션은 고정되지만 키프레임은 계속 모델에 영향을 준다.
     *
     * @param track 컨트롤러 안의 트랙 포인터
     */
    public void pauseAnimation(int track) {
        var stateMachine = checkStateMachine();
        ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
        if (runner != null) {
            runner.pause();
        }
    }

    /**
     * 애니메이션 실행을 재개한다. 이미 실행 중이면 아무 일도 일어나지 않는다
     *
     * @param track 컨트롤러 안의 트랙 포인터
     */
    public void resumeAnimation(int track) {
        var stateMachine = checkStateMachine();
        ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
        if (runner != null) {
            runner.run();
        }
    }

    /**
     * 애니메이션 재생의 절대 진행도를 설정한다.
     * 정규화(normalization을 true로)하면 progress는 0~1이며 0은 시작, 1은 끝이다.
     * 그렇지 않으면 progress는 시간(초)이다
     *
     * @param track         컨트롤러 안의 트랙 포인터
     * @param progress      절대 진행도. normalization이 true면 0~1(0은 시작, 1은 끝), 아니면 시간(초)
     * @param normalization 정규화 여부
     */
    public void setAnimationProgress(int track, float progress, boolean normalization) {
        var stateMachine = checkStateMachine();
        ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
        if (runner != null) {
            if (runner.isRunning() || runner.isPausing()) {
                if (normalization) {
                    progress = runner.getAnimation().getMaxEndTimeS() * progress;
                }
                runner.setProgressNs((long) (progress * 1e9));
                return;
            }
            ObjectAnimationRunner runner1 = runner.getTransitionTo();
            if (runner1 != null) {
                if (normalization) {
                    progress = runner1.getAnimation().getMaxEndTimeS() * progress;
                }
                runner1.setProgressNs((long) (progress * 1e9));
            }
        }
    }

    /**
     * 현재 진행도에서 일정량을 옮긴다. 예: 10초 앞으로, 10초 뒤로.
     * 정규화(normalization을 true로)하면 progress는 -1~1이며 -1은 전체 길이만큼 뒤로, 1은 전체 길이만큼 앞으로다.
     * 그렇지 않으면 progress는 시간(초)이다
     *
     * @param track         컨트롤러 안의 트랙 포인터
     * @param progress      상대 진행도(음수 가능). normalization이 true면 -1~1(-1은 전체 길이만큼 뒤로, 1은 앞으로).
     *                      아니면 시간(초).
     * @param normalization 정규화 여부
     */
    public void adjustAnimationProgress(int track, float progress, boolean normalization) {
        var stateMachine = checkStateMachine();
        ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
        if (runner != null) {
            if (runner.isRunning()) {
                if (normalization) {
                    progress = runner.getAnimation().getMaxEndTimeS() * progress;
                }
                runner.setProgressNs(runner.getProgressNs() + (long) (progress * 1e9));
                return;
            }
            ObjectAnimationRunner runner1 = runner.getTransitionTo();
            if (runner1 != null) {
                if (normalization) {
                    progress = runner1.getAnimation().getMaxEndTimeS() * progress;
                }
                runner1.setProgressNs(runner1.getProgressNs() + (long) (progress * 1e9));
            }
        }
    }

    /**
     * 지정한 트랙이 걸려 있는지 얻는다
     *
     * @return 트랙 애니메이션이 걸려 있는지. 트랙이 비어 있으면 false다. 애니메이션이 없는 트랙은 걸림이 아니라 정지로 보기 때문이다.
     */
    public boolean isHolding(int track) {
        var stateMachine = checkStateMachine();
        ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
        if (runner != null) {
            return (runner.getTransitionTo() != null ? runner.getTransitionTo().isHolding() : runner.isHolding());
        } else {
            return false;
        }
    }

    /**
     * 지정한 트랙이 정지했는지 얻는다
     *
     * @return 트랙 애니메이션이 정지했는지. 트랙이 비어 있으면 true다. 애니메이션이 없는 트랙은 정지로 보기 때문이다.
     */
    public boolean isStopped(int track) {
        var stateMachine = checkStateMachine();
        ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
        if (runner != null) {
            return (runner.getTransitionTo() != null ? runner.getTransitionTo().isStopped() : runner.isStopped());
        } else {
            return true;
        }
    }

    /**
     * 지정한 트랙이 일시 정지했는지 얻는다
     *
     * @return 트랙 애니메이션이 일시 정지했는지. 트랙이 비어 있으면 false다. 애니메이션이 없는 트랙은 일시 정지가 아니라 정지로 보기 때문이다.
     */
    public boolean isPause(int track) {
        var stateMachine = checkStateMachine();
        ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
        if (runner != null) {
            return (runner.getTransitionTo() != null ? !runner.getTransitionTo().isPausing() : !runner.isPausing());
        } else {
            return false;
        }
    }

    /**
     * 애니메이션 파일에 해당 애니메이션이 있는지 얻는다
     *
     * @param name 애니메이션 이름
     * @return 애니메이션이 있는지
     */
    public boolean hasAnimationPrototype(String name) {
        var stateMachine = checkStateMachine();
        AnimationController animationController = stateMachine.getAnimationController();
        return animationController.containPrototype(name);
    }

    /**
     * 상태 전이를 직접 한 번 일으킨다
     *
     * @param input 상태 전이 입력
     */
    public void trigger(String input) {
        var stateMachine = checkStateMachine();
        stateMachine.trigger(input);
    }

    /**
     * 애니메이션에 시점이 크게 움직이는 구간이 있으면 어지럼을 줄이려고 조준선을 숨겨야 할 수 있다.
     *
     * @return 렌더링할 때 조준선을 숨겨야 하는지
     */
    public boolean shouldHideCrossHair() {
        return shouldHideCrossHair;
    }

    /**
     * 애니메이션에 시점이 크게 움직이는 구간이 있으면 어지럼을 줄이려고 조준선을 숨겨야 할 수 있다.
     *
     * @param shouldHideCrossHair 렌더링할 때 조준선을 숨겨야 하는지
     */
    public void setShouldHideCrossHair(boolean shouldHideCrossHair) {
        this.shouldHideCrossHair = shouldHideCrossHair;
    }

    void setStateMachine(@Nullable AnimationStateMachine<?> stateMachine) {
        if (this.stateMachine != null) {
            this.stateMachine.getAnimationController().setUpdatingTrackArray(null);
        }
        if (stateMachine != null) {
            stateMachine.getAnimationController().setUpdatingTrackArray(trackArray);
        }
        this.stateMachine = stateMachine;
    }

    private void checkTrackArray() {
        if (stateMachine != null && stateMachine.getAnimationController().getUpdatingTrackArray() != trackArray) {
            throw new TrackArrayMismatchException();
        }
    }

    @Nonnull
    private AnimationStateMachine<?> checkStateMachine() {
        if (this.stateMachine == null) {
            throw new IllegalStateException("This context has not been bound to a state machine.");
        }
        return this.stateMachine;
    }
}
