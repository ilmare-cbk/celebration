# Celebration DB ERD (Draft)

## 엔티티

### 1) message
- `id` (PK, bigint)
- `public_token` (varchar, unique, not null)
- `edit_token` (varchar, unique, not null)
- `title` (varchar(120), not null)
- `content` (text, not null)
- `occasion_type` (varchar(30), not null)
- `occasion_name` (varchar(80), null)
- `template_code` (varchar(30), not null)
- `status` (varchar(20), not null, default='ACTIVE')
- `expires_at` (timestamp, not null)  // default now + 7 days
- `created_at` (timestamp, not null)
- `updated_at` (timestamp, not null)

Index
- `uk_message_public_token (public_token)`
- `uk_message_edit_token (edit_token)`
- `idx_message_expires_at (expires_at)`

### 2) media
- `id` (PK, bigint)
- `message_id` (FK -> message.id, not null)
- `media_type` (varchar(20), not null) // IMAGE | VOICE
- `storage_url` (varchar(500), not null)
- `file_name` (varchar(255), not null)
- `file_size_bytes` (bigint, not null)
- `mime_type` (varchar(120), not null)
- `sort_order` (int, null)
- `created_at` (timestamp, not null)

Index
- `idx_media_message_id (message_id)`
- `idx_media_message_type (message_id, media_type)`

### 3) access_log (optional)
- `id` (PK, bigint)
- `message_id` (FK -> message.id, not null)
- `accessed_at` (timestamp, not null)
- `ip_hash` (varchar(128), null)
- `user_agent` (varchar(500), null)

## 관계
- `message 1 : N media`
- `message 1 : N access_log`

## 제약/정책 매핑
- 비회원 작성: 별도 user FK 없음(MVP)
- 수정 가능 조건: `now < message.expires_at`
- 기본 만료: `created_at + interval 7 day`
- 업로드 제한
  - IMAGE: message당 최대 3건
  - VOICE: message당 최대 1건
  - 총 용량 14MB: 애플리케이션 레벨 검증
