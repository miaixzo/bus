package org.miaixz.bus.mapper.handler;

/**
 * Base class for handling multi-table conditions. Provides methods for processing SELECT, UPDATE, and DELETE statements
 * and appending conditions based on table metadata.
 *
 * @param <T> the type parameter for the mapper handler
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ConditionHandler<T> extends AbstractSqlHandler implements MapperHandler<T> {

}
