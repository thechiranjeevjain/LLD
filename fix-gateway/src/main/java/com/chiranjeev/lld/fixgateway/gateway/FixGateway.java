package com.chiranjeev.lld.fixgateway.gateway;

import com.chiranjeev.lld.fixgateway.domain.ExecutionReport;
import com.chiranjeev.lld.fixgateway.domain.OrderRequest;
import com.chiranjeev.lld.fixgateway.fix.FixMessage;
import com.chiranjeev.lld.fixgateway.fix.FixParser;
import com.chiranjeev.lld.fixgateway.fix.FixSerializer;
import com.chiranjeev.lld.fixgateway.fix.FixTags;
import com.chiranjeev.lld.fixgateway.fix.InMemorySequenceStore;
import com.chiranjeev.lld.fixgateway.fix.SessionContext;
import com.chiranjeev.lld.fixgateway.fix.SessionManager;
import com.chiranjeev.lld.fixgateway.risk.DuplicateClientOrderTracker;
import com.chiranjeev.lld.fixgateway.risk.MaxNotionalRule;
import com.chiranjeev.lld.fixgateway.risk.MaxQuantityRule;
import com.chiranjeev.lld.fixgateway.risk.RiskEngine;
import com.chiranjeev.lld.fixgateway.routing.InMemoryExchange;
import com.chiranjeev.lld.fixgateway.routing.OrderService;

import java.math.BigDecimal;
import java.util.List;

public final class FixGateway {
    private final FixParser parser;
    private final SessionManager sessionManager;
    private final OrderFixMapper mapper;
    private final OrderService orderService;

    public FixGateway(
            FixParser parser,
            SessionManager sessionManager,
            OrderFixMapper mapper,
            OrderService orderService
    ) {
        this.parser = parser;
        this.sessionManager = sessionManager;
        this.mapper = mapper;
        this.orderService = orderService;
    }

    public static FixGateway defaultGateway(String gatewayCompId) {
        FixSerializer serializer = new FixSerializer();
        RiskEngine riskEngine = new RiskEngine(List.of(
                new MaxQuantityRule(250_000),
                new MaxNotionalRule(new BigDecimal("10000000.00"))
        ));
        return new FixGateway(
                new FixParser(),
                new SessionManager(gatewayCompId, new InMemorySequenceStore(), serializer),
                new OrderFixMapper(),
                new OrderService(new DuplicateClientOrderTracker(), riskEngine, new InMemoryExchange())
        );
    }

    public String onMessage(String rawMessage) {
        FixMessage inbound = parser.parse(rawMessage);
        SessionContext context = sessionManager.accept(inbound);

        try {
            FixMessage outbound = dispatch(inbound, context);
            return sessionManager.serialize(context, outbound);
        } catch (FixGatewayException ex) {
            return sessionManager.serialize(context, sessionReject(inbound, ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return sessionManager.serialize(context, sessionReject(inbound, ex.getMessage()));
        }
    }

    private FixMessage dispatch(FixMessage inbound, SessionContext context) {
        return switch (inbound.messageType()) {
            case FixTags.MSG_TYPE_LOGON -> onLogon(context);
            case FixTags.MSG_TYPE_HEARTBEAT -> onHeartbeat(inbound, context);
            case FixTags.MSG_TYPE_NEW_ORDER_SINGLE -> onNewOrder(inbound, context);
            default -> sessionReject(inbound, "Unsupported MsgType: " + inbound.messageType());
        };
    }

    private FixMessage onLogon(SessionContext context) {
        sessionManager.markLoggedOn(context);
        return FixMessage.builder(FixTags.MSG_TYPE_LOGON)
                .put(FixTags.ENCRYPT_METHOD, "0")
                .put(FixTags.HEART_BT_INT, "30")
                .build();
    }

    private FixMessage onHeartbeat(FixMessage inbound, SessionContext context) {
        sessionManager.ensureLoggedOn(context);
        FixMessage.Builder heartbeat = FixMessage.builder(FixTags.MSG_TYPE_HEARTBEAT);
        inbound.get(FixTags.TEST_REQ_ID).ifPresent(value -> heartbeat.put(FixTags.TEST_REQ_ID, value));
        return heartbeat.build();
    }

    private FixMessage onNewOrder(FixMessage inbound, SessionContext context) {
        sessionManager.ensureLoggedOn(context);
        OrderRequest order = mapper.toOrderRequest(inbound, context.clientCompId());
        ExecutionReport report = orderService.place(order);
        return mapper.toExecutionReport(report, order);
    }

    private FixMessage sessionReject(FixMessage inbound, String reason) {
        FixMessage.Builder reject = FixMessage.builder(FixTags.MSG_TYPE_SESSION_REJECT)
                .put(FixTags.REF_SEQ_NUM, inbound.get(FixTags.MSG_SEQ_NUM).orElse("0"))
                .put(FixTags.REF_MSG_TYPE, inbound.get(FixTags.MSG_TYPE).orElse("?"))
                .put(FixTags.TEXT, reason);
        return reject.build();
    }
}

