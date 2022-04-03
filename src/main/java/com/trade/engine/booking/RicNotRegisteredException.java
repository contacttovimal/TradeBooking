package com.trade.engine.booking;

public class RicNotRegisteredException extends RuntimeException{
    public RicNotRegisteredException(String errorMessage){
        super(errorMessage);
    }
}
