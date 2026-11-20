package com.arivubuilds.internals.solid.p05_isp.bad;

public class BasicReceiptPrinter implements KitchenSinkDevice {

    @Override
    public String print(String document) {
        return "RECEIPT_PRINTED:" + document;
    }

    @Override
    public String scan(String document) {
        throw new UnsupportedOperationException("BasicReceiptPrinter has no scanner hardware");
    }

    @Override
    public String fax(String document, String destinationNumber) {
        throw new UnsupportedOperationException("BasicReceiptPrinter has no fax modem");
    }

    @Override
    public String staple(String document) {
        throw new UnsupportedOperationException("BasicReceiptPrinter has no stapler");
    }
}
