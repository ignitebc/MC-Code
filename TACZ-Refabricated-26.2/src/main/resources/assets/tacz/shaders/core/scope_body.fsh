#version 330

// 조준경 몸체 조각 셰이더 — 바닐라 core/entity.fsh에 한 가지만 더한다:
// 접안렌즈에 덮인 픽셀은 discard한다.
//
// 이는 원본 1.21.1의 다음 stencil 문장과 같은 역할이다:
//     scope_body: stencilFunc(GL_EQUAL, 0)   // 접안렌즈가 [덮지 않은] 곳에만 조준경 몸체를 그린다
// 26.2에는 스텐실 버퍼가 없어서 화면 밖 마스크 텍스처(ScopeMaskSampler)를 샘플링해 같은 이분 판정을 한다.
//
// entity.fsh를 "상속"할 방법을 찾지 않고 통째로 베낀 이유:
// GLSL에는 상속이 없고 바닐라도 끼워 넣을 수 있는 조각 훅을 주지 않는다. entity의
// 렌더링 의미에 discard 한 단계를 더하려면 복사해서 고치는 수밖에 없다. 아래 SCOPE_MASK 부분을 빼면
// 이 파일은 26.2의 assets/minecraft/shaders/core/entity.fsh와 줄마다 같다 —
// 나중에 바닐라가 entity.fsh를 바꾸면 여기도 함께 맞춰야 한다.

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

#ifdef DISSOLVE
uniform sampler2D DissolveMaskSampler;
#endif

#ifdef SCOPE_MASK
// 접안렌즈 마스크: 흰색 = 이 픽셀이 조준경 안(접안렌즈 투영이 덮음), 검정 = 조준경 밖.
// ScopeMaskRenderer가 단계 경계에서 화면 밖 target에 그린다.
uniform sampler2D ScopeMaskSampler;
#endif

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
#ifdef PER_FACE_LIGHTING
in vec4 vertexPerFaceColorBack;
in vec4 vertexPerFaceColorFront;
#else
in vec4 vertexColor;
#endif

#ifndef EMISSIVE
in vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
in vec4 overlayColor;
#endif

in vec2 texCoord0;

out vec4 fragColor;

void main() {
#ifdef SCOPE_MASK
    // texCoord0이 아니라 gl_FragCoord를 쓴다: 알고 싶은 것은 "화면의 이 위치"가
    // 접안렌즈에 덮였는지이며, 조준경 몸체 자신의 텍스처 UV와는 관계없다.
    //
    // gl_FragCoord.xy는 [왼쪽 아래]가 원점인 창 픽셀 좌표이고, 마스크 target의
    // 텍스처 원점도 왼쪽 아래라 둘이 같으므로 여기서는 Y를 뒤집을 [필요가 없다].
    // (디버그 미리보기에서 V를 뒤집은 것은 GUI 좌표계 원점이 왼쪽 위이기 때문이다 — 서로 다른 일이니 헷갈리지 않는다.)
    vec2 maskUv = gl_FragCoord.xy / ScreenSize;
    vec2 maskSample = texture(ScopeMaskSampler, maskUv).rg;
    bool insideOcular = maskSample.r > 0.5;

    // [조준 점진 효과] 초록 채널에는 조준 진행도가 들어 있다(ScopeMaskRenderer가 ColorModulator.g에 씀).
    //
    // 원본 방식은 원 중심을 접안렌즈 투영 중심에 고정하고 반지름만 진행도에 따라 키운다 —
    //     centerX/centerY = getBedrockPartCenter(...)  // 고정
    //     rad = 80 * modifier * aimingProgress         // 반지름만 바뀐다
    // 핵심은 이것이 [순수 2차원] 작업이라는 점이다: 위치는 그대로고 덮는 범위만 바뀐다.
    //
    // 예전 구현은 진행도에 따라 3D 접안렌즈 형상의 크기를 바꿨는데, 원근 투영에서는 투영 [위치]도 함께 바뀌어
    // 조준경 안 영역이 화면 밖에서 "날아 들어오는" 것처럼 보였다 — 사용자가 실측한 두 번째 문제다.
    //
    // 여기서는 같은 효과의 2차원 작업으로 바꿨다: 마스크 가장자리를 따라 안쪽으로 줄인다. progress가 작으면
    // 깊은 곳(가장자리에서 먼) 픽셀만 남기고, progress=1이면 모두 남긴다. 위치는 늘 그대로다.
    if (insideOcular) {
        float progress = maskSample.g;
        if (progress < 0.999) {
            // 마스크 자체를 거리장으로 쓴다: 주변 몇 겹을 샘플링해 마스크 안에 든 것이 몇 개인지 센다.
            // 모두 안쪽 -> depth≈1(중심 깊은 곳), 가장자리에 붙음 -> depth≈0.
            // 그러면 원 중심이 어디인지 몰라도 어떤 모양의 접안렌즈 투영에도 통한다
            // (우리 마스크는 정원이 아니라 다각형 투영이다).
            const int RINGS = 3;
            const int STEPS = 8;
            float inside = 0.0;
            float total = 0.0;
            // 줄어드는 띠의 너비(UV 단위). 0.055는 화면 높이의 약 5.5%이며,
            // 기본 총기 팩의 가장 큰 접안렌즈 투영에서도 중심까지 덮기에 충분하다.
            float unit = 0.055;
            for (int r = 1; r <= RINGS; r++) {
                float radius = unit * float(r) / float(RINGS);
                for (int i = 0; i < STEPS; i++) {
                    float a = 6.2831853 * float(i) / float(STEPS);
                    vec2 off = vec2(cos(a), sin(a)) * radius;
                    // 가로세로 비율 보정: UV 공간에서 같은 수치라도 x/y에서 해당하는 픽셀 수가 다르다
                    off.x *= ScreenSize.y / max(ScreenSize.x, 1.0);
                    total += 1.0;
                    inside += texture(ScopeMaskSampler, maskUv + off).r > 0.5 ? 1.0 : 0.0;
                }
            }
            float depth = total > 0.0 ? inside / total : 1.0;
            // depth < 1-progress인 픽셀(가장자리 가까운)은 잠시 "조준경 안"으로 치지 않는다
            if (depth < 1.0 - progress) {
                insideOcular = false;
            }
        }
    }
  #ifdef SCOPE_MASK_INVERT
    // [반대] 조준경 안만 남긴다 — 조준선(눈금)에 쓴다.
    // 원본은 조준선에 stencilFunc(GL_EQUAL, i+1), 곧 "i번째 접안렌즈의
    // 투영 영역 안에만 그리기"를 썼다(renderDivisionOnly / renderOcularAndDivision 모두 그렇다).
    // 이 단계가 빠지면 조준선이 경통 밖으로 넘쳐 조준경 테에 묶이지 않고 화면에 붙는다.
    if (!insideOcular) {
        discard;
    }
  #else
    // 접안렌즈 투영 안에 들어옴 — 여기는 "조준경 안"이라 조준경 몸체가 나오면 안 되고,
    // 뒤의 월드 화면이 비쳐 보이게 한다. 원본의 stencilFunc(GL_EQUAL, 0)과 같다.
    if (insideOcular) {
        discard;
    }
  #endif
#endif

    vec4 color = texture(Sampler0, texCoord0);
#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
#endif

#ifdef PER_FACE_LIGHTING
    vec4 faceVertexColor = gl_FrontFacing ? vertexPerFaceColorFront : vertexPerFaceColorBack;
#else
    vec4 faceVertexColor = vertexColor;
#endif

#ifdef DISSOLVE
    if (faceVertexColor.a < texture(DissolveMaskSampler, texCoord0).a) {
        discard;
    }
    // 디졸브 효과가 반투명을 완전히 대신한다
    faceVertexColor.a = 1.0;
#endif

    color *= faceVertexColor * ColorModulator;
#ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
#endif
#ifndef EMISSIVE
    color *= lightMapColor;
#endif

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
