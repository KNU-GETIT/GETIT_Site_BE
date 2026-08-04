CREATE TABLE file_asset
(
    id            bigint       NOT NULL AUTO_INCREMENT,

    stored_key    varchar(512) NOT NULL,
    original_name varchar(255) NOT NULL,
    url           varchar(512) NOT NULL,
    size          bigint       NOT NULL,
    content_type  varchar(100) NOT NULL,

    status        varchar(20)  NOT NULL,
    uploader_id   bigint       NOT NULL,

    created_at    datetime(6)  NOT NULL,
    updated_at    datetime(6)  NOT NULL,
    deleted_at    datetime(6)  DEFAULT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_file_asset_stored_key (stored_key),
    CONSTRAINT ck_file_asset_status CHECK (status IN ('PENDING', 'CONNECTED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
