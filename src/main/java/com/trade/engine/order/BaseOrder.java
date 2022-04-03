package com.trade.engine.order;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


public abstract class BaseOrder implements Comparable<BaseOrder> {

    private UUID id;
    protected double price;
    protected volatile int quantity;
    protected volatile OrderType orderType;
    private CounterParty counterParty;
    protected String RIC;
    protected LocalDate orderTime;
    protected int lotSize = 1;
    protected AtomicReference<OrderStatus> orderStatus;

    public BaseOrder(String RIC, int quantity, double price, int lotSize) {
        id = UUID.randomUUID();
        this.RIC = RIC;
        this.orderTime = LocalDate.now();
        this.lotSize = lotSize;
        orderStatus = new AtomicReference<>(OrderStatus.NEW);
        setQuantity(quantity * lotSize);
        setPrice(price);
    }

    public UUID getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderType getType() {
        return orderType;
    }

    public CounterParty getCounterParty() {
        return counterParty;
    }

    public String getRIC() {
        return RIC;
    }

    public LocalDate getOrderTime() {
        return orderTime;
    }

    public OrderStatus getOrderStatus(){return orderStatus.get();}

    public void setPrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Order price must be positive");
        }

        this.price = price;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Order quantity can not be <= 0 ");
        }
        this.quantity = quantity;
    }

    public void setCounterParty(CounterParty counterParty) {
        this.counterParty = counterParty;
    }

    public void setRIC(String RIC) {
        this.RIC = RIC;
    }

    public void setOrderTime(LocalDate orderTime) {
        this.orderTime = orderTime;
    }

    public void setOrderStatus(OrderStatus status) {
        this.orderStatus.set(status);
    }

    @Override
    public String toString() {
        String counterPartyName = counterParty == null ? "NA" : counterParty.getName();
        String orderType = this.orderType == OrderType.BUY ? orderType = "BUY" : "SELL";
        StringBuilder toString = new StringBuilder();
        toString.append("[").append(counterPartyName).append(",").append(RIC).append(",")
                .append(orderType).append(",").append(quantity).append(",").append(price).append(",")
                .append(orderStatus).append(",")
                .append(id).append("]");
        return toString.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof BaseOrder) {
            UUID thatOrder = ((BaseOrder) obj).getId();
            return id.equals(thatOrder);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }


    public static enum OrderType {BUY, SELL};

    public static enum OrderStatus {EXECUTED, REJECTED, PENDING, NEW,CANCELLED};

}
