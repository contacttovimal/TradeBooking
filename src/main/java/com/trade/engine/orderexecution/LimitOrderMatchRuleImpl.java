package com.trade.engine.orderexecution;

import com.trade.engine.order.SellOrder;
import com.trade.engine.order.BuyOrder;

public class LimitOrderMatchRuleImpl implements OrderMatchRule{

    @Override
    public boolean isOrderMatch(BuyOrder buyOrder, SellOrder sellOrder) {
        if (buyOrder != null && sellOrder != null &&
                buyOrder.getPrice() <= sellOrder.getPrice()) {
            return false;
        }
        return true;
    }
}
