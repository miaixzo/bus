package org.aoju.bus.core.builder;

/**
 * <p>{@code Diffable} classes can be compared with other objects
 * for differences. The {@link DiffResult} object retrieved can be queried
 * for a list of differences or printed using the {@link DiffResult#toString()}.</p>
 *
 * <p>The calculation of the differences is <i>consistent with equals</i> if
 * and only if {@code d1.equals(d2)} implies {@code d1.diff(d2) == ""}.
 * It is strongly recommended that implementations are consistent with equals
 * to avoid confusion. Note that {@code null} is not an instance of any class
 * and {@code d1.diff(null)} should throw a {@code NullPointerException}.</p>
 *
 * <p>
 * {@code Diffable} classes lend themselves well to unit testing, in which a
 * easily readable description of the differences between an anticipated result and
 * an actual result can be retrieved. For example:
 * </p>
 * <pre>
 * Assert.assertEquals(expected.diff(result), expected, result);
 * </pre>
 *
 * @param <T> the type of objects that this object may be differentiated against
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Diffable<T> {

    /**
     * <p>Retrieves a list of the differences between
     * this object and the supplied object.</p>
     *
     * @param obj the object to diff against, can be {@code null}
     * @return a list of differences
     * @throws NullPointerException if the specified object is {@code null}
     */
    DiffResult diff(T obj);

}
