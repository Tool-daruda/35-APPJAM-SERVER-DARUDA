-- #363 tool_blog OG 메타데이터 컬럼 추가 (prod 배포 전 수동 실행)
-- prod 는 ddl-auto: validate 이며 마이그레이션 도구가 없으므로 배포 전에 직접 적용한다.
ALTER TABLE tool_blog
    ADD COLUMN title              VARCHAR(500)  NULL,
    ADD COLUMN thumbnail_url      VARCHAR(2000) NULL,
    ADD COLUMN summary            VARCHAR(2000) NULL,
    ADD COLUMN site_name          VARCHAR(200)  NULL,
    ADD COLUMN favicon_url        VARCHAR(2000) NULL,
    ADD COLUMN metadata_fetched_at DATETIME     NULL;
