package com.example.tokenissuance.listener;

import com.example.commonmodel.dto.TokenRequest;
import com.example.commonmodel.dto.TokenResponse;
import com.example.commonmodel.exception.CardInfoNotFoundException;
import com.example.tokenissuance.model.CardInfo;
import com.example.tokenissuance.model.TokenInfo;
import com.example.tokenissuance.repository.CardInfoRepository;
import com.example.tokenissuance.repository.TokenInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TokenIssuanceListener {

    private final CardInfoRepository cardInfoRepository;
    private final RabbitTemplate rabbitTemplate;
    private final TokenInfoRepository tokenInfoRepository;


    public TokenIssuanceListener(CardInfoRepository cardInfoRepository, RabbitTemplate rabbitTemplate, TokenInfoRepository tokenInfoRepository) {
        this.cardInfoRepository = cardInfoRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.tokenInfoRepository = tokenInfoRepository;
    }

    @Transactional
    @RabbitListener(queues = "tokenQueue")
    public void processTokenRequest(TokenRequest tokenRequest) {
        try {
            // 토큰 발급 및 저장
            TokenInfo tokenInfo = issueToken(tokenRequest.getRefId());
            tokenInfoRepository.save(tokenInfo); // DB에 토큰 저장
            // 발급된 토큰 정보를 메시지로 전송
            rabbitTemplate.convertAndSend("sagaExchange", "paymentRoutingKey",
                    new TokenResponse(tokenInfo.getRefId(), tokenInfo.getTokenId(), "SUCCESS", tokenRequest.getUserId(), tokenRequest.getAmount()),
                    message -> {
                        message.getMessageProperties().setReplyTo("responseQueue"); // 응답을 받을 큐 이름을 설정
                        return message;
                    });

        } catch (Exception e) {
            rabbitTemplate.convertAndSend("sagaExchange", "paymentRoutingKey",
                    new TokenResponse(tokenRequest.getRefId(), null, "FAILURE",tokenRequest.getUserId(), tokenRequest.getAmount()));
        }
    }


    private TokenInfo issueToken(String refId) {
        // 만료 시간 설정 (1분)
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(1);

        return new TokenInfo(refId, expiryTime);
    }


}
