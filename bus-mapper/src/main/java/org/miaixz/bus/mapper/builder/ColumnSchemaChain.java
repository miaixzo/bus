/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT of OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.builder;

import java.util.List;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.FieldMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * A processing chain for column factories (ColumnSchemaBuilder), designed to be thread-safe and support singleton
 * instances (via lazy initialization of the next node). This class implements the **Chain of Responsibility pattern**
 * to sequentially process fields and determine their corresponding database column metadata.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ColumnSchemaChain implements ColumnSchemaBuilder.Chain {

    /**
     * The list of column factories (ColumnSchemaBuilder instances) that form the processing chain.
     */
    private final List<ColumnSchemaBuilder> factories;
    /**
     * The index of the current factory in the 'factories' list. This determines which factory the current chain node
     * will execute.
     */
    private final int index;
    /**
     * The next node in the processing chain. Declared as **volatile** and initialized lazily using **Double-Checked
     * Locking (DCL)** for thread safety.
     */
    private volatile ColumnSchemaChain next;

    /**
     * Constructs the head of a new column factory processing chain, starting from the first factory (index 0).
     *
     * @param factories The list of column factories (ColumnSchemaBuilder instances).
     */
    public ColumnSchemaChain(List<ColumnSchemaBuilder> factories) {
        // Initializes the chain starting at index 0.
        this(factories, 0);
    }

    /**
     * Private constructor to initialize a specific node in the processing chain.
     *
     * @param factories The list of all column factories.
     * @param index     The index of the current factory to be executed by this chain node.
     */
    private ColumnSchemaChain(List<ColumnSchemaBuilder> factories, int index) {
        this.factories = factories;
        this.index = index;
    }

    /**
     * Creates entity column information by invoking column factories sequentially in a chain.
     * <p>
     * It executes the factory at the current {@code index} and passes the reference to the next chain node for
     * continued processing. A factory should return an empty {@link Optional} if the field does not map to a column, or
     * if it decides not to handle the field.
     * </p>
     *
     * @param tableMeta The existing table metadata, which may be needed by the factory (e.g., to check for primary
     *                  keys).
     * @param fieldMeta The field metadata (FieldMeta) representing the field being analyzed.
     * @return An {@link Optional} containing a list of column information ({@link ColumnMeta}) for the entity field. An
     *         empty Optional is returned if the end of the chain is reached or if the field is determined not to be a
     *         column.
     */
    @Override
    public Optional<List<ColumnMeta>> createColumn(TableMeta tableMeta, FieldMeta fieldMeta) {
        // Check if the current index is within the bounds of the factories list.
        if (index < factories.size()) {

            // Lazy initialization of the next chain node using Double-Checked Locking (DCL) for thread safety.
            if (next == null) {
                // First check outside synchronization for performance optimization.
                synchronized (this) {
                    if (next == null) {
                        // Second check inside synchronization for thread-safe instantiation.
                        // Create the next chain node with the next index (index + 1).
                        next = new ColumnSchemaChain(factories, index + 1);
                    }
                }
            }

            // Execute the current factory at 'index' and pass the 'next' chain node as the Chain parameter.
            return factories.get(index).createColumn(tableMeta, fieldMeta, next);
        }

        // If the index reaches the size of the list, the chain is finished without a result.
        // Return Optional.empty() to indicate that no factory in the chain handled the field/column mapping.
        return Optional.empty();
    }

}
