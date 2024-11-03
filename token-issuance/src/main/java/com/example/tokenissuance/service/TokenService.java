package com.example.tokenissuance.service;

import com.example.commonmodel.dto.AuthorizationRequest;
import com.example.commonmodel.dto.AuthorizationResponse;
import com.example.commonmodel.dto.CardRegisterRequest;
import com.example.commonmodel.exception.CardAlreadyRegisteredException;
import com.example.commonmodel.exception.CardInfoNotFoundException;
import com.example.tokenissuance.model.CardInfo;
import com.example.tokenissuance.model.TokenInfo;
import com.example.tokenissuance.repository.CardInfoRepository;
import com.example.tokenissuance.repository.TokenInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {
    private final CardInfoRepository cardInfoRepository;
    private final TokenInfoRepository tokenInfoRepository;

    public TokenService(CardInfoRepository cardInfoRepository, TokenInfoRepository tokenInfoRepository) {
        this.cardInfoRepository = cardInfoRepository;
        this.tokenInfoRepository = tokenInfoRepository;

    }


    public String generateRefId() {
        return UUID.randomUUID().toString();
    }

    @Transactional()
    public CardInfo registerCard(CardRegisterRequest cardRegisterRequest) {

        String refId = generateRefId();
        CardInfo cardInfo = new CardInfo();
        cardInfo.setEncryptedCardNumber(cardRegisterRequest.getEncryptedCardNumber());
        cardInfo.setRefId(refId);
        cardInfo.setCi(cardRegisterRequest.getCi());
        cardInfo.setCardHash(cardRegisterRequest.getCardHash());

        if (cardInfoRepository.existsByCiAndCardHash(cardInfo.getCi(), cardInfo.getCardHash())) {
            throw new CardAlreadyRegisteredException("This card is already registered.");
        }
        return cardInfoRepository.save(cardInfo);
    }


    public CardInfo getRefId(String refId) {
        return cardInfoRepository.findByRefId(refId)
                .orElseThrow(() -> new CardInfoNotFoundException("참조된 카드정보가 없습니다. " + refId));
    }

    @Transactional
    public AuthorizationResponse verifyToken(AuthorizationRequest authorizationRequest) {
        AuthorizationResponse response = new AuthorizationResponse("FAILURE", "Token verification failed", authorizationRequest.getRefId(), authorizationRequest.getTokenId(), null, null);

        try {
            // 토큰이 존재하지 않으면 바로 "Token not found" 응답 반환
            Optional<TokenInfo> tokenInfoOptional = tokenInfoRepository.findByTokenId(authorizationRequest.getTokenId());
            if (tokenInfoOptional.isEmpty()) {
                response.setMessage("토큰이 없습니다.");
                return response;
            }

            TokenInfo tokenInfo = tokenInfoOptional.get();

            // 토큰이 이미 사용된 경우 응답 설정 후 반환
            if (tokenInfo.getStatus() == TokenInfo.Status.CONSUMED) {
                response.setMessage("이미 사용된 토큰입니다.");
                return response;
            }

            // 만료 시간이 지나지 않았으면 토큰을 사용 완료로 표시하고 성공 응답 반환
            if (tokenInfo.getExpiryTime().isAfter(LocalDateTime.now())) {
                response.setStatus("SUCCESS");
                response.setMessage("결제 성공");
                tokenInfo.setStatus(TokenInfo.Status.CONSUMED);
                tokenInfoRepository.save(tokenInfo);
                return response;
            }

            // 만료된 경우 상태를 EXPIRED로 설정하고 저장
            response.setMessage("만료된 토큰입니다.");
            tokenInfo.setStatus(TokenInfo.Status.EXPIRED);
            tokenInfoRepository.save(tokenInfo);

        } catch (Exception e) {
            // 예외 발생 시 오류 응답 설정
            System.err.println("An error occurred during token verification: " + e.getMessage());
            response.setStatus("ERROR");
            response.setMessage("An unexpected error occurred during token verification");
        }

        return response;
    }


}
