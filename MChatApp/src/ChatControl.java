
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatControl implements Initializable {

    static ObservableList<Node> children_message;
    static ObservableList<Node> children_user;
    static int msgIndex = 0;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField txtInput;
    @FXML
    private Button btnSend;
    @FXML
    private VBox messagePane;
    @FXML
    private VBox UserPane;
    @FXML
    private Label UsernameLabel;

    private static Client client;

    private static boolean turnToUser;

    private static String targetUser;

    public  static TransModel model = new TransModel();

    @FXML
    public void initialize(URL location,ResourceBundle resources) {
        try{
            client = new Client(new Socket("127.0.0.1", 1234));
            print("Connecting to %s:%d\n", "127.0.0.1", 1234);
        }catch (IOException e) {
            System.err.println("Connect failure!!!");
            e.printStackTrace();
        }

        model.textProperty().addListener((obs, oldText, newText) -> UsernameLabel.setText(newText));
        model.textProperty().addListener((obs, oldText, newText) -> {
            client.login(newText.substring(7));
        });

        children_message = messagePane.getChildren();
        children_user = UserPane.getChildren();

        messagePane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                scrollPane.setVvalue((Double) newValue);
            }
        });

        client.receiveMessageFromServer(messagePane);

        if(turnToUser){
            txtInput.setText("To "+targetUser);
        }

        btnSend.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String messageToSend = txtInput.getText();
                if (messageToSend != null && !"".equals(messageToSend)) {
                    children_message.add(messageNode(messageToSend,  true));
                    if (turnToUser){
                        client.sendMessageToServer("@"+targetUser.substring(targetUser.length() - 1)+":"+messageToSend);
                        turnToUser = false;
                    }else {
                        client.sendMessageToServer(messageToSend);
                    }
                    txtInput.clear();
                }

            }
        });
    }

    private static Node messageNode(String text, boolean alignToRight) {
        HBox box = new HBox();
        box.paddingProperty().setValue(new Insets(10, 10, 10, 10));

        if (alignToRight)
            box.setAlignment(Pos.BASELINE_RIGHT);
        javafx.scene.control.Label label = new Label(text);
        label.setWrapText(true);
        box.getChildren().add(label);
        return box;
    }

    private static Node userNode(String username) throws IOException{
        HBox box = new HBox();
        box.paddingProperty().setValue(new Insets(5, 5, 5, 5));

        javafx.scene.control.Label label = new Label(username);
        label.setWrapText(true);
        box.getChildren().add(label);
        box.setOnMouseClicked(event -> {
                turnToUser = true;
                targetUser = username;
        });
        return box;
    }

    public static void displayReceiveMessage(String receiveText) {
        Platform.runLater(() -> {
            children_message.add(messageNode(receiveText, false));
        });
    }

    public static void displayUsername(String newUser) throws IOException {
        Platform.runLater(() -> {
            try{
                children_user.add(userNode(newUser));
            }catch (IOException e){
                e.printStackTrace();
            }
        });
    }

    public static void print(String str, Object... o) {
        System.out.printf(str, o);
    }
}
