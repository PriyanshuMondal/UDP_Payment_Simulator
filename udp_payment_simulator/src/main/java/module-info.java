module com.example.udp_payment_simulator {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.udp_payment_simulator to javafx.fxml;
    exports com.example.udp_payment_simulator;
}