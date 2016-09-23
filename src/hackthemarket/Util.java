package hackthemarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * A utility class.
 *
 * @author Jeff Niu
 */
public class Util {

    public static <E> List<E> newList() {
        return new ArrayList<>();
    }

    public static <E> Set<E> newSet() {
        return new HashSet<>();
    }

    public static <K, V> Map<K, V> newMap() {
        return new HashMap<>();
    }

    public static double rand(final double lower, final double upper) {
        final double range = upper - lower;
        return range * Math.random() + lower;
    }

    public static int randInt(final int lower, final int upper) {
        Random rng = new Random();
        return rng.nextInt((upper - lower) + 1) + lower;
    }

    /**
     * Private constructor.
     */
    private Util() {
    }

}
