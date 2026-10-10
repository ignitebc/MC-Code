package cn.sh1rocu.tacz.api.extension;

/**
 * 26.2에서 제거된 {@code walkDistO}(직전 틱의 걷기 거리)를 엔티티에 되살린다.
 *
 * <p><b>필요한 이유</b></p>
 *
 * <p>원본 1.21.1의 총 들고 걷는 애니메이션은 <b>보간한</b> 걷기 거리로 움직인다:</p>
 * <pre>
 * entity.walkDist + (entity.walkDist - entity.walkDistO) * partialTicks
 * </pre>
 *
 * <p>26.2는 {@code walkDist}를 {@code moveDist}로 이름을 바꾸면서 짝이 되는
 * {@code walkDistO}를 <b>남기지 않았다</b>(javap 확인: {@code Entity}에는 {@code public float moveDist}만 있다).
 * 6차에서 "애니메이션이 6.7배 빠른" 문제를 고치려고 {@code moveDist}로 바꿔 단위는 맞췄지만,
 * 값이 게임 틱(20Hz)마다 한 번만 바뀌고 보간도 할 수 없다 — 렌더링은 프레임(60~144Hz)마다 돌기 때문에
 * 애니메이션이 계단처럼 끊겨 <b>프레임이 떨어진 것처럼</b> 보였다.</p>
 *
 * <p>이 인터페이스는 {@code EntityMixin}이 구현한다. 매 {@code Entity#tick()}의 HEAD에서
 * 직전 틱의 {@code moveDist}를 저장해 원본과 같은
 * {@code walkDistO}를 다시 만들고, {@code GunAnimationStateContext#getWalkDist()}가
 * 1.21.1과 똑같이 선형 보간할 수 있게 한다.</p>
 */
public interface IMoveDistTracker {
    /**
     * @return 직전 게임 틱이 끝났을 때의 {@code moveDist}. 처음 호출하면 현재 값과 같다(증가량 0).
     */
    float tacz$getMoveDistO();
}
