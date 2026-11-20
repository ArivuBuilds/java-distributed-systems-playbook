package com.arivubuilds.internals.solid.p06_dip.good.adapter;

import com.arivubuilds.internals.solid.p06_dip.good.domain.SettlementEventPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class KafkaEventAdapter implements SettlementEventPort {
    @Override
    public String publishSettledEvent(String txId, BigDecimal amount, String recipient) {
        return "KAFKA_TOPIC_SETTLED:" + txId + "->" + recipient;
    }
}
