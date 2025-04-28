# UDP Payment Simulator (JavaFX Version) - FINAL

## Project Structure
- SenderApp.java : JavaFX Sender Client
- ReceiverApp.java : JavaFX Receiver Client
- README.md : Project Instructions

## Features
- Open/Close Payment Channel Simulation
- Payment Request & Acknowledgment over UDP
- Colored Buttons for Actions
- Packet Loss Simulation (20% chance)
- Latency Measurement
- Transaction history saved to history.csv
- Auto-scrollable GUI
- Professional JavaFX GUI

## How to Run
1. Make sure Java 11+ is installed.
2. Download JavaFX SDK from [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/).

3. Compile both files:
```bash
javac --module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls SenderApp.java ReceiverApp.java
```

4. First, run the Receiver:
```bash
java --module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls ReceiverApp
```

5. Then run the Sender:
```bash
java --module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls SenderApp
```

6. Open Channel ➔ Send Payment ➔ Close Channel ➔ See real-time logs and CSV updates.

## Notes
- Receiver IP: use 127.0.0.1 (localhost) for testing on the same computer.
- Across devices: use the actual IP address of the receiver.
