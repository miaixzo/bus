package org.aoju.bus.core.builder;

import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.utils.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * <p>
 * Assists in implementing {@link Diffable#diff(Object)} methods.
 * </p>
 * <p>
 * All non-static, non-transient fields (including inherited fields)
 * of the objects to diff are discovered using reflection and compared
 * for differences.
 * </p>
 *
 * <p>
 * To use this class, write code as follows:
 * </p>
 *
 * <pre>
 * public class Person implements Diffable&lt;Person&gt; {
 *   String name;
 *   int age;
 *   boolean smoker;
 *   ...
 *
 *   public DiffResult diff(Person obj) {
 *     // No need for null check, as NullPointerException correct if obj is null
 *     return new ReflectionDiffBuilder(this, obj, ToStringStyle.SHORT_PREFIX_STYLE)
 *       .build();
 *   }
 * }
 * </pre>
 *
 * <p>
 * The {@code ToStringStyle} passed to the constructor is embedded in the
 * returned {@code DiffResult} and influences the style of the
 * {@code DiffResult.toString()} method. This style choice can be overridden by
 * calling {@link DiffResult#toString(ToStringStyle)}.
 * </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ReflectionDiffBuilder implements Builder<DiffResult> {

    private final Object left;
    private final Object right;
    private final DiffBuilder diffBuilder;

    /**
     * <p>
     * Constructs a builder for the specified objects with the specified style.
     * </p>
     *
     * <p>
     * If {@code lhs == rhs} or {@code lhs.equals(rhs)} then the builder will
     * not evaluate any calls to {@code append(...)} and will return an empty
     * {@link DiffResult} when {@link #build()} is executed.
     * </p>
     *
     * @param <T>   type of the objects to diff
     * @param lhs   {@code this} object
     * @param rhs   the object to diff against
     * @param style the style will use when outputting the objects, {@code null}
     *              uses the default
     * @throws IllegalArgumentException if {@code lhs} or {@code rhs} is {@code null}
     */
    public <T> ReflectionDiffBuilder(final T lhs, final T rhs, final ToStringStyle style) {
        this.left = lhs;
        this.right = rhs;
        diffBuilder = new DiffBuilder(lhs, rhs, style);
    }

    @Override
    public DiffResult build() {
        if (left.equals(right)) {
            return diffBuilder.build();
        }

        appendFields(left.getClass());
        return diffBuilder.build();
    }

    private void appendFields(final Class<?> clazz) {
        for (final Field field : FieldUtils.getAllFields(clazz)) {
            if (accept(field)) {
                try {
                    diffBuilder.append(field.getName(), FieldUtils.readField(field, left, true),
                            FieldUtils.readField(field, right, true));
                } catch (final IllegalAccessException ex) {
                    //this can't happen. Would get a Security exception instead
                    //throw a runtime exception in case the impossible happens.
                    throw new InternalError("Unexpected IllegalAccessException: " + ex.getMessage());
                }
            }
        }
    }

    private boolean accept(final Field field) {
        if (field.getName().indexOf(Symbol.C_DOLLAR) != -1) {
            return false;
        }
        if (Modifier.isTransient(field.getModifiers())) {
            return false;
        }
        return !Modifier.isStatic(field.getModifiers());
    }

}
