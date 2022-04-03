package com.trade.engine.order;

public class InvalidOrderException extends RuntimeException{
    public InvalidOrderException(String errorMessage){
        super(errorMessage);
    }
}
