package com.trade.engine.orderexecution;

import com.trade.engine.order.SellOrder;
import com.trade.engine.order.BuyOrder;

public interface OrderMatchRule {
     boolean isOrderMatch(BuyOrder buyOrder, SellOrder sellOrder) ;
}
