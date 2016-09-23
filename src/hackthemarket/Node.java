package hackthemarket;

import java.io.Serializable;
import java.util.Objects;

public class Node implements Comparable<Node>, Serializable {

    private static final double NO_THRESHOLD = -2.0;
    private static final long serialVersionUID = 114L;

    private final int key;

    private final double threshold;

    private final Allele allele;

    public Node(int key, Allele allele) {
        this(key, NO_THRESHOLD, allele);
    }

    public Node(int key, double threshold,
            final Allele allele) {
        this.key = key;
        this.threshold = threshold;
        this.allele = allele;
    }

    public Node copy() {
        return new Node(key, threshold, allele);
    }

    public int key() {
        return key;
    }

    /**
     * @return the threshold signal of this node
     */
    public double threshold() {
        return threshold;
    }

    /**
     * @return the allele of this node
     */
    public Allele allele() {
        return allele;
    }

    @Override
    public int compareTo(Node node) {
        return key - node.key;
    }

    /**
     * @return a string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node: Key = ").append(key)
                .append(", Allele = ").append(allele)
                .append('\n');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            Node node = (Node) obj;
            return key == node.key;
        } else {
            return false;
        }
    }

    /**
     * @return a hash code
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.key;
        return hash;
    }

}
