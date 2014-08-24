package org.cheddarmonk.util;

/**
 * Represents an ordered pair.
 * @param A The type of the first element of the pair.
 * @param B The type of the second element of the pair.
 * @specifier Peter Taylor
 * @implementer Peter Taylor
 */
public class Pair<A, B>
{
    /**
     * The first element of the pair.
     */
    public final A first;

    /**
     * The second element of the pair.
     */
    public final B second;

    /**
     * Constructs an ordered pair.
     * @param first The first element of the pair.
     * @param second The second element of the pair.
     */
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair)
        {
            Pair<?, ?> p = (Pair)obj;
            return equal(first, p.first) && equal(second, p.second);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return hash(first) ^ hash(second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    /**
     * Compares two objects for equality, handling nulls.
     * @param o1 The first object to compare.
     * @param o2 The second object to compare.
     * @return Whether or not the objects are equal.
     */
    private static boolean equal(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    /**
     * Finds the hash code of an object, handling nulls.
     * @param obj The object to hash.
     * @return The hash code of the object.
     */
    private static int hash(Object obj) {
        return obj == null ? 0 : obj.hashCode();
    }
}
