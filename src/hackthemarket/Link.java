package hackthemarket;

import java.io.Serializable;

public class Link implements Comparable<Link>, Serializable {

    private static final int NO_INNOVATION = -1;
    private static final long serialVersionUID = 1432L;

    private final int input;

    private final int output;

    private int innovation;

    private double weight;

    private boolean enabled;

    public Link(int input, int output, double weight) {
        this(input, output, weight, true);
    }

    public Link(int input, int output,
            double weight, boolean enabled) {
        this(input, output, NO_INNOVATION, weight, enabled);
    }

    public Link(int input, int output,
            int innovation, double weight,
            boolean enabled) {
        this.input = input;
        this.output = output;
        this.innovation = innovation;
        this.weight = weight;
        this.enabled = enabled;
    }

    public Link copy() {
        return new Link(input, output, innovation, weight, enabled);
    }

    /**
     * @return the input neuron index
     */
    public int in() {
        return input;
    }

    /**
     * @return the output neuron index
     */
    public int out() {
        return output;
    }

    /**
     * @return the innovation number
     */
    public int innov() {
        return innovation;
    }

    /**
     * @return the connection weight
     */
    public double weight() {
        return weight;
    }

    /**
     * @return whether the connection is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled whether this link is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @param weight the new connection weight
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setInnov(int innovation) {
        this.innovation = innovation;
    }

    @Override
    public int compareTo(Link link) {
        return link.innovation - innovation;
    }

    /**
     * @return a string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Link: Input = ").append(input)
                .append(", Output = ").append(output)
                .append(", Innovation = ").append(innovation)
                .append(", Weight = ").append(weight)
                .append(", Enabled = ").append(enabled)
                .append('\n');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Link) {
            Link link = (Link) obj;
            return input == link.input && output == link.output;
        } else {
            return false;
        }
    }

    /**
     * @return a hash code
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.input;
        hash = 29 * hash + this.output;
        return hash;
    }

}
