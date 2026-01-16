package ci553.happyshop.client.customer;

import ci553.happyshop.utility.UIStyle;
import ci553.happyshop.utility.WindowBounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class QuantityErrorNotifier {
    public CustomerView cusView; //tracking the window of cusView

    private static int WIDTH = UIStyle.removeProNotifierWinWidth;
    private static int HEIGHT = UIStyle.removeProNotifierWinHeight;

    private Stage window; //window for ProductRemovalNotifier
    private Scene scene; // Scene for ProductRemovalNotifier
    private TextArea taRemoveMsg;// TextArea to display removal products messages

    // Create the Scene (only once)
    private void createScene() {
        Label laTitle = new Label("\u26A0 An error has been detected."); // ⚠️
        laTitle.setStyle(UIStyle.alertTitleLabelStyle);

        taRemoveMsg = new TextArea();
        taRemoveMsg.setEditable(false);
        taRemoveMsg.setWrapText(true);
        taRemoveMsg.setPrefHeight(80);
        taRemoveMsg.setStyle(UIStyle.alertContentTextAreaStyle);

        Label laCustomerAction = new Label(cutomerActionBuilder());
        laCustomerAction.setWrapText(true);
        laCustomerAction.setStyle(UIStyle.alertContentUserActionStyle);

        Button btnOk = new Button("Ok");
        btnOk.setStyle(UIStyle.alertBtnStyle);
        btnOk.setOnAction(e -> {
            window.close();
        });

        // HBox: Customer action Label + OK button
        HBox hbCustomerAction = new HBox(20, laCustomerAction, btnOk);
        hbCustomerAction.setAlignment(Pos.CENTER_LEFT);

        // Top level GridPane layout
        GridPane pane = new GridPane();
        pane.setHgap(5);
        pane.setVgap(5);

        pane.add(laTitle, 0, 0);
        pane.add(taRemoveMsg, 0, 1);
        pane.add(hbCustomerAction, 0, 2);
        pane.setStyle(UIStyle.rootStyleGray);

        scene = new Scene(pane, WIDTH, HEIGHT);
    }

    private String cutomerActionBuilder(){
        StringBuilder actions = new StringBuilder(" \u26A1 You can now: \n");
        actions.append("\u2022 Re-add the product to your order up to the mex amount\n");
        actions.append("\u2022 Cancel your order\n");
        actions.append("Thank you for understanding! \n");
        return  actions.toString();
    }

    // Create the window if not exists
    //also recreate a window if the user closed it with messages in it
    private void createWindow() {
        if (scene == null) {
            createScene();  //create scene if not exists
        }

        window = new Stage();
        window.initModality(Modality.NONE); //Optional: explicitly set as non-blocking, though this is the default
        window.setTitle("Quantity Error");
        window.setScene(scene);

        //get bounds of betterCustomer window which trigers the ProductRemovalNotifier
        // so that we can put the ProductRemovalNotifier at a suitable position
        WindowBounds bounds = cusView.getWindowBounds();
        window.setX(bounds.x + bounds.width -WIDTH -10); // Position to the right of warehouse window
        window.setY(bounds.y + bounds.height / 2 + 40);
        window.show();
    }

    // Show remove product message
    public void showRemovalMsg(String removalMsg) {
        if (window ==null ||!window.isShowing() ) {
            createWindow(); // create window if not exists
        }

        taRemoveMsg.setText(removalMsg); // Update the error message
        window.toFront(); // Bring the window to the front if it's already open
    }

    /**
     * Closes the ProductRemovalNotifier window.
     * The purpose of this method is to provide a way to close the notifier window from outside this class,
     * when it is no longer needed (e.g., after canceling or submitting order while the window is still showing
     * from previous).
     */
    public void closeNotifierWindow() {
        if (window != null && window.isShowing()) {
            window.close();
        }
    }
}
