package com.kapusniak.tomasz.scheduler;

import com.kapusniak.tomasz.openapi.model.Delivery;
import com.kapusniak.tomasz.openapi.model.DeliveryStatus;
import com.kapusniak.tomasz.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Profile({"!test", "!jdbc", "!integration-test"})
@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final DeliveryService deliveryService;

    @Scheduled(cron = "0 0 */3 * * *")
    @Transactional
    public void deleteDeliveredOlderThan2Days() {
        try {
            List<Delivery> deliveryList = deliveryService
                    .findAllByDeliveryStatusAndDeliveryTimeBefore(
                            DeliveryStatus.DELIVERED,
                            LocalDateTime.
                                    now()
                                    .minusDays(2));

            if (deliveryList.isEmpty()) {
                log.info("No delivered deliveries older than 2 days were found to be deleted.");
            } else {
                log.warn("Found {} delivered deliveries older than 2 days to be deleted.", deliveryList.size());

                deliveryList.forEach(delivery -> deliveryService.delete(delivery.getUuid()));
            }
            log.info("{} deliveries deleted.", deliveryList.size());
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.warn("Locking error occurred during deleteDeliveredOlderThan2Days. Sending details:");
        }
    }

}