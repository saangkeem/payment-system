package com.example.tokenissuance.repository;

import com.example.tokenissuance.model.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardInfoRepository extends JpaRepository<CardInfo, Long> {

    Optional<CardInfo> findByRefId(String refId);
    boolean existsByCiAndEncryptedCardNumber(String ci, String encryptedCardNumber);

    boolean existsByCiAndCardHash(String ci, String cardHash);
}


