package com.example.payment.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "transaction", indexes = {
        @Index(name = "idx_transaction_ref_id", columnList = "refId")  // refId에 인덱스 추가
})
public class Transaction {

    public enum Status {
        PENDING,    // 결제 요청이 들어왔으나 아직 완료되지 않은 상태
        SUCCESS,    // 결제가 성공적으로 완료된 상태
        FAILED      // 결제가 실패한 상태
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "token_id", nullable = false, updatable = false, length = 36)
    private String tokenId;

    @Column(name = "ref_id", nullable = false, length = 36)
    private String refId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors, Getters, and Setters

    public Transaction() {
        // 기본 생성자
    }

    public Transaction(String userId, String tokenId, String refId, BigDecimal amount, Status status) {
        this.userId = userId;
        this.tokenId = tokenId;
        this.refId = refId;
        this.amount = amount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
