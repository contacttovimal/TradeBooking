package com.trade.engine;

import com.trade.engine.booking.RicNotRegisteredException;
import com.trade.engine.booking.TradeBookingEngine;
import com.trade.engine.booking.TradeBookingEngineImpl;
import com.trade.engine.order.BaseOrder;
import com.trade.engine.order.BuyOrder;
import com.trade.engine.order.CounterParty;
import com.trade.engine.order.SellOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test class to check the scenario described here
 * <p>
 * Id   Side    Time   Qty   Price   Qty    Time   Side
 * --- ------ ------- ----- ------- ----- ------- ------
 * 3                        20.30   200   09:05   SELL
 * 1                        20.30   100   09:01   SELL
 * 2                        20.25   100   09:03   SELL
 * 5   BUY    09:08   200   20.20
 * 4   BUY    09:06   100   20.15
 * 6   BUY    09:09   200   20.15
 */
public class TradeEngineTest {

    static final String RIC_N225 = ".N225";
    static final String RIC_SPX = ".SPX";

    static final SellOrder SELL_ORDER_1 = new SellOrder(RIC_N225, 1, 20.30, 100);
    static final SellOrder SELL_ORDER_2 = new SellOrder(RIC_N225, 1, 20.25, 100);
    static final SellOrder SELL_ORDER_3 = new SellOrder(RIC_N225, 2, 20.30,100 );

    static final BuyOrder BUY_ORDER_1 = new BuyOrder(RIC_N225, 1, 20.15,100 );
    static final BuyOrder BUY_ORDER_2 = new BuyOrder(RIC_N225, 2, 20.20, 100);
    static final BuyOrder BUY_ORDER_3 = new BuyOrder(RIC_N225, 2, 20.15,100 );
    static final TradeBookingEngine tradeBookingEngine = new TradeBookingEngineImpl(4);

    @Before
    public void setup(){
        tradeBookingEngine.start();
        tradeBookingEngine.register(RIC_N225);
    }

    @Test(expected = RicNotRegisteredException.class)
    public void postOrderWithoutRegistrationTest() throws Exception {
        tradeBookingEngine.submitOrder(RIC_SPX,SELL_ORDER_1);
    }

    @Test
    public void checkSellOrders() throws Exception {
        tradeBookingEngine.submitOrder(RIC_N225,SELL_ORDER_1);
        tradeBookingEngine.submitOrder(RIC_N225,SELL_ORDER_2);
        tradeBookingEngine.submitOrder(RIC_N225,SELL_ORDER_3);
        Assert.assertEquals("expected order o2", SELL_ORDER_2, tradeBookingEngine.popSellOrder(RIC_N225));
        tradeBookingEngine.popSellOrder(RIC_N225);tradeBookingEngine.popSellOrder(RIC_N225);
        Assert.assertNull("No Orders", tradeBookingEngine.peekSellOrder(RIC_N225));
    }

    @Test
    public void checkBuyOrders() throws Exception {
        tradeBookingEngine.submitOrder(RIC_N225,BUY_ORDER_1);
        tradeBookingEngine.submitOrder(RIC_N225,BUY_ORDER_2);
        tradeBookingEngine.submitOrder(RIC_N225,BUY_ORDER_3);
        Assert.assertEquals("expected order o2", BUY_ORDER_2, tradeBookingEngine.popBuyOrder(RIC_N225));
        tradeBookingEngine.popBuyOrder(RIC_N225);tradeBookingEngine.popBuyOrder(RIC_N225);
        Assert.assertNull("No Orders", tradeBookingEngine.peekBuyOrder(RIC_N225));
    }

    @Test
    public void inValidateOrderTest() throws Exception {
        CounterParty cp1 = Mockito.spy(new CounterParty("CP1"));
        SellOrder INVALID_LOT_SIZE_SELL_ORDER = new SellOrder(RIC_N225, 1, 20.30, 10);
        tradeBookingEngine.submitOrder(RIC_N225,INVALID_LOT_SIZE_SELL_ORDER);
        Assert.assertEquals("Rejected", INVALID_LOT_SIZE_SELL_ORDER.getOrderStatus(), BaseOrder.OrderStatus.REJECTED);
        Mockito.verify(cp1,
                Mockito.atLeast(0)).orderStatusCallBack(Mockito.any(),Mockito.anyInt(),Mockito.anyString());

        SellOrder INVALID_LOT_SIZE_WITH_CP_SELL_ORDER_1 = new SellOrder(RIC_N225, 1, 20.30, 10);
        INVALID_LOT_SIZE_WITH_CP_SELL_ORDER_1.setCounterParty(cp1);
        tradeBookingEngine.submitOrder(RIC_N225,INVALID_LOT_SIZE_WITH_CP_SELL_ORDER_1);
        Assert.assertEquals("Rejected", INVALID_LOT_SIZE_WITH_CP_SELL_ORDER_1.getOrderStatus(), BaseOrder.OrderStatus.REJECTED);
        Mockito.verify(cp1,
                Mockito.atLeast(1)).orderStatusCallBack(Mockito.any(),Mockito.anyInt(),Mockito.anyString());

    }

    @Test
    public void removeOrderTest() throws Exception {
        SellOrder CANCEL_SELL_ORDER_4 = Mockito.spy(new SellOrder(RIC_N225, 1, 30.30, 100));
        tradeBookingEngine.submitOrder(RIC_N225,CANCEL_SELL_ORDER_4);
        Assert.assertEquals("NEW", CANCEL_SELL_ORDER_4.getOrderStatus(), BaseOrder.OrderStatus.NEW);
        tradeBookingEngine.cancelOrder(CANCEL_SELL_ORDER_4);
        Mockito.verify(CANCEL_SELL_ORDER_4,Mockito.atLeast(1)).setOrderStatus(Mockito.any());
        Assert.assertEquals("Order cancelled ", CANCEL_SELL_ORDER_4.getOrderStatus(), BaseOrder.OrderStatus.CANCELLED);


    }
	
	
/* scenario: place order "buy 300 shares at 20.35"
 * result:
    100 shares at 20.25 (order-2)
    100 shares at 20.30 (order-1)
    100 shares at 20.30 (order-3)

Id   Side    Time   Qty   Price   Qty    Time   Side  
--- ------ ------- ----- ------- ----- ------- ------
3                        20.30   150   09:05   SELL
5   BUY    09:08   200   20.20
4   BUY    09:06   100   20.15
6   BUY    09:09   200   20.15
 */

    @Test
    public void checkAddMatchingOrder() throws Exception {
        CounterParty cp1 = new CounterParty("CP1");
        CounterParty cp2 = new CounterParty("CP2");
        CounterParty cp3 = new CounterParty("CP3");
        CounterParty cp4 = new CounterParty("CP4");

        tradeBookingEngine.submitOrder(RIC_N225, SELL_ORDER_1,cp1);//100
        tradeBookingEngine.submitOrder(RIC_N225, SELL_ORDER_2,cp2);
        tradeBookingEngine.submitOrder(RIC_N225,SELL_ORDER_3,cp3);
        tradeBookingEngine.submitOrder(RIC_N225, BUY_ORDER_1,cp3);
        tradeBookingEngine.submitOrder(RIC_N225, BUY_ORDER_3,cp4);

        // posting new matching buy order
        BuyOrder matchingOrder = Mockito.spy(new BuyOrder(RIC_N225, 3, 20.35, 100));
        tradeBookingEngine.submitOrder(RIC_N225, matchingOrder,cp4);

        Mockito.verify(matchingOrder,Mockito.timeout(1000).atLeast(3)).setOrderStatus(Mockito.any());
        Assert.assertEquals("Sell order 2 should be executed", SELL_ORDER_2.getOrderStatus(), BaseOrder.OrderStatus.EXECUTED);
        Assert.assertEquals("Sell order 1 should be executed", SELL_ORDER_1.getOrderStatus(), BaseOrder.OrderStatus.EXECUTED);
        Assert.assertEquals("Sell order 3 should be pending", SELL_ORDER_3.getOrderStatus(), BaseOrder.OrderStatus.PENDING);
        Assert.assertEquals("Buy order 4 should be executed", matchingOrder.getOrderStatus(), BaseOrder.OrderStatus.EXECUTED);

    }

    /*@After
    public void shutDown(){
        tradeBookingEngine.shutdown();
    }*/

}
