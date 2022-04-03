package com.trade.engine.orderexecution;

public class OrderProcessorNotActiveException extends RuntimeException{
    public OrderProcessorNotActiveException(String errorMessage){
        super(errorMessage);
    }
}
