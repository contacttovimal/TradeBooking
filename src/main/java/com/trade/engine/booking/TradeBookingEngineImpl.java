package com.trade.engine.booking;

import com.trade.engine.order.*;
import com.trade.engine.orderexecution.LimitOrderMatchRuleImpl;
import com.trade.engine.orderexecution.OrderProcessor;
import com.trade.engine.orderexecution.OrderProcessorImpl;

import java.util.*;
import java.util.concurrent.*;

public class TradeBookingEngineImpl implements TradeBookingEngine {
    private Map<String, OrderProcessor> orderProcessorByRIC = new ConcurrentHashMap<>();
    private Map<String, ForkJoinTask> submittedTaskByRIC = new ConcurrentHashMap<>();
    private int parallism;
    private ForkJoinPool orderProcessorPool;


    public TradeBookingEngineImpl(int parallism) {
        this.parallism = parallism;
    }

    private class OrderProcessorTask extends RecursiveAction {
        private String RIC;
        private OrderProcessor orderProcessor;
        private volatile boolean isRunning = false;

        public OrderProcessorTask(String RIC, OrderProcessor orderProcessor) {
            this.RIC = RIC;
            this.orderProcessor = orderProcessor;
        }

        @Override
        protected void compute() {
            System.out.println("start order processing for:"+RIC);
            while (true) {
                try {
                    Set<BaseOrder> executedOrders = this.orderProcessor.executeAllMatchedOrder();
                    if (executedOrders.size() > 0) {
                        System.out.println("end order processing for :" + this.RIC + ", executedOrder : " + executedOrders);
                    }
                    Thread.currentThread().sleep(1);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }


    @Override
    public void start() {
        try {
            orderProcessorPool = new ForkJoinPool(this.parallism);
            Timer timer = new Timer("TradBookingProcessor");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    orderProcessorByRIC.forEach((RIC, orderProcessorL) -> {
                        startProcessor(RIC,orderProcessorL);
                    });
                }
            },0,(100));

        } catch (Exception exception) {
            System.out.println("error in starting tradebooking engine..");
            exception.printStackTrace();
        }

    }

    @Override
    public boolean register(String RIC) {
        if (orderProcessorByRIC.containsKey(RIC)) {
            System.out.println("Already registered : " + RIC);
            return false;
        }
        OrderProcessor orderProcessor = new OrderProcessorImpl(RIC, new LimitOrderMatchRuleImpl());
        orderProcessorByRIC.putIfAbsent(RIC, orderProcessor);
        startProcessor(RIC,orderProcessor);
        return true;
    }

    @Override
    public boolean unregister(String RIC) {
        if (!orderProcessorByRIC.containsKey(RIC)) {
            System.out.println("RIC not registered : " + RIC);
            return false;
        }
        OrderProcessor orderProcessor = orderProcessorByRIC.get(RIC);
        System.out.println("Stopping order processor on unregister event for : " + RIC);
        orderProcessor.stop();
        return true;
    }

    @Override
    public void shutdown() {
        try {
            System.out.println("shutting down trading engine...waiting for 30 sec..");
            orderProcessorPool.awaitTermination((30 * 1000), TimeUnit.MILLISECONDS);
            System.out.println("shutting down trading engine...pending task " + orderProcessorPool.getRunningThreadCount());
            orderProcessorPool.shutdown();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private boolean isRegistered(String RIC){
        return (orderProcessorByRIC.containsKey(RIC)) ;
    }

    @Override
    public void submitOrder(String RIC, BaseOrder order) {
        if(isRegistered(RIC)) {
            OrderProcessor orderProcessor = orderProcessorByRIC.get(RIC);
            orderProcessor.submitOrder(order);
        }else{
            throw new RicNotRegisteredException("RIC not registered.");
        }
    }

    @Override
    public void submitOrder(String RIC, BaseOrder order, CounterParty counterParty) {
        if(isRegistered(RIC)) {
            if (counterParty == null) {
                throw new InvalidOrderException("Counterparty can not be null.");
            }
            order.setCounterParty(counterParty);
            OrderProcessor orderProcessor = orderProcessorByRIC.get(RIC);
            orderProcessor.submitOrder(order);
        }else{
            throw new RicNotRegisteredException("RIC not registered.");
        }
    }

    @Override
    public boolean isPendingOrder(BaseOrder order) {
        if(isRegistered(order.getRIC())) {
            OrderProcessor orderProcessor = orderProcessorByRIC.get(order.getRIC());
            orderProcessor.isPendingOrder(order);
        }else{
            throw new RicNotRegisteredException("RIC not registered.");
        }
        return false;
    }



    @Override
    public BuyOrder peekBuyOrder(String RIC) {
        if(isRegistered(RIC)) {
            OrderProcessor orderProcessor = orderProcessorByRIC.get(RIC);
            return orderProcessor.peekBuyOrder();
        }else{
            throw new RicNotRegisteredException("RIC not registered.");
        }
    }

    @Override
    public SellOrder peekSellOrder(String RIC) {
        if(isRegistered(RIC)) {
            OrderProcessor orderProcessor = orderProcessorByRIC.get(RIC);
            return orderProcessor.peekSellOrder();
        }else{
            throw new RicNotRegisteredException("RIC not registered.");
        }
    }

    @Override
    public boolean cancelOrder(BaseOrder order) {
        if(isRegistered(order.getRIC())) {
            OrderProcessor orderProcessor = orderProcessorByRIC.get(order.getRIC());
            return orderProcessor.cancelOrder(order);
        }else{
            throw new RicNotRegisteredException("RIC not registered.");
        }
    }

    @Override
    public BuyOrder popBuyOrder(String RIC) {
        if(isRegistered(RIC)) {
            OrderProcessor orderProcessor = orderProcessorByRIC.get(RIC);
            return orderProcessor.popBuyOrder();
        }else{
            throw new RicNotRegisteredException("RIC not registered.");
        }
    }

    @Override
    public SellOrder popSellOrder(String RIC) {
        if(isRegistered(RIC)) {
            OrderProcessor orderProcessor = orderProcessorByRIC.get(RIC);
            return orderProcessor.popSellOrder();
        }else{
            throw new RicNotRegisteredException("RIC not registered.");
        }
    }

    private void startProcessor(String RIC, OrderProcessor orderProcessorL){
        submittedTaskByRIC.computeIfAbsent(RIC,s -> {
            orderProcessorL.setActive(true);
            return orderProcessorPool.submit(new OrderProcessorTask(RIC, orderProcessorL));
        });
    }

}
