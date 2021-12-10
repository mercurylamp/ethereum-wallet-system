package com.example.ethereumwalletsystem.service;

import lombok.RequiredArgsConstructor;
import org.apache.activemq.ScheduledMessage;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final JmsTemplate jmsTemplate;

    public void queue(String destination, Object message) {
        jmsTemplate.convertAndSend(destination, message);
    }

    public void queueDelay(String destination, Object message, int delayInSeconds) {
        jmsTemplate.convertAndSend(destination, message, msg -> {
            msg.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delayInSeconds * 1000L);
            return msg;
        });
    }
}
