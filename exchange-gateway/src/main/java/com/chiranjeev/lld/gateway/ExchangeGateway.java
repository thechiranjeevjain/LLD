package com.chiranjeev.lld.gateway;

import java.util.*;
import java.util.function.Consumer;

public final class ExchangeGateway {
    private final ProtocolAdapter adapter;private final RateLimiter limiter;private final ExchangeTransport transport;private final Consumer<VenueEvent> eventSink;private final Deque<InternalOrder> reconnectQueue=new ArrayDeque<>();private final Set<String> knownIds=new HashSet<>();private boolean connected;
    public ExchangeGateway(ProtocolAdapter adapter,RateLimiter limiter,ExchangeTransport transport,Consumer<VenueEvent> eventSink){this.adapter=Objects.requireNonNull(adapter);this.limiter=Objects.requireNonNull(limiter);this.transport=Objects.requireNonNull(transport);this.eventSink=Objects.requireNonNull(eventSink);}
    public synchronized SubmissionResult submit(InternalOrder order){Objects.requireNonNull(order);if(!knownIds.add(order.id()))throw new IllegalArgumentException("duplicate order id");if(!connected){reconnectQueue.addLast(order);return new SubmissionResult(SubmissionResult.Status.QUEUED_DISCONNECTED,order.id(),"awaiting reconnect");}return route(order);}
    public synchronized int onConnected(){connected=true;int sent=0;while(!reconnectQueue.isEmpty()&&limiter.tryAcquire()){transport.send(adapter.encodeNewOrder(reconnectQueue.removeFirst()));sent++;}return sent;}
    public synchronized void onDisconnected(){connected=false;}
    public synchronized int flush(){if(!connected)return 0;int sent=0;while(!reconnectQueue.isEmpty()&&limiter.tryAcquire()){transport.send(adapter.encodeNewOrder(reconnectQueue.removeFirst()));sent++;}return sent;}
    public void onVenueMessage(String wireMessage){eventSink.accept(adapter.decodeEvent(wireMessage));}
    public synchronized int queuedCount(){return reconnectQueue.size();}
    private SubmissionResult route(InternalOrder order){if(!limiter.tryAcquire()){reconnectQueue.addLast(order);return new SubmissionResult(SubmissionResult.Status.QUEUED_THROTTLED,order.id(),"queued behind venue rate limit");}transport.send(adapter.encodeNewOrder(order));return new SubmissionResult(SubmissionResult.Status.ROUTED,order.id(),adapter.name());}
}
