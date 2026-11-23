package com.arivubuilds.microservices.retry.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Component
public class DownstreamSettlementClient {

    private final RestClient restClient;
    private final String settlementUri;

    public DownstreamSettlementClient(@Value("${downstream.settlement-uri}") String settlementUri) {
        this.restClient = RestClient.create();
        this.settlementUri = settlementUri;
    }

    public String submitSettlement(String transactionId) {
        return restClient.post()
                .uri(settlementUri + "/settlements/" + transactionId)
                .retrieve()
                .body(String.class);
    }

    public static boolean isTransientFailure(Throwable throwable) {
        if (throwable instanceof HttpClientErrorException) {
            return false; // Never retry 4xx client/validation failures
        }
        if (throwable instanceof HttpServerErrorException serverEx) {
            int code = serverEx.getStatusCode().value();
            return code == 502 || code == 503 || code == 504;
        }
        return true; // Network timeouts / connection resets are transient
    }
}