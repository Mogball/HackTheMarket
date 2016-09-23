package hackthemarket;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Genome implements Comparable<Genome>, Serializable {

    private final List<Node> nodes;
    final List<Node> excludeInput;
    final List<Node> excludeOutput;

    private final List<Link> links;

    private double fitness;

    public Genome(Genome base) {
        Genome genome = base.copy();
        nodes = genome.nodes();
        links = genome.links();
        fitness = Double.NaN;

        excludeInput = Util.newList();
        excludeOutput = Util.newList();
        for (Node node : nodes) {
            if (node.allele().equals(Allele.Input)) {
                excludeOutput.add(node);
            } else if (node.allele().equals(Allele.Output)) {
                excludeInput.add(node);
            } else {
                excludeInput.add(node);
                excludeOutput.add(node);
            }
        }
    }

    public Genome(List<Node> nodes, List<Link> links) {
        this.nodes = nodes;
        this.links = links;
        fitness = Double.NaN;

        excludeInput = Util.newList();
        excludeOutput = Util.newList();
        for (Node node : nodes) {
            if (node.allele().equals(Allele.Input)) {
                excludeOutput.add(node);
            } else if (node.allele().equals(Allele.Output)) {
                excludeInput.add(node);
            } else {
                excludeInput.add(node);
                excludeOutput.add(node);
            }
        }
    }

    public Genome copy() {
        List<Node> newNodes = Util.newList();
        for (Node node : nodes) {
            newNodes.add(node.copy());
        }
        List<Link> newLinks = Util.newList();
        for (Link link : links) {
            newLinks.add(link.copy());
        }
        return new Genome(newNodes, newLinks);
    }

    public List<Node> nodes() {
        Collections.sort(nodes);
        return nodes;
    }

    public List<Link> links() {
        Collections.sort(links);
        return links;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Genome:").append('\n');
        for (final Node node : nodes) {
            sb.append(node.toString());
        }
        for (final Link link : links) {
            sb.append(link.toString());
        }
        return sb.toString();
    }

    @Override
    public int compareTo(Genome g) {
        final double v = g.getFitness() - getFitness();
        if (v < 0) {
            return -1;
        } else if (v > 0) {
            return 1;
        } else {
            return 0;
        }
    }

}
