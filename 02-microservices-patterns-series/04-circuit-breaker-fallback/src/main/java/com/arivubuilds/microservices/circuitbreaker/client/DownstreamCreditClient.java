package com.arivubuilds.microservices.circuitbreaker.client;

import com.arivubuilds.microservices.circuitbreaker.dto.CreditAssessmentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DownstreamCreditClient {

    private final RestClient restClient;
    private final String creditScoringUri;

    public DownstreamCreditClient(@Value("${downstream.credit-scoring-uri}") String creditScoringUri) {
        this.restClient = RestClient.create();
        this.creditScoringUri = creditScoringUri;
    }

    public CreditAssessmentResponse fetchLiveCreditScore(String customerId) {
        return restClient.get()
                .uri(creditScoringUri + "/credit/" + customerId)
                .retrieve()
                .body(CreditAssessmentResponse.class);
    }
}