package com.arivubuilds.internals.solid.p05_isp;

import com.arivubuilds.internals.solid.p05_isp.bad.BasicReceiptPrinter;
import com.arivubuilds.internals.solid.p05_isp.bad.KitchenSinkDevice;
import com.arivubuilds.internals.solid.p05_isp.good.DocumentWorkflowRouter;
import com.arivubuilds.internals.solid.p05_isp.good.EnterpriseMultifunctionStation;
import com.arivubuilds.internals.solid.p05_isp.good.ThermalReceiptPrinter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InterfaceSegregationPrincipleTest {

    @Test
    @DisplayName("BAD: Fat KitchenSinkDevice forces BasicReceiptPrinter to implement unsupported methods")
    void testFatInterfaceForcesUnsupportedOperations() {
        KitchenSinkDevice device = new BasicReceiptPrinter();
        assertEquals("RECEIPT_PRINTED:INV-01", device.print("INV-01"));
        assertThrows(UnsupportedOperationException.class, () -> device.scan("INV-01"));
    }

    @Test
    @DisplayName("GOOD: Segregated Printable and Scannable roles bind only to capable implementations")
    void testSegregatedInterfacesBindCleanly() {
        ThermalReceiptPrinter thermal = new ThermalReceiptPrinter();
        EnterpriseMultifunctionStation mfp = new EnterpriseMultifunctionStation();

        DocumentWorkflowRouter router = new DocumentWorkflowRouter(
                List.of(thermal, mfp),
                List.of(mfp)
        );

        assertEquals(List.of("THERMAL_PRINT:POLICY-99", "MFP_LASER_PRINT:POLICY-99"),
                router.broadcastPrint("POLICY-99"));
        assertEquals(List.of("MFP_DUPLEX_SCAN:POLICY-99"),
                router.archiveScan("POLICY-99"));
    }
}
