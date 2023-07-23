package com.kapusniak.tomasz.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JmsProducer {

    private final JmsTemplate jmsTemplate;

    public void sendMessage(String topic, Object model) {
        try {
            log.info("Attempting Send message to queue: " + topic);
            jmsTemplate.convertAndSend(topic, model);
        } catch (Exception e) {
            log.error("Received Exception during send Message: ", e);
        }
    }
}