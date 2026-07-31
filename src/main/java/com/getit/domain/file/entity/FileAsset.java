package com.getit.domain.file.entity;

import com.getit.global.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "file_asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileAsset extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 512)
  private String storedKey;

  @Column(nullable = false, length = 255)
  private String originalName;

  @Column(nullable = false, length = 512)
  private String url;

  @Column(nullable = false)
  private Long size;

  @Column(nullable = false, length = 100)
  private String contentType;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(nullable = false, length = 20)
  private FileStatus status;

  @Column(nullable = false)
  private Long uploaderId;

  @Builder(access = AccessLevel.PRIVATE)
  private FileAsset(
      String storedKey,
      String originalName,
      String url,
      Long size,
      String contentType,
      Long uploaderId
  ) {
    this.storedKey = storedKey;
    this.originalName = originalName;
    this.url = url;
    this.size = size;
    this.contentType = contentType;
    this.uploaderId = uploaderId;
    this.status = FileStatus.PENDING;
  }

  public static FileAsset upload(
      String storedKey,
      String originalName,
      String url,
      Long size,
      String contentType,
      Long uploaderId
  ) {
    return FileAsset.builder()
        .storedKey(storedKey)
        .originalName(originalName)
        .url(url)
        .size(size)
        .contentType(contentType)
        .uploaderId(uploaderId)
        .build();
  }

  public void connect() {
    this.status = FileStatus.CONNECTED;
  }

  public void disconnect() {
    this.status = FileStatus.PENDING;
  }

  public boolean isInUse() {
    return status == FileStatus.CONNECTED;
  }
}
