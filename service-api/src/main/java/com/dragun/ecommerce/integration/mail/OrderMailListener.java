package com.dragun.ecommerce.integration.mail;

import com.dragun.ecommerce.service.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends order mails only after the order transaction commits, on a worker thread.
 * Kept apart from OrderMailService: a @TransactionalEventListener may not itself be @Transactional (REQUIRED).
 */
@Component
@RequiredArgsConstructor
public class OrderMailListener {

    private final OrderMailService orderMailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        orderMailService.sendOrderPlacedMails(event.orderId());
    }
}
