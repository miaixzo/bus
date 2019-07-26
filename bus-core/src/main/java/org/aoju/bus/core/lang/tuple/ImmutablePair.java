package org.aoju.bus.core.lang.tuple;

/**
 * <p>An immutable pair consisting of two {@code Object} elements.</p>
 *
 * <p>Although the implementation is immutable, there is no restriction on the objects
 * that may be stored. If mutable objects are stored in the pair, then the pair
 * itself effectively becomes mutable. The class is also {@code final}, so a subclass
 * can not add undesirable behaviour.</p>
 *
 * <p>#ThreadSafe# if both paired objects are thread-safe</p>
 *
 * @param <L> the left element type
 * @param <R> the right element type
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class ImmutablePair<L, R> extends Pair<L, R> {

    /**
     * An immutable pair of nulls.
     */
    private static final ImmutablePair NULL = of(null, null);
    /**
     * Left object
     */
    public final L left;
    /**
     * Right object
     */
    public final R right;

    /**
     * Create a new pair instance.
     *
     * @param left  the left value, may be null
     * @param right the right value, may be null
     */
    public ImmutablePair(final L left, final R right) {
        super();
        this.left = left;
        this.right = right;
    }

    /**
     * Returns an immutable pair of nulls.
     *
     * @param <L> the left element of this pair. Value is {@code null}.
     * @param <R> the right element of this pair. Value is {@code null}.
     * @return an immutable pair of nulls.
     * @since 3.6
     */
    public static <L, R> ImmutablePair<L, R> nullPair() {
        return NULL;
    }

    /**
     * <p>Obtains an immutable pair of two objects inferring the generic types.</p>
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
    public static <L, R> ImmutablePair<L, R> of(final L left, final R right) {
        return new ImmutablePair<>(left, right);
    }

    @Override
    public L getLeft() {
        return left;
    }

    @Override
    public R getRight() {
        return right;
    }

    /**
     * <p>Throws {@code UnsupportedOperationException}.</p>
     *
     * <p>This pair is immutable, so this operation is not supported.</p>
     *
     * @param value the value to set
     * @return never
     * @throws UnsupportedOperationException as this operation is not supported
     */
    @Override
    public R setValue(final R value) {
        throw new UnsupportedOperationException();
    }

}
