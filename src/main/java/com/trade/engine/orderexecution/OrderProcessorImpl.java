package com.trade.engine.orderexecution;

import com.trade.engine.order.SellOrder;
import com.trade.engine.order.BaseOrder;
import com.trade.engine.order.BuyOrder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OrderProcessorImpl implements OrderProcessor {

    private String RIC;
    private PriorityBlockingQueue<BuyOrder> buyOrderQ;
    private PriorityBlockingQueue<SellOrder> sellOrderQ;
    private Lock orderProcessLock;
    private OrderMatchRule orderMatchRule;
    private boolean isActive;

    public OrderProcessorImpl(String RIC, OrderMatchRule orderMatchRule) {
        this.setRIC(RIC);
        this.orderMatchRule = orderMatchRule;
        buyOrderQ = new PriorityBlockingQueue<BuyOrder>(100);
        sellOrderQ = new PriorityBlockingQueue<SellOrder>(100);
        orderProcessLock = new ReentrantLock(true);
    }

    @Override
    public void setActive(boolean active){this.isActive=active;}

    public String getRIC() {
        return RIC;
    }

    @Override
    public void setRIC(String RIC) {
        this.RIC = RIC;
    }

    @Override
    public void submitOrder(BaseOrder order) {
        if (isActive()) {
            order.setOrderTime(LocalDate.now());
            if (!validateOrder(order)) {
                orderStatusUpdate(order, 0,  BaseOrder.OrderStatus.REJECTED);
                return;
            }
            if (order instanceof BuyOrder) {
                buyOrderQ.add((BuyOrder) order);
            } else {
                sellOrderQ.add((SellOrder) order);
            }
        }
    }

    public boolean removeOrder(BaseOrder order) {
        if (isActive()) {

            if (!isPendingOrder(order)) {
                return false;
            }
            if (order instanceof BuyOrder) {
                buyOrderQ.remove(order);
            } else {
                sellOrderQ.remove(order);
            }

            return true;

        }
        return false;
    }

    public boolean cancelOrder(BaseOrder order) {
        if (isActive()) {

            if (!isPendingOrder(order)) {
                return false;
            }
            if (order instanceof BuyOrder) {
                buyOrderQ.remove(order);
            } else {
                sellOrderQ.remove(order);
            }
            order.setOrderStatus(BaseOrder.OrderStatus.CANCELLED);
            return true;

        }
        return false;
    }

    @Override
    public boolean isPendingOrder(BaseOrder order) {
        if (isActive()) {
            if (order instanceof BuyOrder) {
                return buyOrderQ.contains(order);
            } else {
                return sellOrderQ.contains(order);
            }
        }
        return false;
    }

    @Override
    public boolean isOrderMatch() {
        if (isActive()) {
            if (buyOrderQ.size() == 0 || sellOrderQ.size() == 0) {
                return false;
            }
            BuyOrder buyOrder = buyOrderQ.peek();
            SellOrder sellOrder = sellOrderQ.peek();
            return isOrderMatch(buyOrder, sellOrder);
        }
        return false;
    }

    private boolean isOrderMatch(BuyOrder buyOrder, SellOrder sellOrder) {
        return orderMatchRule != null ? orderMatchRule.isOrderMatch(buyOrder, sellOrder) : false;
    }


    @Override
    public void executeMatchedOrder() {
        if (isActive()) {
            if (!isOrderMatch()) {
                return;
            }
            orderProcessLock.lock();
            try {
                BuyOrder buyOrder = buyOrderQ.peek();
                SellOrder sellOrder = sellOrderQ.peek();
                if (isOrderMatch(buyOrder, sellOrder)) {
                    processOrder(buyOrder, sellOrder);
                }
            } finally {
                orderProcessLock.unlock();
            }

        }

    }

    @Override
    public Set<BaseOrder> executeAllMatchedOrder() {
        if (isActive()) {
            if (!isOrderMatch()) {
                return Collections.emptySet();
            }
            orderProcessLock.lock();
            try {
                Set<BaseOrder> executedOrders = new HashSet<>();
                BuyOrder buyOrder = buyOrderQ.peek();
                SellOrder sellOrder = sellOrderQ.peek();
                while (isOrderMatch(buyOrder, sellOrder)) {
                    processOrder(buyOrder, sellOrder);
                    executedOrders.add(buyOrder);
                    executedOrders.add(sellOrder);
                    buyOrder = buyOrderQ.peek();
                    sellOrder = sellOrderQ.peek();
                }
                return executedOrders;
            } finally {
                orderProcessLock.unlock();
            }
        }
        return Collections.emptySet();
    }

    private void processOrder(BuyOrder buyOrder, SellOrder sellOrder) {
        //buy order
        int quantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
        orderStatusUpdate(sellOrder, quantity, BaseOrder.OrderStatus.PENDING);
        orderStatusUpdate(buyOrder, quantity,BaseOrder.OrderStatus.PENDING);
    }

    private void orderStatusUpdate(BaseOrder order, int quantity,  BaseOrder.OrderStatus status) {
        order.setOrderStatus(status);
        if (quantity == order.getQuantity()) {
            order.setOrderStatus(BaseOrder.OrderStatus.EXECUTED);
            removeOrder(order);
        } else {
            order.setQuantity(order.getQuantity() - quantity);
        }
        StringBuilder orderStatusMessage = new StringBuilder(" ORDER STATE: ").append(order);
        if (order.getCounterParty() != null) {
            order.getCounterParty().orderStatusCallBack(order, quantity, orderStatusMessage.toString());
        }
        ;
    }

    @Override
    public BuyOrder peekBuyOrder() {
        return buyOrderQ.peek();
    }

    public BuyOrder popBuyOrder() {
        BuyOrder buyOrder = buyOrderQ.poll();
        return buyOrder;
    }

    @Override
    public SellOrder peekSellOrder() {
        return sellOrderQ.peek();
    }

    public SellOrder popSellOrder() {
        SellOrder sellOrder = sellOrderQ.poll();
        return sellOrder;
    }

    @Override
    public void stop() {
        orderProcessLock.lock();
        isActive = false;
        sellOrderQ.clear();
        buyOrderQ.clear();
        orderProcessLock.unlock();
        System.out.println("Order procesor stopped and clear queue for :" + RIC);
    }

    private boolean validateOrder(BaseOrder order) {
        return order.lotSize == 100;
    }

    private boolean isActive() {
        if (!isActive) throw new OrderProcessorNotActiveException("Order Processor stopepd for RIC :" + this.RIC);
        return true;
    }
}
