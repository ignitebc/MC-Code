# 복셀 펫 (꼬미 · 슈퍼꼬미 · 암흑드래곤 · 스컬큰 레이븐)

네 펫은 블록벤치 파일이 아니라 파이썬으로 **복셀을 조각**해서 만든다.
스크립트 하나가 모델 메시(Java), 텍스처(PNG), 미리보기를 함께 만들어 내므로
모양과 UV 가 어긋날 일이 없다.

## 다시 만들기

```
pip install pillow numpy
python design/pets/build_pets.py              # 네 종 전부
python design/pets/build_pets.py gomi_pet     # 하나만
```

| 결과물 | 위치 |
| --- | --- |
| 텍스처 | `Common/src/main/resources/assets/advancednetherite/textures/entity/<이름>.png` |
| 메시 | `Fabric/src/main/java/.../client/model/mesh/<이름>Mesh.java` (직접 고치지 않는다) |
| 미리보기 | 바탕화면 `pet_preview/<이름>.png` (게임을 켜지 않고 여섯 방향에서 확인, 저장소 밖) |

## 파일

- `petkit.py` : 공용 도구. 복셀 덩어리를 큰 박스들로 분해하고, 박스 UV 아틀라스에
  복셀 색·베벨·틈새 그림자를 구운 뒤 Java 메시와 미리보기를 출력한다.
- `dogs.py` : 꼬미, 슈퍼꼬미. 복셀 한 칸 = 0.5 유닛. 참고 그림(`gomi.png`, `super_gomi.png`)은
  저장소에 두지 않는다.
- `legendaries.py` : 암흑드래곤, 스컬큰 레이븐. 복셀 한 칸 = 1 유닛 (렌더러에서 0.3 배 안팎으로 축소).

## 고칠 때 지킬 것

- 파트 이름(`head`, `front_left_leg`, `cape_top` …)은 `*PetModel.java` 의 `getChild` 와 짝이다.
  이름을 바꾸거나 파트를 없애면 모델 클래스도 같이 고친다.
- 애니메이션은 메시의 기본 포즈에 `+=` 로 더한다. 망토·날개·목의 기본 각도는
  파이썬 쪽 상수(`CAPE_*_ANGLE`, `WING_*_ANGLE`, `NECK_ANGLE`)에서 정한다.
- 발바닥이 지면에 닿으려면 다리 맨 아래 복셀이 모델 좌표 y=24 에 와야 한다.
- 회전이 없는 자식 파트의 피벗은 복셀 격자에서 0.05~0.1 유닛 어긋나게 둔다.
  격자에 딱 맞추면 부모와 겹치는 면이 같은 평면에 놓여 깜빡인다.
- 최종 크기는 모델이 아니라 각 Renderer 의 `scale` 에서만 조정한다.
