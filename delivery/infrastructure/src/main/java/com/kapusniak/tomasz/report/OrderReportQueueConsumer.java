package com.kapusniak.tomasz.report;

import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
public class OrderReportQueueConsumer implements MessageListener {

    @Override
    @JmsListener(
            destination = "#{T(com.kapusniak.tomasz.message.Topic).ORDERS.toString().toLowerCase()}"
    )
    public void onMessage(Message message) {

        try {
            log.info("Received Message: " + message.getBody(List.class));

        } catch (Exception e) {
            log.error("Received Exception : " + e);
            e.printStackTrace();
        }

    }

}
