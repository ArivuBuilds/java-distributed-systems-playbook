package com.arivubuilds.internals.solid.p05_isp.good;

import org.springframework.stereotype.Component;

@Component
public class ThermalReceiptPrinter implements Printable {
    @Override
    public String print(String document) {
        return "THERMAL_PRINT:" + document;
    }
}
