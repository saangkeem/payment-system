package com.example.tokenissuance.repository;


import com.example.tokenissuance.model.CardInfo;
import com.example.tokenissuance.model.TokenInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenInfoRepository extends JpaRepository<TokenInfo, String> {
    // 필요한 쿼리 메서드를 추가할 수 있습니다.

    Optional<TokenInfo> findByTokenId(String tokenId);
}
