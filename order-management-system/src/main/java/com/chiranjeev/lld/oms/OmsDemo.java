package com.chiranjeev.lld.oms;
public final class OmsDemo { private OmsDemo(){} public static void main(String[] args){OrderManagementSystem oms=new OrderManagementSystem();oms.submit("O1","AAPL",10_000,100);oms.acknowledgeNew("O1");oms.recordFill("O1",40,"E1");oms.requestCancel("O1");System.out.println(oms.acknowledgeCancel("O1"));oms.events().forEach(System.out::println);} }
