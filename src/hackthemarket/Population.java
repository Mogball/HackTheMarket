package hackthemarket;

import java.io.Serializable;
import static java.lang.Math.floor;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Population implements Serializable {

    private static final double EXTINCTION_THRESHOLD = 0.05;

    private static final int MAX_STAGNATION = 100;
    private static final long serialVersionUID = 13241235L;

    private static void insert(Genome g, List<Species> species) {
        for (Species s : species) {
            if (s.insert(g)) {
                return;
            }
        }
        species.add(new Species(g));
    }

    private final GeneticAlgorithm GA;

    private final List<Species> species;

    private final int size;

    public Population(int size, Genome seed, GeneticAlgorithm GA) {
        this.size = size;
        this.GA = GA;
        species = Util.newList();
        for (int n = 0; n < size; n++) {
            insert(seed.copy(), species);
        }
    }

    private Population(int size, List<Species> species,
            GeneticAlgorithm GA) {
        this.size = size;
        this.species = species;
        this.GA = GA;
    }

    /**
     * @return an iterator over all of the genomes in this population
     */
    public List<Genome> getGenomeList() {
        List<Genome> genomes = Util.newList();
        for (Species s : species) {
            Iterator<Genome> iter = s.getGenomes();
            while (iter.hasNext()) {
                genomes.add(iter.next());
            }
        }
        Collections.sort(genomes);
        return genomes;
    }

    public Iterator<Genome> getGenomes() {
        return getGenomeList().iterator();
    }

    public Population evolve() {
        // Compute the hierarchy of each species
        for (Species s : species) {
            s.hierarchy();
        }
        // Remove stagnating species
        List<Species> stagnating = Util.newList();
        Collections.sort(species);
        int numSpecies = species.size();
        for (int s = numSpecies / 2; s < numSpecies; s++) {
            Species specie = species.get(s);
            if (specie.staleness() >= MAX_STAGNATION) {
                stagnating.add(specie);
            }
        }
        species.removeAll(stagnating);
        // Remove the weaker members of each species
        for (Species s : species) {
            s.cull();
        }
        // Remove species that are too weak
        double total = 0.0;
        for (Species s : species) {
            total += s.average();
        }
        List<Species> extinct = Util.newList();
        for (Species s : species) {
            if (s.average() / total < EXTINCTION_THRESHOLD) {
                total -= s.average();
                extinct.add(s);
            }
        }
        species.removeAll(extinct);
        // Breed new offspring for each species based on their fitness
        List<Species> posterity = Util.newList();
        List<Genome> offspring = Util.newList();
        int N = 0;
        for (Species s : species) {
            int n = (int) floor(s.average() / total * size) - 1;
            N += n + 1;
            for (int i = 0; i < n; i++) {
                offspring.add(GA.breed(s, s));
            }
            posterity.add(new Species(s.elite()));
        }
        for (Genome child : offspring) {
            insert(child, posterity);
        }
        // Fill the rest of the population with hybrids
        List<Genome> hybrids = Util.newList();
        Bound B = new Bound(0, species.size() - 1);
        while (N < size) {
            N++;
            Species s1 = species.get(B.randInt());
            Species s2 = species.get(B.randInt());
            hybrids.add(GA.breed(s1, s2));
        }
        for (Genome hybrid : hybrids) {
            insert(hybrid, posterity);
        }
        // Create the new population
        return new Population(size, posterity, GA);
    }

}
