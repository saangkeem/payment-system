package com.example.payment.service.saga;

import com.example.commonmodel.dto.AuthorizationResponse;
import com.example.commonmodel.dto.TokenResponse;
import com.example.payment.model.Transaction;
import com.example.payment.service.AuthorizationService;
import com.example.payment.service.PaymentService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


@Component
public class PaymentProcessOrchestrator {

    private final AuthorizationService authorizationService;
    private final RabbitTemplate rabbitTemplate;
    private final PaymentService paymentService;

    public PaymentProcessOrchestrator(AuthorizationService authorizationService, RabbitTemplate rabbitTemplate, PaymentService paymentService) {
        this.authorizationService = authorizationService;
        this.rabbitTemplate = rabbitTemplate;
        this.paymentService = paymentService;
    }

    @RabbitListener(queues = "sagaQueue")
    public void processPaymentAuthorizationResponse(TokenResponse tokenResponse, Message incomingMessage) {
        AuthorizationResponse response = new AuthorizationResponse();



        try {
            if ("SUCCESS".equals(tokenResponse.getStatus())) {
                response = authorizationService.requestPaymentAuthorization(tokenResponse.getRefId(), tokenResponse.getTokenId());
                response.setAmount(tokenResponse.getAmount());
                response.setUserId(tokenResponse.getUserId());
                handleTransactionRecord(tokenResponse, response.getStatus());
            } else {
                handleCompensation(tokenResponse);
                response = new AuthorizationResponse("FAILURE", "Compensation executed", tokenResponse.getRefId(), tokenResponse.getTokenId(), tokenResponse.getUserId(), tokenResponse.getAmount());
            }
        } catch (Exception e) {
            System.err.println("Error processing response: " + e.getMessage());
            handleCompensation(tokenResponse);
            response = new AuthorizationResponse("ERROR", "Unexpected error", tokenResponse.getRefId(), tokenResponse.getTokenId(), tokenResponse.getUserId(), tokenResponse.getAmount());
        }
        sendResponse(response, incomingMessage);
    }

    private void handleTransactionRecord(TokenResponse tokenResponse, String status) {
        Transaction.Status transactionStatus = switch (status) {
            case "SUCCESS" -> Transaction.Status.SUCCESS;
            case "FAILURE" -> Transaction.Status.FAILED;
            default -> Transaction.Status.PENDING;
        };
        paymentService.recordTransaction(tokenResponse.getRefId(), tokenResponse.getTokenId(),
                tokenResponse.getAmount(), tokenResponse.getUserId(), transactionStatus);
    }

    private void handleCompensation(TokenResponse tokenResponse) {
        System.out.println("Compensating transaction for order: " + tokenResponse.getTokenId());
        notifyUser(tokenResponse.getRefId(), "Payment failed, order canceled.");
        logCompensation(tokenResponse, "Payment failed, transaction compensated.");
    }

    private void notifyUser(String refId, String message) {
        // TODO: Implement notification logic (e.g., SMS/Email)
    }

    private void logCompensation(TokenResponse tokenResponse, String reason) {
        // TODO: Implement compensation logging logic
    }

    private void sendResponse(AuthorizationResponse response, Message incomingMessage) {
        String replyTo = incomingMessage.getMessageProperties().getReplyTo();
        if (replyTo != null) {
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setCorrelationId(incomingMessage.getMessageProperties().getCorrelationId());
            Message responseMessage = rabbitTemplate.getMessageConverter().toMessage(response, messageProperties);
            rabbitTemplate.send(replyTo, responseMessage);
        } else {
            System.err.println("No replyTo address in message.");
        }
    }

    @RabbitListener(queues = "responseQueue")
    public void handleResponse(AuthorizationResponse authorizationResponse) {
        if ("SUCCESS".equals(authorizationResponse.getStatus())) {
            paymentService.completePayment(authorizationResponse.getRefId(), authorizationResponse);
        } else {
            System.out.println("Payment authorization failed for refId: " + authorizationResponse.getRefId());
        }
    }
}


