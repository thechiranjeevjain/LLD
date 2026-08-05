package com.chiranjeev.lld.fixgateway.routing;

import com.chiranjeev.lld.fixgateway.domain.ExecutionReport;
import com.chiranjeev.lld.fixgateway.domain.OrderRequest;
import com.chiranjeev.lld.fixgateway.risk.DuplicateClientOrderTracker;
import com.chiranjeev.lld.fixgateway.risk.RiskEngine;
import com.chiranjeev.lld.fixgateway.risk.RiskResult;

public final class OrderService {
    private final DuplicateClientOrderTracker duplicateTracker;
    private final RiskEngine riskEngine;
    private final OrderRouter orderRouter;

    public OrderService(
            DuplicateClientOrderTracker duplicateTracker,
            RiskEngine riskEngine,
            OrderRouter orderRouter
    ) {
        this.duplicateTracker = duplicateTracker;
        this.riskEngine = riskEngine;
        this.orderRouter = orderRouter;
    }

    public ExecutionReport place(OrderRequest order) {
        if (!duplicateTracker.reserve(order)) {
            return ExecutionReport.rejected(order, "Duplicate ClOrdID: " + order.clientOrderId());
        }

        RiskResult riskResult = riskEngine.evaluate(order);
        if (!riskResult.accepted()) {
            return ExecutionReport.rejected(order, riskResult.reason());
        }

        return orderRouter.route(order);
    }
}

