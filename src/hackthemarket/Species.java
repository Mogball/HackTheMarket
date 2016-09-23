package hackthemarket;

import java.io.Serializable;
import static java.lang.Math.ceil;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Species implements Comparable<Species>, Serializable {

    private static final double TOURNAMENT_RATIO = 0.10;
    private static final long serialVersionUID = 1425123L;

    private final List<Genome> genomes;

    private double topFitness;

    private double averageFitness;

    private int staleness;

    public Species(Genome g) {
        genomes = Util.newList();
        genomes.add(g);
        topFitness = Double.NaN;
        averageFitness = Double.NaN;
        staleness = 0;
    }

    /**
     * @return the staleness
     */
    public int staleness() {
        return staleness;
    }

    /**
     * @return the average fitness
     */
    public double average() {
        return averageFitness;
    }

    /**
     * @return a copy of the species champion
     */
    public Genome elite() {
        return genomes.get(0).copy();
    }

    /**
     * @return an iterator of all the genomes in this species
     */
    public Iterator<Genome> getGenomes() {
        return genomes.iterator();
    }

    private void computeAverageFitness() {
        double sum = 0.0;
        for (Genome g : genomes) {
            sum += g.getFitness();
        }
        averageFitness = sum / genomes.size();
    }

    public boolean insert(Genome g) {
        if (GeneticAlgorithm.sameSpecies(genomes.get(0), g)) {
            return genomes.add(g);
        } else {
            return false;
        }
    }

    public void hierarchy() {
        Collections.sort(genomes);
        double fitness = genomes.get(0).getFitness();
        if (fitness > topFitness) {
            topFitness = fitness;
            staleness = 0;
        } else {
            staleness++;
        }
        computeAverageFitness();
    }

    public void cull() {
        List<Genome> weak = Util.newList();
        double k = genomes.get(0).getFitness() < 0 ? 2 : 0.5;
        for (Genome g : genomes) {
            if (g.getFitness() < averageFitness * k) {
                weak.add(g);
            }
        }
        genomes.removeAll(weak);
        computeAverageFitness();
    }

    public Genome select() {
        int size = (int) ceil(genomes.size() * TOURNAMENT_RATIO);
        List<Genome> tourn = Util.newList();
        Bound B = new Bound(0, genomes.size() - 1);
        Iterator<Integer> indices = B.randString(size, false);
        while (indices.hasNext()) {
            int index = indices.next();
            tourn.add(genomes.get(index));
        }
        Collections.sort(tourn);
        return tourn.get(0);
    }

    @Override
    public int compareTo(Species s) {
        double v = s.topFitness - topFitness;
        if (v == 0) {
            return 0;
        } else if (v < 0) {
            return -1;
        } else {
            return 1;
        }
    }

}
