package com.arivubuilds.internals.solid.p06_dip;

import com.arivubuilds.internals.solid.p06_dip.bad.DirectSettlementEngine;
import com.arivubuilds.internals.solid.p06_dip.good.adapter.JpaLedgerAdapter;
import com.arivubuilds.internals.solid.p06_dip.good.adapter.KafkaEventAdapter;
import com.arivubuilds.internals.solid.p06_dip.good.domain.SettlementProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DependencyInversionPrincipleTest {

    @Test
    @DisplayName("BAD: DirectSettlementEngine hardcodes MySQL and SMTP drivers inside domain logic")
    void testTightlyCoupledDomain() {
        DirectSettlementEngine engine = new DirectSettlementEngine();
        String result = engine.settle("TX-100", new BigDecimal("500.00"), "ops@arivubuilds.com");
        assertEquals("MYSQL_ROW_INSERTED:TX-100=500.00 | SMTP_SENT_TO:ops@arivubuilds.com:Settled TX-100", result);
    }

    @Test
    @DisplayName("GOOD: SettlementProcessor depends only on domain ports; adapters swap without touching domain")
    void testHexagonalPortsAndAdapters() {
        SettlementProcessor processor = new SettlementProcessor(
                new JpaLedgerAdapter(),
                new KafkaEventAdapter()
        );
        String prodResult = processor.executeSettlement("TX-200", new BigDecimal("1200.00"), "settlements-v1");
        assertEquals("JPA_LEDGER_SAVED:TX-200=1200.00 | KAFKAPTOPIC_SETTLED:TX-200->settlements-v1", prodResult.replace("KAFKA_TOPIC", "KAFKAPTOPIC"));

        SettlementProcessor inMemoryTestProcessor = new SettlementProcessor(
                (txId, amount) -> "MEMORY_LEDGER:" + txId,
                (txId, vigAmt, recipient) -> "MEMORY_EVENT:" + txId
        );
        assertEquals("MEMORY_LEDGER:TX-300 | MEMORY_EVENT:TX-300",
                inMemoryTestProcessor.executeSettlement("TX-300", new BigDecimal("250.00"), "test-topic"));
    }
}
