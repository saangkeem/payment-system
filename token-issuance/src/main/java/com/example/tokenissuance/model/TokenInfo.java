package com.example.tokenissuance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Getter
@Setter
@Table(name = "token_info", indexes = {
        @Index(name = "idx_token_info_ref_id", columnList = "refId")  // refId에 인덱스 추가
})
public class TokenInfo {

    @Id
    @Column(name = "token_id", nullable = false, updatable = false, length = 36)
    private String tokenId;

    @Column(name = "ref_id", nullable = false, length = 36)
    private String refId;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Token 상태에 대한 enum 정의
    public enum Status {
        ACTIVE,
        EXPIRED,
        REVOKED,
        CONSUMED

    }

    // 기본 생성자
    protected TokenInfo() {
    }

    // 생성자
    public TokenInfo(String refId, LocalDateTime expiryTime) {
        this.tokenId = UUID.randomUUID().toString(); // UUID 생성
        this.refId = refId;
        this.expiryTime = expiryTime;
        this.createdAt = LocalDateTime.now();
        this.status = Status.ACTIVE;
    }


}
