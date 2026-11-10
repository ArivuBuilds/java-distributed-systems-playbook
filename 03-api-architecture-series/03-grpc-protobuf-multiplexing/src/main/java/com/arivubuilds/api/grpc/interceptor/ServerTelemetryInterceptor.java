package com.arivubuilds.api.grpc.interceptor;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ServerTelemetryInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ServerTelemetryInterceptor.class);
    private final AtomicInteger activeStreams = new AtomicInteger(0);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        int count = activeStreams.incrementAndGet();
        log.info("[HTTP/2 Multiplexing] Inbound RPC: {} | Active Streams: {}",
                call.getMethodDescriptor().getFullMethodName(), count);

        return next.startCall(call, headers);
    }

    public int getActiveStreamCount() {
        return activeStreams.get();
    }
}