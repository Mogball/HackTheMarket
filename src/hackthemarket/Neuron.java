package hackthemarket;

import java.util.List;

public class Neuron {

    public static final double sigmoid(final double z) {
        return 2 / (1 + Math.exp(-4.9 * z)) - 1;
    }

    private final int key;

    private final double threshold;

    private final List<Connection> connections;

    private double forward;

    private double buffer;

    private double receive;

    public Neuron(int key, double threshold) {
        this.key = key;
        this.threshold = threshold;
        forward = 0.0f;
        buffer = 0.0f;
        receive = 0.0f;
        connections = Util.newList();
    }

    public void attach(Connection conn) {
        connections.add(conn);
    }

    public void receive(double signal) {
        receive += signal;
    }

    public void flush() {
        buffer = receive;
        receive = 0.0f;
    }

    public void forward() {
        if (buffer > threshold) {
            forward = sigmoid(buffer);
        } else {
            forward = 0.0f;
        }
        for (Connection conn : connections) {
            conn.forward(forward);
        }
    }

    public double val() {
        return forward;
    }

    public int getKey() {
        return key;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Neuron: Key = ").append(key);
        sb.append(", R = ").append(String.format("%.2f", receive));
        sb.append(", B = ").append(String.format("%.2f", buffer));
        sb.append(", F = ").append(String.format("%.2f", forward));
        for (Connection conn : connections) {
            sb.append(", ").append(conn.toString());
        }
        sb.append('\n');
        return sb.toString();
    }

}
