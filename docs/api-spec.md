# Celebration API Specification (Draft)

Base URL: `/api/v1`

## 1. Message APIs

### 1) 메시지 생성
- `POST /messages`
- 설명: 비회원 포함 메시지 생성

Request (multipart/form-data)
- `title` (string, required)
- `content` (string, required, max 2000)
- `occasionType` (string, required) - `BIRTHDAY | WEDDING_ANNIVERSARY | VALENTINES_DAY | CUSTOM`
- `occasionName` (string, optional, CUSTOM일 때 사용)
- `templateCode` (string, required) - `TEMPLATE_A | TEMPLATE_B | TEMPLATE_C`
- `expiresAt` (datetime, optional) - 미입력 시 생성 시점 + 7일
- `images` (file[], optional, up to 3)
- `voice` (file, optional, up to 1)

Validation
- 이미지 파일당 최대 3MB, 최대 3장
- 음성 파일 최대 5MB, 최대 1개
- 전체 업로드 총합 최대 14MB

Response 201
```json
{
  "messageId": "msg_01",
  "editToken": "ed_xxx",
  "publicToken": "pub_xxx",
  "expiresAt": "2026-02-23T12:00:00Z"
}
```

### 2) 메시지 수정
- `PUT /messages/{messageId}`
- 설명: 링크 만료 전까지만 수정 가능
- 인증: `X-Edit-Token` 헤더 필요

Request (json)
```json
{
  "title": "string",
  "content": "string",
  "templateCode": "TEMPLATE_A",
  "expiresAt": "2026-02-23T12:00:00Z"
}
```

Rules
- 현재 시각이 만료 시각 이후면 `403 MESSAGE_EXPIRED`
- 만료 시간을 연장할 수 있는지 여부는 정책으로 분리(현재는 허용)

Response 200
```json
{
  "messageId": "msg_01",
  "updatedAt": "2026-02-17T10:00:00Z"
}
```

### 3) 공개 메시지 조회
- `GET /messages/public/{publicToken}`
- 설명: 수신자가 링크로 열람

Response 200
```json
{
  "title": "생일 축하해",
  "content": "항상 고마워",
  "templateCode": "TEMPLATE_B",
  "occasion": {
    "type": "BIRTHDAY",
    "name": "생일"
  },
  "media": {
    "images": [
      { "url": "https://.../img1.jpg", "sortOrder": 1 }
    ],
    "voice": { "url": "https://.../voice.mp3" }
  },
  "expiresAt": "2026-02-23T12:00:00Z"
}
```

Error
- 만료 링크: `410 LINK_EXPIRED`

## 2. Media APIs

### 1) 이미지 추가 업로드
- `POST /messages/{messageId}/images`
- 인증: `X-Edit-Token`
- 제한: 최대 3장

### 2) 음성 업로드/교체
- `POST /messages/{messageId}/voice`
- 인증: `X-Edit-Token`
- 제한: 1개만 유지

### 3) 미디어 삭제
- `DELETE /messages/{messageId}/media/{mediaId}`
- 인증: `X-Edit-Token`

## 3. 공통 에러 포맷

```json
{
  "code": "VALIDATION_ERROR",
  "message": "voice file size exceeds 5MB",
  "traceId": "trace-123"
}
```

주요 에러 코드
- `VALIDATION_ERROR`
- `UNAUTHORIZED_EDIT_TOKEN`
- `MESSAGE_EXPIRED`
- `LINK_EXPIRED`
- `NOT_FOUND`
- `INTERNAL_SERVER_ERROR`
