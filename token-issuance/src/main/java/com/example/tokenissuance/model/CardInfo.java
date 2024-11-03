
package com.example.tokenissuance.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "card_info", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ci", "cardHash"})
}, indexes = {
        @Index(name = "idx_card_info_ref_id", columnList = "refId")
})
public class CardInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String encryptedCardNumber;
    private String refId;
    private String ci;
    private String cardHash;


}