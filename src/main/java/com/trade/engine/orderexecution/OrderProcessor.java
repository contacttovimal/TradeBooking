package com.trade.engine.orderexecution;

import com.trade.engine.order.SellOrder;
import com.trade.engine.order.BaseOrder;
import com.trade.engine.order.BuyOrder;

import java.util.Set;

public interface OrderProcessor {
    void setRIC(String RIC);

    void submitOrder(BaseOrder order);

    boolean isPendingOrder(BaseOrder order);

    boolean isOrderMatch();

    void executeMatchedOrder();

    Set<BaseOrder> executeAllMatchedOrder();

    BuyOrder peekBuyOrder();

    SellOrder peekSellOrder();

    public boolean removeOrder(BaseOrder order);

    public boolean cancelOrder(BaseOrder order);

    public BuyOrder popBuyOrder() ;

    public SellOrder popSellOrder() ;

    public void stop();

    public void setActive(boolean active);
}
