package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.exceptions.ExcessiveOrderQuantityException;
import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.catalogue.exceptions.UnderMinimumPaymentException;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.utility.ProductListFormatter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

/**
 * TODO
 * You can either directly modify the CustomerModel class to implement the required tasks,
 * or create a subclass of CustomerModel and override specific methods where appropriate.
 */
public class CustomerModel {
    public CustomerView cusView;
    public DatabaseRW databaseRW; //Interface type, not specific implementation
                                  //Benefits: Flexibility: Easily change the database implementation.



    private Product theProduct =null; // product found from search
    private ArrayList<Product> trolley =  new ArrayList<>(); // a list of products in trolley

    // Four UI elements to be passed to CustomerView for display updates.
    private String imageName = "imageHolder.jpg";                // Image to show in product preview (Search Page)
    private String displayLaSearchResult = "No Product was searched yet"; // Label showing search result message (Search Page)
    private String displayTaTrolley = "";                                // Text area content showing current trolley items (Trolley Page)
    private String displayTaReceipt = "";

    //variables for the error messages for customers
    public RemoveProductNotifier removeProductNotifier;// Text area content showing receipt after checkout (Receipt Page)
    public MinPayNotifer minPayNotifier;
    public QuantityErrorNotifier quanitityErrorNotifier;
    private String ErrorMessage = "";


    public CustomerModel() {}

    //SELECT productID, description, image, unitPrice,inStock quantity
    void search() throws SQLException {
        String productId = cusView.tfId.getText().trim();
        if(!productId.isEmpty()){
            theProduct = databaseRW.searchByProductId(productId); //search database
            if(theProduct != null && theProduct.getStockQuantity()>0){
                double unitPrice = theProduct.getUnitPrice();
                String description = theProduct.getProductDescription();
                int stock = theProduct.getStockQuantity();

                String baseInfo = String.format("Product_Id: %s\n%s,\nPrice: £%.2f", productId, description, unitPrice);
                String quantityInfo = stock < 100 ? String.format("\n%d units left.", stock) : "";
                displayLaSearchResult = baseInfo + quantityInfo;
                System.out.println(displayLaSearchResult);
            }
            else{
                theProduct=null;
                displayLaSearchResult = "No Product was found with ID " + productId;
                System.out.println("No Product was found with ID " + productId);
            }
        }else{
            theProduct=null;
            displayLaSearchResult = "Please type ProductID";
            System.out.println("Please type ProductID.");
        }
        updateView();
    }

    void addToTrolley(){
        if(theProduct!= null){
            MakingOrganisedTrolley(); //organizes the list call here
            displayTaTrolley = ProductListFormatter.buildString(trolley); //build a String for trolley so that we can show it
        }
        else{
            displayLaSearchResult = "Please search for an available product before adding it to the trolley";
            System.out.println("must search and get an available product before add to trolley");
        }
        displayTaReceipt=""; // Clear receipt to switch back to trolleyPage (receipt shows only when not empty)
        updateView();
    }

    // This is the code for ensuring that the trolley remains organized by ID
    void MakingOrganisedTrolley(){
        for(Product t:trolley){ //for loop that goes through the products in trolley
            if(t.getProductId().equals(theProduct.getProductId())){ // if statement comparing ID of new item to those in the list
                t.setOrderedQuantity(t.getOrderedQuantity()+ theProduct.getOrderedQuantity());//if true increment by 1
                return; //end method
            }
        }
        Product tNew= new Product(theProduct.getProductId(), // code for adding new product to trolley
                theProduct.getProductDescription(),
                theProduct.getProductImageName(),
                theProduct.getUnitPrice(),
                theProduct.getStockQuantity());
        trolley.add(tNew);

        Collections.sort(trolley, Comparator.comparing(Product::getProductId));
// code for sorting the items in the trolley using overiding


    }
    //The checkOut method was broken up into an method that calls an serious of sub methods to make
    //debugging and manging easier!

    void checkOut() throws IOException, SQLException {
        if(!trolley.isEmpty()){
            ValidateCheckout();
        }
        else{
            displayTaTrolley = "Your trolley is empty";
            System.out.println("Your trolley is empty");
        }
        updateView();
    }

    /**
     * Groups products by their productId to optimize database queries and updates.
     * By grouping products, we can check the stock for a given `productId` once, rather than repeatedly
     */
    private ArrayList<Product> groupProductsById(ArrayList<Product> proList) {
        Map<String, Product> grouped = new HashMap<>();
        for (Product p : proList) {
            String id = p.getProductId();
            if (grouped.containsKey(id)) {
                Product existing = grouped.get(id);
                existing.setOrderedQuantity(existing.getOrderedQuantity() + p.getOrderedQuantity());
            } else {
                // Make a shallow copy to avoid modifying the original
                grouped.put(id,new Product(p.getProductId(),p.getProductDescription(),
                        p.getProductImageName(),p.getUnitPrice(),p.getStockQuantity()));
            }
        }
        return new ArrayList<>(grouped.values());
    }

    void cancel(){
        trolley.clear();
        displayTaTrolley="";
        updateView();
    }
    void closeReceipt(){
        displayTaReceipt="";
    }

    void updateView() {
        if(theProduct != null){
            imageName = theProduct.getProductImageName();
            String relativeImageUrl = StorageLocation.imageFolder +imageName; //relative file path, eg images/0001.jpg
            // Get the full absolute path to the image
            Path imageFullPath = Paths.get(relativeImageUrl).toAbsolutePath();
            imageName = imageFullPath.toUri().toString(); //get the image full Uri then convert to String
            System.out.println("Image absolute path: " + imageFullPath); // Debugging to ensure path is correct
        }
        else{
            imageName = "imageHolder.jpg";
        }
        cusView.update(imageName, displayLaSearchResult, displayTaTrolley,displayTaReceipt);
    }
     // extra notes:
     //Path.toUri(): Converts a Path object (a file or a directory path) to a URI object.
     //File.toURI(): Converts a File object (a file on the filesystem) to a URI object

    //All Methods below this comment are created by me for the CustomerModel Class




    // this method calls in all the validation methods
    public void ValidateCheckout() throws IOException, SQLException {
        ValidateCheckOutMinTotal();
        ArrayList<Product> TooManyOrderedProducts = databaseRW.ReduceStockTo50(trolley);
        ValidateQuanitity(TooManyOrderedProducts);
        ValidationCheckOutComplete(TooManyOrderedProducts);
    }

    public void ValidateCheckOutStock(ArrayList<Product> insufficientProducts) throws IOException, SQLException {
        if(!insufficientProducts.isEmpty()){
            StringBuilder errorMsg = new StringBuilder();
            for (Product p : insufficientProducts) {
                errorMsg.append("\u2022 " + p.getProductId()).append(", ")
                        .append(p.getProductDescription()).append(" (Only ")
                        .append(p.getStockQuantity()).append(" available, ")
                        .append(p.getOrderedQuantity()).append(" requested)\n");
            }
            theProduct = null;
            // for loop that removes items from the trolley
            for (Product p : insufficientProducts) {
                trolley.remove(p);
            }
            // updating the visual trolley
            displayTaTrolley = ProductListFormatter.buildString(trolley);
            //Printing the error message
            ErrorMessage = errorMsg.toString();
            removeProductNotifier.showRemovalMsg(ErrorMessage);
        }
    }


    public void ValidationCheckOutComplete( ArrayList<Product> TooManyOrderedProducts) throws IOException, SQLException {
        if(TooManyOrderedProducts.isEmpty() && !ValidateCheckOutMinTotal() && !ValidateQuanitity(TooManyOrderedProducts)){
            OrderHub orderHub =OrderHub.getOrderHub();
            Order theOrder = orderHub.newOrder(trolley);
            trolley.clear();
            displayTaTrolley ="";
            displayTaReceipt = String.format(
                    "Order_ID: %s\nOrdered_Date_Time: %s\n%s",
                    theOrder.getOrderId(),
                    theOrder.getOrderedDateTime(),
                    ProductListFormatter.buildString(theOrder.getProductList())
            );
            System.out.println(displayTaReceipt);
        }

    }
//exception handling for less than £5
    public boolean ValidateCheckOutMinTotal() throws IOException, SQLException {
        double totalprice = 0; // sets total price of trolley to 0
        for(Product t:trolley){ //loop through items in trolley
            int OrderQuantity = t.getOrderedQuantity();
            totalprice = totalprice + t.getUnitPrice() * OrderQuantity; //calculate total trolley price
        }
        try{ //try catch statement
            if(totalprice < 5){
                throw new UnderMinimumPaymentException("Less than £5"); //throws to system
            }
        } catch (UnderMinimumPaymentException e) {
            minPayNotifier.showRemovalMsg("Under minimum payment amount"); // used on pop up notifier
            return true;

        }
        return false;

    }
    //method for checking quantity of products both greater than 50nand not enough stock
    public boolean ValidateQuanitity(ArrayList<Product> TooManyOrderedProducts) throws IOException, SQLException {
        if(!TooManyOrderedProducts.isEmpty()){ // check array list isn't empty
            for(Product t:TooManyOrderedProducts){ // loops though list
                try{ //first try catch
                    if(t.getOrderedQuantity() >= 50){ // check greater than 50
                        throw new ExcessiveOrderQuantityException("Excessive order quantity");
                    }
                }
                catch(ExcessiveOrderQuantityException e){
                    StringBuilder errorMsg = new StringBuilder();
                    for (Product p : TooManyOrderedProducts) {
                        errorMsg.append("\u2022 " + p.getProductId()).append(", ")
                                .append(p.getProductDescription()).append("")
                                .append(p.getOrderedQuantity()).append("requested)")
                                .append(" Only up to 50 allowed to be ordered\n");
                    }
                    //build error messagefor any products greater than 50
                    theProduct = null;
                    for (Product p : TooManyOrderedProducts) {
                        trolley.remove(p);
                    }//removes them

                    displayTaTrolley = ProductListFormatter.buildString(trolley);
                    //Printing the error message
                    ErrorMessage = errorMsg.toString();
                    quanitityErrorNotifier.showRemovalMsg(ErrorMessage);
                    return true;
                }
                try{//less than 50
                    if(t.getOrderedQuantity() < 50){
                        throw new ExcessiveOrderQuantityException("All ordered quantity under 50");
                    }
                }
                catch(ExcessiveOrderQuantityException e){
                    StringBuilder errorMsg = new StringBuilder();
                    for (Product p : TooManyOrderedProducts) {
                        errorMsg.append("\u2022 " + p.getProductId()).append(", ")
                                .append(p.getProductDescription()).append(" (Only ")
                                .append(p.getStockQuantity()).append(" available, ")
                                .append(p.getOrderedQuantity()).append(" requested)\n");
                    }
                    //builds error message for products ordered greater than the ones in stock
                    theProduct = null;
                    // for loop that removes items from the trolley
                    for (Product p : TooManyOrderedProducts) {
                        trolley.remove(p);
                    }
                    // updating the visual trolley
                    displayTaTrolley = ProductListFormatter.buildString(trolley);
                    //Printing the error message
                    ErrorMessage = errorMsg.toString();
                    removeProductNotifier.showRemovalMsg(ErrorMessage);
                    return true;
                }
            }
        }
        return false;

        //Initial try catch I used
        /*try{
            if(!TooManyOrderedProducts.isEmpty()){
                throw new ExcessiveOrderQuantityException("Excessive order quantity");
            }
        } catch (ExcessiveOrderQuantityException e){
            StringBuilder errorMsg = new StringBuilder();
            for (Product p : TooManyOrderedProducts) {
                errorMsg.append("\u2022 " + p.getProductId()).append(", ")
                        .append(p.getProductDescription()).append("")
                        .append(p.getOrderedQuantity()).append("requested)")
                        .append(" Only up to 50 allowed to be ordered\n");
            }
            theProduct = null;
            for (Product p : TooManyOrderedProducts) {
                trolley.remove(p);
            }

            displayTaTrolley = ProductListFormatter.buildString(trolley);
            //Printing the error message
            ErrorMessage = errorMsg.toString();
            removeProductNotifier.showRemovalMsg(ErrorMessage);
            return true;
        }
        return false;*/


    }




    //for test only
    public ArrayList<Product> getTrolley() {
        return trolley;
    }
}















