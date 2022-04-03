package com.trade.engine.order;


public class CounterParty {
	
	private String name;

	public CounterParty(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	

	/**
	 * Callback function for order execution updates.
	 */
	public void orderStatusCallBack(BaseOrder order, int quantity,String message) {
		System.out.println(message + " - executed qty : " + quantity + " - status :" + order.getOrderStatus());
	}

	@Override
	public String toString() {
		return "CounterParty{" +
				"name='" + name + '\'' +
				'}';
	}
}
