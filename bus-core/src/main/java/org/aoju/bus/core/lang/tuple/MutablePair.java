package org.aoju.bus.core.lang.tuple;

/**
 * <p>A mutable pair consisting of two {@code Object} elements.</p>
 *
 * <p>Not #ThreadSafe#</p>
 *
 * @param <L> the left element type
 * @param <R> the right element type
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MutablePair<L, R> extends Pair<L, R> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 4954918890077093841L;

    /**
     * Left object
     */
    public L left;
    /**
     * Right object
     */
    public R right;

    /**
     * Create a new pair instance of two nulls.
     */
    public MutablePair() {
        super();
    }

    /**
     * Create a new pair instance.
     *
     * @param left  the left value, may be null
     * @param right the right value, may be null
     */
    public MutablePair(final L left, final R right) {
        super();
        this.left = left;
        this.right = right;
    }

    /**
     * <p>Obtains a mutable pair of two objects inferring the generic types.</p>
     *
     * <p>This factory allows the pair to be created using inference to
     * obtain the generic types.</p>
     *
     * @param <L>   the left element type
     * @param <R>   the right element type
     * @param left  the left element, may be null
     * @param right the right element, may be null
     * @return a pair formed from the two parameters, not null
     */
    public static <L, R> MutablePair<L, R> of(final L left, final R right) {
        return new MutablePair<>(left, right);
    }


    @Override
    public L getLeft() {
        return left;
    }

    /**
     * Sets the left element of the pair.
     *
     * @param left the new value of the left element, may be null
     */
    public void setLeft(final L left) {
        this.left = left;
    }


    @Override
    public R getRight() {
        return right;
    }

    /**
     * Sets the right element of the pair.
     *
     * @param right the new value of the right element, may be null
     */
    public void setRight(final R right) {
        this.right = right;
    }

    /**
     * Sets the {@code Map.Entry} value.
     * This sets the right element of the pair.
     *
     * @param value the right value to set, not null
     * @return the old value for the right element
     */
    @Override
    public R setValue(final R value) {
        final R result = getRight();
        setRight(value);
        return result;
    }

}
