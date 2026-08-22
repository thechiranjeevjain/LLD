package com.chiranjeev.lld.gateway;
public record SubmissionResult(Status status,String orderId,String detail) { public enum Status{ROUTED,QUEUED_DISCONNECTED,QUEUED_THROTTLED} }
