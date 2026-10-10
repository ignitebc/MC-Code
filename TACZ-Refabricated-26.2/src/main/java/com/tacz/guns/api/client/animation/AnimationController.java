package com.tacz.guns.api.client.animation;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Supplier;

public class AnimationController {
    protected final ArrayList<ObjectAnimationRunner> currentRunners = new ArrayList<>();
    protected final ArrayList<Boolean> blending = new ArrayList<>();
    private final AnimationListenerSupplier listenerSupplier;
    private final ArrayList<Queue<AnimationPlan>> animationQueue = new ArrayList<>();
    protected Map<String, ObjectAnimation> prototypes = Maps.newHashMap();
    protected @Nullable Iterable<Integer> updatingTrackArray = null;

    public AnimationController(List<ObjectAnimation> animationPrototypes, AnimationListenerSupplier model) {
        for (ObjectAnimation prototype : animationPrototypes) {
            if (prototype == null) {
                continue;
            }
            prototypes.put(prototype.name, prototype);
        }
        this.listenerSupplier = model;
    }

    public void providePrototypeIfAbsent(String name, Supplier<ObjectAnimation> supplier) {
        if (!prototypes.containsKey(name)) {
            prototypes.put(name, supplier.get());
        }
    }

    public boolean containPrototype(String name) {
        return prototypes.containsKey(name);
    }

    @Nullable
    public ObjectAnimationRunner getAnimation(int track) {
        if (track >= currentRunners.size()) {
            return null;
        }
        return currentRunners.get(track);
    }

    public void removeAnimation(int track) {
        if (track < currentRunners.size()) {
            currentRunners.set(track, null);
        }
        if (track < animationQueue.size()) {
            animationQueue.set(track, null);
        }
    }

    public void queueAnimation(int track, Queue<AnimationPlan> queue) {
        // 배열 길이가 맞는지 확인한다
        for (int i = animationQueue.size(); i <= track; i++) {
            animationQueue.add(null);
        }
        animationQueue.set(track, queue);
        if (queue != null) {
            AnimationPlan plan = null;
            while (plan == null && !queue.isEmpty()) {
                plan = queue.poll();
            }
            if (plan != null) {
                run(track, plan.animationName, plan.playType, plan.transitionTimeS);
            }
        }
    }

    public void runAnimation(int track, String animationName, ObjectAnimation.PlayType playType, float transitionTimeS) {
        // 애니메이션 하나를 실행할 때도 애니메이션이 하나뿐인 대기열로 보므로, 예전 대기열을 비운다.
        if (track < animationQueue.size()) {
            animationQueue.set(track, null);
        }
        run(track, animationName, playType, transitionTimeS);
    }

    synchronized private void run(int track, String animationName, ObjectAnimation.PlayType playType, float transitionTimeS) {
        ObjectAnimation prototype = prototypes.get(animationName);
        if (prototype == null) {
            return;
        }
        // 배열 길이가 맞는지 확인한다
        for (int i = currentRunners.size(); i <= track; i++) {
            currentRunners.add(null);
        }

        ObjectAnimation animation = new ObjectAnimation(prototype);
        animation.applyAnimationListeners(listenerSupplier);
        animation.playType = playType;
        ObjectAnimationRunner runner = new ObjectAnimationRunner(animation);
        runner.setProgressNs(0);
        runner.run();

        ObjectAnimationRunner oldRunner = currentRunners.get(track);
        if (transitionTimeS > 0) {
            if (oldRunner != null) {
                oldRunner.transition(runner, (long) (transitionTimeS * 1e9));
            } else {
                currentRunners.set(track, runner);
            }
        } else {
            currentRunners.set(track, runner);
        }
    }

    public void setBlending(int track, boolean blend) {
        // 배열 길이가 맞는지 확인한다
        for (int i = blending.size(); i <= track; i++) {
            blending.add(false);
        }
        blending.set(track, blend);
    }

    public @Nullable Iterable<Integer> getUpdatingTrackArray() {
        return updatingTrackArray;
    }

    public void setUpdatingTrackArray(@Nullable Iterable<Integer> updatingTrackArray) {
        this.updatingTrackArray = updatingTrackArray;
    }

    synchronized public void update() {
        // updatingTrackArray가 있으면 그 순서대로, 없으면 낮은 번호부터 갱신한다.
        if (updatingTrackArray != null) {
            updatingTrackArray.forEach(track -> this.updateByTrack(track, false));
        } else {
            for (int i = 0; i < currentRunners.size(); i++) {
                updateByTrack(i, false);
            }
        }
    }

    synchronized public void updateSoundOnly() {
        for (int i = 0; i < currentRunners.size(); i++) {
            updateByTrack(i, true);
        }
    }

    private void updateByTrack(int track, boolean isSoundOnly) {
        if (track >= currentRunners.size()) {
            return;
        }
        boolean blend = track < blending.size() ? blending.get(track) : false;
        ObjectAnimationRunner runner = currentRunners.get(track);
        if (runner == null) {
            return;
        }
        //현재 애니메이션 runner 갱신
        if (runner.isRunning() || runner.isHolding() || runner.isPausing() || runner.isTransitioning()) {
            if (isSoundOnly) {
                runner.updateSoundOnly();
            } else {
                runner.update(blend);
            }
        }
        //전환 목표 애니메이션 runner를 갱신하고, 전환이 끝났으면 currentRunners에 넣는다
        if (runner.getTransitionTo() != null) {
            if (isSoundOnly) {
                runner.getTransitionTo().updateSoundOnly();
            } else {
                runner.getTransitionTo().update(blend);
            }
            if (!runner.isTransitioning()) {
                currentRunners.set(track, runner.getTransitionTo());
                runner = runner.getTransitionTo();
            }
        }
        // 애니메이션이 끝나면 대기열에 다음 애니메이션이 있는지 보고, 있으면 재생한다
        if ((runner.isHolding() || runner.isStopped()) && !runner.isTransitioning()) {
            if (track < animationQueue.size()) {
                Queue<AnimationPlan> queue = animationQueue.get(track);
                if (queue != null) {
                    AnimationPlan plan = null;
                    while (plan == null && !queue.isEmpty()) {
                        plan = queue.poll();
                    }
                    if (plan != null) {
                        run(track, plan.animationName, plan.playType, plan.transitionTimeS);
                    }
                }
            }
        }
    }
}
