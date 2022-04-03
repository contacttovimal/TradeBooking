package com.trade.engine.order;


import com.trade.engine.order.BaseOrder;

public class SellOrder extends BaseOrder {
	
	public SellOrder(String symbol, int quantity, double price, int lotSize) {
		super(symbol, quantity, price, lotSize);
		orderType = OrderType.SELL;
	}
	public int compareTo(BaseOrder o) {
		int diff =  Double.compare(getPrice() , o.getPrice());

		if(diff != 0.0d) {
			return diff;
		}

		diff = getOrderTime().compareTo(o.getOrderTime());

		if(diff != 0){
			return diff;
		}

		return getQuantity() < o.getQuantity()?-1:1;
	}

}
