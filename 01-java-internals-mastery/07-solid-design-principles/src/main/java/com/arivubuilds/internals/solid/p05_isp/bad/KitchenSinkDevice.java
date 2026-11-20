package com.arivubuilds.internals.solid.p05_isp.bad;

public interface KitchenSinkDevice {
    String print(String document);
    String scan(String document);
    String fax(String document, String destinationNumber);
    String staple(String document);
}
