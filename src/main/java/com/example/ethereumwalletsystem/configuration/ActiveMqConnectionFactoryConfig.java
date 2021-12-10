package com.example.ethereumwalletsystem.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

@Configuration
@Slf4j
public class ActiveMqConnectionFactoryConfig {
    @Bean
    public JmsListenerContainerFactory<DefaultMessageListenerContainer> queueListenerFactory(
        ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        factory.setCacheLevel(DefaultMessageListenerContainer.CACHE_AUTO);
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setConcurrency("1-100");
        factory.setPubSubDomain(false);
        factory.setMessageConverter(jacksonJmsMessageConverter());
        factory.setErrorHandler(e -> log.error(String.format("queueListenerFactory: %s", e.getMessage()), e));
        return factory;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
