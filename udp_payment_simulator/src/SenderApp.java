import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class SenderApp extends Application {

    private TextField ipField;
    private TextField paymentIdField;
    private TextField amountField;
    private TextArea outputArea;
    private Label channelStatusLabel;
    private boolean channelOpen = false;
    private Random random = new Random();
    private static final int SERVER_PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Payment Sender");

        ipField = new TextField("127.0.0.1");
        paymentIdField = new TextField();
        amountField = new TextField();
        outputArea = new TextArea();
        outputArea.setEditable(false);

        Button openButton = new Button("Open Channel");
        openButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");

        Button sendButton = new Button("Send Payment");
        sendButton.setStyle("-fx-background-color: blue; -fx-text-fill: white;");

        Button closeButton = new Button("Close Channel");
        closeButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        openButton.setOnAction(e -> openChannel());
        sendButton.setOnAction(e -> sendPayment());
        closeButton.setOnAction(e -> closeChannel());

        channelStatusLabel = new Label("Channel Status: CLOSED");
        channelStatusLabel.setTextFill(Color.RED);

        HBox buttons = new HBox(10, openButton, sendButton, closeButton);
        VBox vbox = new VBox(10,
                new Label("Receiver IP:"), ipField,
                new Label("Payment ID:"), paymentIdField,
                new Label("Amount (BTC):"), amountField,
                channelStatusLabel,
                buttons,
                outputArea);
        vbox.setPadding(new Insets(15));

        Scene scene = new Scene(vbox, 450, 600);
        primaryStage.getIcons().add(new Image("https://upload.wikimedia.org/wikipedia/commons/4/46/Bitcoin.svg"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openChannel() {
        channelOpen = true;
        channelStatusLabel.setText("Channel Status: OPEN");
        channelStatusLabel.setTextFill(Color.GREEN);
        logOutput("Channel opened.");
        logHistory("CHANNEL OPEN", "-", "-", "-", "-", "Open");
    }

    private void closeChannel() {
        channelOpen = false;
        channelStatusLabel.setText("Channel Status: CLOSED");
        channelStatusLabel.setTextFill(Color.RED);
        logOutput("Channel closed.");
        logHistory("CHANNEL CLOSE", "-", "-", "-", "-", "Closed");
    }

    private void sendPayment() {
        if (!channelOpen) {
            logOutput("Cannot send payment. Channel is CLOSED.");
            return;
        }

        String ip = ipField.getText().trim();
        String paymentId = paymentIdField.getText().trim();
        String amount = amountField.getText().trim();

        if (ip.isEmpty() || paymentId.isEmpty() || amount.isEmpty()) {
            logOutput("Please fill all fields!");
            return;
        }

        try (DatagramSocket socket = new DatagramSocket()) {
            if (random.nextInt(5) == 0) { // 20% chance to simulate packet loss
                logOutput("Simulated packet loss. Payment not sent.");
                logHistory(paymentId, amount, now(), "-", "-", "Packet Loss (Not Sent)");
                return;
            }

            String sendTime = now();
            long startTime = System.currentTimeMillis();

            String message = "{\"payment_id\": \"" + paymentId + "\", \"amount\": \"" + amount + "\", \"timestamp\": \"" + sendTime + "\"}";

            byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
            InetAddress receiverAddress = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, SERVER_PORT);
            socket.send(packet);

            logOutput("Payment request sent!");

            socket.setSoTimeout(5000);
            byte[] ackBuffer = new byte[BUFFER_SIZE];
            DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
            socket.receive(ackPacket);
            String ackMessage = new String(ackPacket.getData(), 0, ackPacket.getLength(), StandardCharsets.UTF_8);

            long endTime = System.currentTimeMillis();
            long latency = endTime - startTime;

            logOutput("Acknowledgment received: " + ackMessage);
            logHistory(paymentId, amount, sendTime, now(), String.valueOf(latency), "Success");

        } catch (Exception ex) {
            logOutput("Error: " + ex.getMessage());
            logHistory(paymentId, amount, now(), "-", "-", "Timeout/Error");
        }
    }

    private void logOutput(String message) {
        outputArea.appendText("[" + now() + "] " + message + "\n");
        outputArea.setScrollTop(Double.MAX_VALUE);
    }

    private void logHistory(String paymentId, String amount, String sendTime, String ackTime, String latency, String status) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("history.csv", true))) {
            if (new java.io.File("history.csv").length() == 0) {
                writer.println("Payment ID,Amount (BTC),Sent Time,Acknowledgment Time,Latency (ms),Status");
            }
            writer.println(paymentId + "," + amount + "," + sendTime + "," + ackTime + "," + latency + "," + status);
        } catch (Exception e) {
            logOutput("Failed to log history: " + e.getMessage());
        }
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
