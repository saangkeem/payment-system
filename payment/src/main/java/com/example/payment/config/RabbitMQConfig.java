package com.example.payment.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        return new Queue("tokenQueue", true);
    }

    @Bean
    public Queue sagaQueue() {
        return new Queue("sagaQueue", true);
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
    public Binding tokenBinding() {
        return BindingBuilder.bind(tokenQueue()).to(tokenExchange()).with("tokenRoutingKey");
    }

    @Bean
    public Binding sagaBinding() {
        return BindingBuilder.bind(sagaQueue()).to(sagaExchange()).with("paymentRoutingKey");
    }
}
