package hackthemarket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Bound implements Serializable {

    private static final long serialVersionUID = 134141L;

    public final double lower, upper;

    public Bound(double lower, double upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public double rand() {
        return Math.random() * (upper - lower) + lower;
    }

    public int randInt() {
        Random rng = new Random();
        return rng.nextInt((int) (upper - lower) + 1) + (int) lower;
    }

    public Iterator<Integer> randString(int size, boolean repeat) {
        if (repeat) {
            List<Integer> values = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                values.add(randInt());
            }
            return values.iterator();
        } else {
            Set<Integer> values = new LinkedHashSet<>(size);
            while (values.size() < size) {
                values.add(randInt());
            }
            return values.iterator();
        }
    }

}
