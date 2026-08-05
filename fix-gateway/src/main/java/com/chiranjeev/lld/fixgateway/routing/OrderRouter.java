package com.chiranjeev.lld.fixgateway.routing;

import com.chiranjeev.lld.fixgateway.domain.ExecutionReport;
import com.chiranjeev.lld.fixgateway.domain.OrderRequest;

public interface OrderRouter {
    ExecutionReport route(OrderRequest order);
}

