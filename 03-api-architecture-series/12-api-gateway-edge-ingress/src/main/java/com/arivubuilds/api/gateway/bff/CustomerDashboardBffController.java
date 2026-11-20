package com.arivubuilds.api.gateway.bff;

import com.arivubuilds.api.gateway.model.AccountSummary;
import com.arivubuilds.api.gateway.model.DashboardBffResponse;
import com.arivubuilds.api.gateway.model.TransactionItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/bff/v1/dashboard")
public class CustomerDashboardBffController {

    private final WebClient webClient;
    private final String accountsUrl;
    private final String transactionsUrl;

    public CustomerDashboardBffController(
            WebClient webClient,
            @Value("${downstream.accounts-url}") String accountsUrl,
            @Value("${downstream.transactions-url}") String transactionsUrl) {
        this.webClient = webClient;
        this.accountsUrl = accountsUrl;
        this.transactionsUrl = transactionsUrl;
    }

    @GetMapping("/{customerId}")
    public Mono<ResponseEntity<DashboardBffResponse>> getCustomerDashboard(@PathVariable String customerId) {
        AtomicBoolean degraded = new AtomicBoolean(false);

        Mono<AccountSummary> accountMono = webClient.get()
                .uri(accountsUrl + "/accounts/" + customerId)
                .retrieve()
                .bodyToMono(AccountSummary.class);

        Mono<List<TransactionItem>> txnsMono = webClient.get()
                .uri(transactionsUrl + "/transactions/" + customerId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<TransactionItem>>() {})
                .onErrorResume(ex -> {
                    degraded.set(true);
                    return Mono.just(List.of());
                });

        return Mono.zip(accountMono, txnsMono)
                .map(tuple -> ResponseEntity.ok(new DashboardBffResponse(
                        customerId,
                        tuple.getT1(),
                        tuple.getT2(),
                        degraded.get(),
                        Instant.now()
                )));
    }
}