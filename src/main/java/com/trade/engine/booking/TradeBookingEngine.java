package com.trade.engine.booking;

import com.trade.engine.order.BaseOrder;
import com.trade.engine.order.BuyOrder;
import com.trade.engine.order.CounterParty;
import com.trade.engine.order.SellOrder;

public interface TradeBookingEngine {
    void start();
    boolean register(String RIC);
    boolean unregister(String RIC);
    void shutdown();
    void submitOrder(String RIC, BaseOrder order);

    void submitOrder(String RIC, BaseOrder order, CounterParty counterParty);

    boolean isPendingOrder(BaseOrder order);

    BuyOrder peekBuyOrder(String RIC);

    SellOrder peekSellOrder(String RIC);

    public boolean cancelOrder(BaseOrder order);

    public BuyOrder popBuyOrder(String RIC) ;

    public SellOrder popSellOrder(String RIC) ;



}
