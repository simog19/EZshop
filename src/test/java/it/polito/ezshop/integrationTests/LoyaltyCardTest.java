package it.polito.ezshop.integrationTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.polito.ezshop.model.Customer;
import it.polito.ezshop.data.DataManager;
import it.polito.ezshop.model.LoyaltyCard;

public class LoyaltyCardTest {

    @Before
    @After
    public void cleanDatabase() {

        for (Customer c : DataManager.getInstance().getCustomers()) {
            DataManager.getInstance().deleteCustomer(c);
        }

        for (LoyaltyCard lc : DataManager.getInstance().getLoyaltyCards()) {
            DataManager.getInstance().deleteLoyaltyCard(lc);
        }

    }

    @Test
    public void testValidLoyaltyCard(){
        LoyaltyCard lc = new LoyaltyCard("4583542934", 7, null);
        assertEquals("4583542934", lc.getID());
        assertEquals(7, lc.getPoints().intValue());
        assertEquals(null, lc.getCustomer());
    }

    @Test
    public void testNullLoyaltyCardId(){
        assertThrows(IllegalArgumentException.class, () -> new LoyaltyCard(null, 7, null));
    }

    @Test
    public void testEmptyLoyaltyCardId(){
        assertThrows(IllegalArgumentException.class, () -> new LoyaltyCard("", 7, null));
        assertThrows(IllegalArgumentException.class, () -> new LoyaltyCard("    ", 7, null));
    }

    @Test
    public void testWrongLoyaltyCardId(){
        assertThrows(IllegalArgumentException.class, () -> new LoyaltyCard("132", 7, null));
        assertThrows(IllegalArgumentException.class, () -> new LoyaltyCard("a63odh", 7, null));
        assertThrows(IllegalArgumentException.class, () -> new LoyaltyCard("13649562465395632", 7, null));
    }

    @Test
    public void testValidLoyaltyCardCustomer(){
        LoyaltyCard lc = new LoyaltyCard("4583542934", 7, null);
        Customer c = new Customer(1, "we", lc);
        lc.addCustomer(c);
        assertEquals(lc.getCustomer(), c);
    }

    @Test
    public void testNegativeLoyaltyCardPoints(){
        assertThrows(IllegalArgumentException.class, () -> new LoyaltyCard("4583542934", -7, null));
    }

    @Test
    public void testOverMaxIntLoyaltyCardPoints(){ 
        LoyaltyCard lc = new LoyaltyCard("4583542934", Integer.MAX_VALUE, null);
        assertFalse(lc.addPoints(1));
    }

    @Test
    public void testAddPointsLoyaltyCard(){
        LoyaltyCard lc = new LoyaltyCard("4583542934", 7, null);
        assertTrue(lc.addPoints(10));
        assertEquals(lc.getPoints().intValue(), 17);
    }

    @Test
    public void testNegativeSumAddPointsLoyaltyCard(){
        LoyaltyCard lc = new LoyaltyCard("4583542934", 7, null);
        assertFalse(lc.addPoints(-10));
    }

    @Test
    public void testPositiveSumAddPointsLoyaltyCard(){
        LoyaltyCard lc = new LoyaltyCard("4583542934", 7, null);
        assertTrue(lc.addPoints(-5));
        assertTrue(lc.addPoints(-2));
        assertEquals(lc.getPoints().intValue(), 0);
    }

    @Test
    public void testNullAddPointsLoyaltyCard(){
        LoyaltyCard lc = new LoyaltyCard("4583542934", 7, null);
        assertFalse(lc.addPoints(null));
    }
}
