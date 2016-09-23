package hackthemarket;

public class Connection {

    private final Neuron target;

    private final double weight;

    public Connection(Neuron target, double weight) {
        this.target = target;
        this.weight = weight;
    }

    public void forward(double forward) {
        target.receive(weight * forward);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Conn: N = ").append(target.getKey())
                .append(", W = ").append(String.format("%.2f", weight));
        return sb.toString();
    }

}
