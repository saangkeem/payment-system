package com.example.payment.service;

import com.example.commonmodel.dto.AuthorizationResponse;
import com.example.commonmodel.dto.CardRegisterRequest;
import com.example.commonmodel.dto.CardRegisterResponse;
import com.example.commonmodel.dto.TokenRequest;
import com.example.commonmodel.exception.AlreadyProgressForRefIdException;
import com.example.commonmodel.exception.CardAlreadyRegisteredException;
import com.example.commonmodel.exception.CardInfoNotFoundException;
import com.example.commonmodel.exception.IdentityNotVerifiedException;
import com.example.commonmodel.util.EncryptionUtil;
import com.example.payment.model.Transaction;
import com.example.payment.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {

    private static final long TIMEOUT = 3000L; // 5초 타임아웃
    private static final long REDIS_TTL = 3; // 중복 요청 방지를 위한 Redis TTL (5초)
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;
    private final TransactionRepository transactionRepository;
    private final ConcurrentHashMap<String, DeferredResult<AuthorizationResponse>> pendingRequests = new ConcurrentHashMap<>();
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${token-issuance.service.url}")
    private String tokenissuanceServiceUrl;

    public PaymentService(RabbitTemplate rabbitTemplate, RestTemplate restTemplate, TransactionRepository transactionRepository, RedisTemplate redisTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.restTemplate = restTemplate;
        this.transactionRepository = transactionRepository;
        this.redisTemplate = redisTemplate;
    }

    public DeferredResult<AuthorizationResponse> initiatePayment(TokenRequest tokenRequest) {
        String refId = tokenRequest.getRefId();
        String redisKey = "payment:request:" + refId + ":" + tokenRequest.getAmount(); // refId와 amount로 키를 조합

        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Boolean isFirstRequest = valueOperations.setIfAbsent(redisKey, "PROCESSING", REDIS_TTL, TimeUnit.SECONDS);

        if (isFirstRequest != null && isFirstRequest) {
            System.out.println("New payment request initiated for refId: " + refId + " / isFirstRequest :" + isFirstRequest);
        } else {
            System.out.println("Duplicate payment request detected for refId: " + refId);
            throw new AlreadyProgressForRefIdException("동일한 refId 로 결제중입니다. " + REDIS_TTL + "초 뒤에 재 요청 하세요.");
        }

        if (!isValidRefId(refId)) {
            throw new CardInfoNotFoundException("The provided refId does not exist in the system.");
        }

        DeferredResult<AuthorizationResponse> deferredResult = new DeferredResult<>(TIMEOUT);
        pendingRequests.put(refId, deferredResult);  // pendingRequests에 저장

        deferredResult.onTimeout(() -> {
            redisTemplate.delete(redisKey);
            pendingRequests.remove(refId); // 타임아웃 시 pendingRequests에서 제거
            deferredResult.setErrorResult(
                    new AuthorizationResponse("TIMEOUT", "Payment authorization timed out", refId, null, tokenRequest.getUserId(), tokenRequest.getAmount())
            );
        });

        rabbitTemplate.convertAndSend("tokenExchange", "tokenRoutingKey", tokenRequest, message -> {
            message.getMessageProperties().setReplyTo("responseQueue");
            message.getMessageProperties().setCorrelationId(refId);
            return message;
        });

        return deferredResult;
    }


    public void completePayment(String correlationId, AuthorizationResponse response) {
        DeferredResult<AuthorizationResponse> deferredResult = pendingRequests.remove(correlationId);
        if (deferredResult != null) {
            deferredResult.setResult(response);
        } else {
            System.err.println("No DeferredResult found for correlationId: " + correlationId);
        }
    }


    @Transactional
    public void recordTransaction(String refId, String tokenId, BigDecimal amount, String userId, Transaction.Status status) {
        // Transaction 엔티티 생성
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setTokenId(tokenId);
        transaction.setRefId(refId);
        transaction.setStatus(status); // 승인 성공 상태 설정
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        // Transaction 저장
        transactionRepository.save(transaction);
    }

    private boolean isValidRefId(String refId) {

        try {
            String url = tokenissuanceServiceUrl + "/api/token/validateRefId?refId={refId}";
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class, refId);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return true;
            } else if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
                // 404 Not Found 상태 코드에 대한 예외 처리
                throw new CardInfoNotFoundException("Card information not found for refId: " + refId);
            } else {
                throw new IllegalStateException("IllegalStateException validation service: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            String responseBody = e.getMessage();
            if (responseBody.contains("CARD_INFO_NOT_FOUND")) {
                throw new CardInfoNotFoundException("참조된 카드가 없습니다. : " + refId);
            }

            throw new RuntimeException(e.getMessage());
        }

    }

    // TODO: 언젠가는 외부모듈 이용해서 구현 할것
    private boolean verifyIdentity(String ci) {

        return true;
    }

    public String registerCard(CardRegisterRequest cardRegisterRequest) {
        String url = tokenissuanceServiceUrl + "/api/token/register";

        // 본인 확인이 실패하면 예외 발생
        if (!verifyIdentity(cardRegisterRequest.getCi())) {
            throw new IdentityNotVerifiedException("본인 확인에 실패했습니다. 등록되지 않은 사용자입니다.");
        }

        try {
            // CI 값 복호화
            cardRegisterRequest.setCi(EncryptionUtil.decrypt(cardRegisterRequest.getCi()));

            // 카드 등록 요청
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, cardRegisterRequest, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity.getBody();  // 성공적으로 등록된 경우
            } else {
                throw new IllegalStateException("Failed to register card: " + responseEntity.getBody());
            }

        } catch (HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            String errorMessage = String.format("Failed to register card: HTTP %s - %s", e.getStatusCode(), responseBody);
            System.err.println(errorMessage);
            throw new RuntimeException(errorMessage);

        } catch (Exception e) {
            e.printStackTrace();

            String responseBody = e.getMessage();
            if (responseBody.contains("CARD_ALREADY_REGISTERED")) {
                throw new CardAlreadyRegisteredException("이미 등록된 카드입니다.");
            }

            throw new RuntimeException("Failed to register card due to an unexpected error: " + e.getMessage());
        }
    }

    public List<Transaction> getTransaction(String refId) {
        List<Transaction> transactionOptional = transactionRepository.findAllByRefId(refId);
        return transactionOptional;
    }


}

