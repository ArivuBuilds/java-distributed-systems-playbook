package com.arivubuilds.api.rest.controller;

import com.arivubuilds.api.rest.dto.CreateOrderRequest;
import com.arivubuilds.api.rest.dto.OrderResponse;
import com.arivubuilds.api.rest.exception.InvalidOrderStateException;
import com.arivubuilds.api.rest.exception.OrderNotFoundException;
import com.arivubuilds.api.rest.model.Order;
import com.arivubuilds.api.rest.model.OrderStatus;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final Map<String, Order> orderStore = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(orderId, request.customerId(), request.totalAmount());
        orderStore.put(orderId, order);

        OrderResponse response = mapToResponse(order);
        URI location = linkTo(methodOn(OrderController.class).getOrderById(orderId)).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("id") String id) {
        Order order = getOrThrow(id);
        return ResponseEntity.ok(mapToResponse(order));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<OrderResponse>> listOrders() {
        var items = orderStore.values().stream().map(this::mapToResponse).toList();
        Link selfLink = linkTo(methodOn(OrderController.class).listOrders()).withSelfRel();
        return ResponseEntity.ok(CollectionModel.of(items, selfLink));
    }

    @PutMapping("/{id}/payment")
    public ResponseEntity<OrderResponse> payOrder(@PathVariable("id") String id) {
        Order order = getOrThrow(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Cannot pay for order in status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.PAID);
        return ResponseEntity.ok(mapToResponse(order));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable("id") String id) {
        Order order = getOrThrow(id);
        if (order.getStatus() == OrderStatus.FULFILLED) {
            throw new InvalidOrderStateException("Cannot cancel an already fulfilled order");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return ResponseEntity.noContent().build();
    }

    private Order getOrThrow(String id) {
        Order o = orderStore.get(id);
        if (o == null) throw new OrderNotFoundException(id);
        return o;
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse resp = new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );

        resp.add(linkTo(methodOn(OrderController.class).getOrderById(order.getId())).withSelfRel());

        if (order.getStatus() == OrderStatus.PENDING) {
            resp.add(linkTo(methodOn(OrderController.class).payOrder(order.getId())).withRel("pay"));
            resp.add(linkTo(methodOn(OrderController.class).cancelOrder(order.getId())).withRel("cancel"));
        } else if (order.getStatus() == OrderStatus.PAID) {
            resp.add(linkTo(methodOn(OrderController.class).cancelOrder(order.getId())).withRel("cancel"));
        }
        return resp;
    }
}