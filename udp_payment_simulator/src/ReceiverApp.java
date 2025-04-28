import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReceiverApp extends Application {

    private TextArea outputArea;
    private static final int LISTEN_PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Payment Receiver");

        outputArea = new TextArea();
        outputArea.setEditable(false);

        VBox vbox = new VBox(10, outputArea);
        vbox.setPadding(new Insets(15));

        Scene scene = new Scene(vbox, 400, 500);
        primaryStage.getIcons().add(new Image("https://upload.wikimedia.org/wikipedia/commons/4/46/Bitcoin.svg"));
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(this::listenForPayments).start();
    }

    private void listenForPayments() {
        try (DatagramSocket socket = new DatagramSocket(LISTEN_PORT)) {
            while (true) {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                String senderIP = packet.getAddress().getHostAddress();

                outputArea.appendText("[" + now() + "] Payment received from " + senderIP + ":\n" + received + "\n");

                // Prepare acknowledgment
                String ack = "{\"status\": \"Payment Confirmed\", \"timestamp\": \"" + now() + "\"}";
                byte[] ackBuffer = ack.getBytes(StandardCharsets.UTF_8);
                DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length, packet.getAddress(), packet.getPort());
                socket.send(ackPacket);

                outputArea.appendText("[" + now() + "] Acknowledgment sent.\n\n");
                outputArea.setScrollTop(Double.MAX_VALUE);
            }
        } catch (Exception ex) {
            outputArea.appendText("Error: " + ex.getMessage() + "\n");
        }
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
