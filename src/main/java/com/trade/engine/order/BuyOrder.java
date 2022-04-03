package com.trade.engine.order;


public class BuyOrder extends BaseOrder {

    public BuyOrder(String RIC, int quantity, double price, int lotSize) {
        super(RIC, quantity, price, lotSize);
        orderType = OrderType.BUY;
    }

    public int compareTo(BaseOrder o) {
        int diff = Double.compare(o.getPrice(), getPrice());

        if (diff != 0) {
            return diff;
        }

        diff = getOrderTime().compareTo(o.getOrderTime());

        if (diff != 0) {
            return diff;
        }
        diff = getQuantity() < o.getQuantity() ? -1 : 1;
        return diff;
    }
}
