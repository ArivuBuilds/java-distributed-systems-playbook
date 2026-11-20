package com.arivubuilds.internals.solid.p05_isp.good;

import org.springframework.stereotype.Component;

@Component
public class EnterpriseMultifunctionStation implements Printable, Scannable, Faxable {

    @Override
    public String print(String document) {
        return "MFP_LASER_PRINT:" + document;
    }

    @Override
    public String scan(String document) {
        return "MFP_DUPLEX_SCAN:" + document;
    }

    @Override
    public String fax(String document, String destinationNumber) {
        return "MFP_FAX_TO_" + destinationNumber + ":" + document;
    }
}
