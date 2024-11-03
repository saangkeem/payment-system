package com.example.tokenissuance.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public Queue tokenQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "dlxExchange"); // Dead Letter Exchange 설정
        args.put("x-dead-letter-routing-key", "dlxRoutingKey"); // Dead Letter 라우팅 키
        args.put("x-message-ttl", 60000); // 메시지 TTL 60초 (1분)
        return new Queue("tokenQueue", true, false, false, args);
    }

    @Bean
    public Queue sagaQueue() {
        return new Queue("sagaQueue", true);
    }


    @Bean
    public Queue replyQueue() {
        return new Queue("replyQueue", true);  // 응답 큐 설정
    }

    @Bean
    public Queue dlqQueue() {
        return new Queue("dlqQueue", true);
    }

    @Bean
    public TopicExchange tokenExchange() {
        return new TopicExchange("tokenExchange");
    }

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange("sagaExchange");
    }

    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange("dlxExchange");
    }

    @Bean
    public Binding tokenBinding() {
        return BindingBuilder.bind(tokenQueue()).to(tokenExchange()).with("tokenRoutingKey");
    }

    @Bean
    public Binding sagaBinding() {
        return BindingBuilder.bind(sagaQueue()).to(sagaExchange()).with("paymentRoutingKey");
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue()).to(dlxExchange()).with("dlxRoutingKey");
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());

        // 재시도 설정
        RetryTemplate retryTemplate = new RetryTemplate();

        // 재시도 정책 (최대 5회)
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5);
        retryTemplate.setRetryPolicy(retryPolicy);

        // 재시도 간격 (2초 대기 후 재시도)
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(2000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        factory.setRetryTemplate(retryTemplate);

        // DLQ 처리를 위한 ErrorHandler 설정
        factory.setErrorHandler(new ConditionalRejectingErrorHandler());

        return factory;
    }
}
