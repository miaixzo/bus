package org.aoju.bus.core.reflect;

import java.lang.reflect.Type;

/**
 * Generalization of "has a type."
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @see TypeLiteral
 * @since JDK 1.8
 */
public interface Typed<T> {

    /**
     * Get the {@link Type} represented by this entity.
     *
     * @return Type
     */
    Type getType();
}
