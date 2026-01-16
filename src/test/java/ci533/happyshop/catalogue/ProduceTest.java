package ci553.happyshop.catalogue;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ProduceTest {

    //All tests below this comment are for the Product class
    //This tests that the Product class constructor correctly assigns all provided values to it's
    //corrosponding class fields

    @Test
    void constructor_testFields() {
        Product p = new Product("0001", "Red apple", "0001.jpg", 0.35, 50);

        assertEquals("0001", p.getProductId());
        assertEquals("Red apple", p.getProductDescription());
        assertEquals("0001.jpg", p.getProductImageName());
        assertEquals(0.35, p.getUnitPrice());
        assertEquals(50, p.getStockQuantity());

    }
    //Tests that the default stock quantity is set to 1 automatically when a new product is created

    @Test
    void orderedQuantity_testToOne() {
        Product p = new Product("0002", "Banana", "0002.jpg", 0.20, 100);
        assertEquals(1, p.getOrderedQuantity());
    }

//Tests that the setOrderedQuantity method correctly updates the quantity value
    //stored in the product object
    @Test
    void setOrderedQuantity_testQuantity() {
        Product p = new Product("0003", "Orange", "0003.jpg", 0.50, 25);

        p.setOrderedQuantity(10);

        assertEquals(10, p.getOrderedQuantity());
    }
//
     //tests that the compareTo method found in the Product Class sorts object products correctly in
    //ascending order based on product ID
    @Test
    void compareTo_TestAscending() {
        Product p3 = new Product("0003", "Orange", "0003.jpg", 0.50, 25);
        Product p1 = new Product("0001", "Apple", "0001.jpg", 0.35, 50);
        Product p2 = new Product("0002", "Banana", "0002.jpg", 0.20, 100);

        List<Product> products = new ArrayList<>();
        products.add(p3);
        products.add(p1);
        products.add(p2);

        Collections.sort(products);

        assertEquals("0001", products.get(0).getProductId());
        assertEquals("0002", products.get(1).getProductId());
        assertEquals("0003", products.get(2).getProductId());
    }

    // Tests that the compareTo method returns zero when two Product objects have the same product ID.

    @Test
    void compareTo_Test0() {
        Product a = new Product("0001", "Apple A", "0001.jpg", 0.35, 50);
        Product b = new Product("0001", "Apple B", "0001.jpg", 0.40, 10);

        assertEquals(0, a.compareTo(b));
    }

    //Tests that the toString method returns the correct formatted output including product ID,
    // unit price, stock quantity and description.
//
    @Test
    void toString_TestFormatt() {
        Product p = new Product("0001", "Red apple", "0001.jpg", 2.5, 12);

        String expected = "Id: 0001, £2.50/uint, stock: 12 \nRed apple";
        assertEquals(expected, p.toString());
    }
    // Tests that the unit price is correctly rounded and formatted
    // to two decimal places in the toString output.

    @Test
    void toString_TestTwoDecimal() {
        Product p = new Product("0099", "Test item", "0099.jpg", 1.999, 3);

        assertTrue(p.toString().contains("£2.00/uint"));
    }
}
