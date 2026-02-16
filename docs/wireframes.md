# Celebration Template Wireframes (Draft)

## 공통 화면 구성

### A. 작성 화면
1. 상단: 기념일 선택 (생일/결혼기념일/발렌타인/직접입력)
2. 제목 + 본문 입력
3. 이미지 업로드(최대 3장)
4. 음성 업로드(최대 1개)
5. 템플릿 선택 (3종)
6. 만료일(기본 7일) 안내
7. 생성 버튼

### B. 열람 화면
1. 템플릿 레이아웃 렌더링
2. 본문 텍스트
3. 이미지 갤러리
4. 음성 플레이어
5. 만료 안내(만료 시 만료 화면)

---

## Template 1: `TEMPLATE_A` (Classic Card)
- 톤: 깔끔/정갈
- 레이아웃
  - 상단 카드 헤더(기념일명)
  - 중앙 텍스트 카드
  - 하단 이미지 썸네일 스트립
  - 하단 고정 음성 플레이어
- 사용 시나리오: 가족/지인에게 무난한 메시지

## Template 2: `TEMPLATE_B` (Photo Focus)
- 톤: 감성/사진 중심
- 레이아웃
  - 풀폭 이미지 슬라이더
  - 슬라이더 하단 반투명 본문 오버레이
  - 하단 음성 재생 버튼
- 사용 시나리오: 커플, 여행 추억 공유

## Template 3: `TEMPLATE_C` (Minimal Letter)
- 톤: 편지형/텍스트 중심
- 레이아웃
  - 봉투 오프닝 애니메이션(선택)
  - 편지지 형태 본문
  - 본문 하단 첨부 이미지/음성 아이콘
- 사용 시나리오: 긴 문장, 진심 전달

---

## 컴포넌트 트리 제안 (Vue)
- `CreateMessagePage`
  - `OccasionSelector`
  - `MessageEditor`
  - `ImageUploader`
  - `VoiceUploader`
  - `TemplateSelector`
  - `ExpirationNotice`
  - `GenerateLinkButton`

- `PublicMessagePage`
  - `TemplateRenderer`
    - `TemplateAView | TemplateBView | TemplateCView`
  - `ImageGallery`
  - `VoicePlayer`
  - `ExpiredState`
