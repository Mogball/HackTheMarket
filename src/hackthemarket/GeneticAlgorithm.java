package hackthemarket;

import java.io.Serializable;
import static java.lang.Math.random;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeneticAlgorithm implements Serializable {

    /* Constants */
    private static final double WEIGHT_MUTATION_CHANCE = 0.30;
    private static final double WEIGHT_PERTURB_CHANCE = 0.10;
    private static final double WEIGHT_PERTURB_SIZE = 1.0;
    private static final double WEIGHT_RANDOMIZATION_CHANCE = 0.10;
    private static final double LINK_MUTATION_CHANCE = 0.05;
    private static final double BIAS_MUTATION_CHANCE = 0.002;
    private static final double NODE_MUTATION_CHANCE = 0.01;

    private static final double CROSSOVER_DISABLE_CHANCE = 1.00;
    private static final double ENABLE_MUTATION_CHANCE = 0.10;
    private static final double DISABLE_MUTATION_CHANCE = 0.05;
    private static final double TOGGLE_MUTATION_CHANCE = 0.005;
    private static final double CROSSOVER_RATE = 0.80;

    private static final double N_LENIENCY = 20;
    private static final double Z_EXCESS = 1.0;
    private static final double Z_DISJOINT = 1.0;
    private static final double Z_WEIGHT = 0.4;
    private static final double DELTA_THRESHOLD = 4.0;

    private static final long serialVersionUID = 1134L;

    /* Helper Methods */
    private static Map<Integer, Link> linkMap(List<Link> links) {
        Map<Integer, Link> linkMap = Util.newMap();
        for (Link link : links) {
            linkMap.put(link.innov(), link);
        }
        return linkMap;
    }

    private static Set<Node> nodeSet(List<Node> nodes) {
        Set<Node> nodeSet = Util.newSet();
        for (Node node : nodes) {
            nodeSet.add(node);
        }
        return nodeSet;
    }

    private static int topInnovation(Genome g) {
        Collections.sort(g.links());
        return g.links().get(0).innov();
    }

    private static boolean containsLink(Genome g, Link check) {
        if (g.links().isEmpty()) {
            return false;
        }
        for (Link link : g.links()) {
            if (link.in() == check.in() && link.out() == check.out()) {
                return true;
            }
        }
        return false;
    }

    public static int randNode(Genome g, Allele exclude) {
        Node node;
        if (exclude.equals(Allele.Input)) {
            Bound B = new Bound(0, g.excludeInput.size() - 1);
            node = g.excludeInput.get(B.randInt());
        } else if (exclude.equals(Allele.Output)) {
            Bound B = new Bound(0, g.excludeOutput.size() - 1);
            node = g.excludeOutput.get(B.randInt());
        } else {
            Bound B = new Bound(0, g.nodes().size() - 1);
            node = g.nodes().get(B.randInt());
        }
        return node.key();
    }

    public static void enableLink(Link link, double chance) {
        if (random() < chance) {
            link.setEnabled(true);
        }
    }

    public static void disableLink(Link link, double chance) {
        if (random() < chance) {
            link.setEnabled(false);
        }
    }

    /* Speciation Operations */
    public static int countDisjoint(Genome g1, Genome g2) {

        if (g1.links().isEmpty() || g2.links().isEmpty()) {
            return 0;
        }

        List<Link> lessInnov;
        List<Link> moreInnov;
        if (topInnovation(g1) >= topInnovation(g2)) {
            lessInnov = g2.links();
            moreInnov = g1.links();
        } else {
            lessInnov = g1.links();
            moreInnov = g2.links();
        }

        Map<Integer, Link> linkMap = linkMap(moreInnov);
        int disjoint = 0;
        for (Link link : lessInnov) {
            if (linkMap.get(link.innov()) == null) {
                disjoint++;
            }
        }
        return disjoint;
    }

    public static int countExcess(Genome g1, Genome g2) {

        boolean empty1 = g1.links().isEmpty();
        boolean empty2 = g2.links().isEmpty();
        if (empty1 && empty2) {
            return 0;
        } else if (empty1) {
            return g2.links().size();
        } else if (empty2) {
            return g1.links().size();
        }
        int top1 = topInnovation(g1);
        int top2 = topInnovation(g2);
        int lessInnov;
        List<Link> moreInnov;
        if (top1 > top2) {
            lessInnov = top2;
            moreInnov = g1.links();
        } else if (top1 < top2) {
            lessInnov = top1;
            moreInnov = g2.links();
        } else {
            return 0;
        }
        int excess = 0;
        for (Link link : moreInnov) {
            if (link.innov() > lessInnov) {
                excess++;
            } else {
                break;
            }
        }
        return excess;
    }

    public static double wBar(Genome g1, Genome g2) {

        if (g1.links().isEmpty() || g2.links().isEmpty()) {
            return 0.0;
        }

        List<Link> linkList = g1.links();
        Map<Integer, Link> linkMap = linkMap(g2.links());

        double wBar = 0.0;
        int matching = 0;
        for (Link link1 : linkList) {
            Link link2 = linkMap.get(link1.innov());
            if (link2 != null) {
                wBar += Math.abs(link1.weight() - link2.weight());
                matching++;
            }
        }

        if (matching == 0) {
            return 0.0;
        }
        return wBar / matching;
    }

    public static double dissimilarity(Genome g1, Genome g2) {

        if (g1.links().isEmpty() && g2.links().isEmpty()) {
            return 0.0;
        } else if (g1.links().isEmpty() || g2.links().isEmpty()) {
            return 1.0;
        }

        Map<Integer, Link> linkMap = linkMap(g1.links());
        int genes = g1.links().size();
        int matching = 0;
        for (Link link : g2.links()) {
            if (linkMap.get(link.innov()) == null) {
                genes++;
            } else {
                matching++;
            }
        }
        double dissimilarity = 1.0 - (double) matching / genes;
        return dissimilarity;
    }

    public static boolean sameSpecies(Genome g1, Genome g2) {

        int maxSize = Math.max(g1.links().size(), g2.links().size());
        if (maxSize <= N_LENIENCY) {
            maxSize = 1;
        }
        double dDisjoint = Z_DISJOINT * countDisjoint(g1, g2) / maxSize;
        double dExcess = Z_EXCESS * countExcess(g1, g2) / maxSize;
        double dWeight = Z_WEIGHT * wBar(g1, g2);
        double delta = dDisjoint + dExcess + dWeight;
        return delta < DELTA_THRESHOLD;
    }

    /* Class Proper -- Genetic Operations */
    private int innovation;

    private Map<Integer, Map<Integer, Integer>> history;

    private final Bound W;

    public GeneticAlgorithm(Bound W) {
        this.W = W;
        innovation = -1;
        history = Util.newMap();
    }

    private int innovate(Link link) {

        Map<Integer, Integer> input = history.get(link.in());
        if (input == null) {

            innovation++;
            input = Util.newMap();
            input.put(link.out(), innovation);
            history.put(link.in(), input);
            return innovation;
        } else {
            Integer output = input.get(link.out());
            if (output == null) {

                innovation++;
                input.put(link.out(), innovation);
                return innovation;
            } else {

                return output;
            }
        }
    }

    public void innovate(List<Link> links) {
        for (Link link : links) {
            link.setInnov(innovate(link));
        }
    }

    public void clear() {
        history = Util.newMap();
    }

    public void reset() {
        innovation = -1;
    }

    public void pointMutate(Genome g) {
        if (g.links().isEmpty()) {
            return;
        }
        for (Link link : g.links()) {
            if (random() < WEIGHT_PERTURB_CHANCE) {
                double weight = link.weight();
                weight += WEIGHT_PERTURB_SIZE * (2 * random() - 1);
                link.setWeight(weight);
            }
            if (random() < WEIGHT_RANDOMIZATION_CHANCE) {
                link.setWeight(W.rand());
            }
        }
    }

    public void linkMutate(Genome g) {
        Link link = new Link(randNode(g, Allele.Output),
                randNode(g, Allele.Input), W.rand(), true);
        if (!containsLink(g, link)) {
            link.setInnov(innovate(link));
            g.links().add(link);
        }
    }

    public void biasMutate(Genome g) {
        Node bias = null;
        for (Node node : g.nodes()) {
            if (node.allele().equals(Allele.Bias)) {
                bias = node;
                break;
            }
        }
        if (bias != null) {
            Link link = new Link(bias.key(), randNode(g, Allele.Bias), W.rand(), true);
            if (!containsLink(g, link)) {
                link.setInnov(innovate(link));
                g.links().add(link);
            }
        }
    }

    public void nodeMutate(Genome g) {
        if (g.links().isEmpty()) {
            return;
        }
        Link link = g.links().get(Util.randInt(0, g.links().size() - 1));
        if (!link.isEnabled()) {
            return;
        }
        link.setEnabled(false);
        Node node = new Node(g.nodes().size(), Allele.Hidden);
        Link in = new Link(link.in(), node.key(), 1.0, true);
        Link out = new Link(node.key(), link.out(), link.weight(), true);
        in.setInnov(innovate(in));
        out.setInnov(innovate(out));
        g.links().add(in);
        g.links().add(out);
        g.nodes().add(node);
    }

    public void toggleMutate(Genome g) {
        for (Link link : g.links()) {
            if (link.isEnabled()) {
                disableLink(link, DISABLE_MUTATION_CHANCE);
            } else {
                enableLink(link, ENABLE_MUTATION_CHANCE);
            }
        }
    }

    public void mutate(Genome g) {
        double p;
        p = WEIGHT_MUTATION_CHANCE;
        while (p > 0) {
            if (random() < p) {
                pointMutate(g);
            }
            p--;
        }
        p = TOGGLE_MUTATION_CHANCE;
        while (p > 0) {
            if (random() < p) {
                toggleMutate(g);
            }
            p--;
        }
        p = LINK_MUTATION_CHANCE;
        while (p > 0) {
            if (random() < p) {
                linkMutate(g);
            }
            p--;
        }
        p = BIAS_MUTATION_CHANCE;
        while (p > 0) {
            if (random() < p) {
                biasMutate(g);
            }
            p--;
        }
        p = NODE_MUTATION_CHANCE;
        while (p > 0) {
            if (random() < p) {
                nodeMutate(g);
            }
            p--;
        }
    }

    public Genome crossover(Genome g1, Genome g2) {
        // Compile all of the nodes such that none repeat
        Set<Node> nodeSet = nodeSet(g1.nodes());
        for (Node node : g2.nodes()) {
            nodeSet.add(node);
        }
        List<Node> nodes = Util.newList();
        for (Node node : nodeSet) {
            nodes.add(node.copy());
        }
        // Crossover matching genes and inherit non-matching from fitter
        Genome fitter;
        Map<Integer, Link> linkMap;
        List<Link> links = Util.newList();
        if (g1.compareTo(g2) > 0) {
            fitter = g2;
            linkMap = linkMap(g1.links());
        } else {
            fitter = g1;
            linkMap = linkMap(g2.links());
        }
        for (Link link1 : fitter.links()) {
            Link link2 = linkMap.get(link1.innov());
            if (link2 != null) {
                Link link = random() < 0.5 ? link1.copy() : link2.copy();
                if (!link1.isEnabled() || !link2.isEnabled()) {
                    link.setEnabled(random() > CROSSOVER_DISABLE_CHANCE);
                }
                links.add(link);
            } else {
                links.add(link1.copy());
            }
        }
        // Return the genome
        return new Genome(nodes, links);
    }

    public Genome breed(Species s1, Species s2) {
        Genome g1 = s1.select();
        Genome g2 = s2.select();
        Genome g;
        if (!s1.equals(s2) || random() < CROSSOVER_RATE) {
            g = crossover(g1, g2);
        } else {
            g = random() < 0.5 ? g1.copy() : g2.copy();
        }
        mutate(g);
        return g;
    }

}
