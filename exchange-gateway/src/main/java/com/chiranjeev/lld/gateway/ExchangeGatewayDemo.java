package com.chiranjeev.lld.gateway;
import java.time.Instant;
public final class ExchangeGatewayDemo {private ExchangeGatewayDemo(){}public static void main(String[] args){ExchangeGateway g=new ExchangeGateway(new FixProtocolAdapter(),new TokenBucketRateLimiter(10,10,Instant::now),System.out::println,System.out::println);g.onConnected();System.out.println(g.submit(new InternalOrder("O1","AAPL",InternalOrder.Side.BUY,100,10_050)));g.onVenueMessage("35=8|11=O1|39=0|");}}
