package com.arivubuilds.microservices.dbperservice.orders;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderBoundedService {

    private final OrderLedgerRepository repository;

    public OrderBoundedService(OrderLedgerRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrderLedgerEntity placeOrder(String orderId,
                                    String customerId,
                                    String productId,
                                    int quantity,
                                    BigDecimal unitPriceSnapshot,
                                    String initialTitle) {
        OrderLedgerEntity entity = new OrderLedgerEntity(
                orderId, customerId, productId, quantity, unitPriceSnapshot, initialTitle
        );
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public OrderLedgerEntity getOrder(String orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    @Transactional
    public int handleProductTitleUpdatedEvent(String productId, String newProductTitle) {
        List<OrderLedgerEntity> affected = repository.findByProductId(productId);
        affected.forEach(order -> order.setCachedProductTitle(newProductTitle));
        repository.saveAll(affected);
        return affected.size();
    }
}
