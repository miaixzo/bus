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
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.builder;

import java.util.List;

import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * A processing chain for entity class table factories, designed to be thread-safe and support singleton instances (via
 * lazy initialization of the next node). This class implements the Chain of Responsibility pattern for table schema
 * building.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TableSchemaChain implements TableSchemaBuilder.Chain {

    /**
     * The list of table factories (TableSchemaBuilder instances) that form the processing chain.
     */
    private final List<TableSchemaBuilder> factories;
    /**
     * The index of the current factory in the 'factories' list. This determines which factory the current chain node
     * will execute.
     */
    private final int index;
    /**
     * The next node in the processing chain. Declared as volatile and initialized lazily using Double-Checked Locking
     * (DCL) for thread safety.
     */
    private volatile TableSchemaChain next;

    /**
     * Constructs the head of a new table factory processing chain, starting from the first factory (index 0).
     *
     * @param factories The list of table factories (TableSchemaBuilder instances).
     */
    public TableSchemaChain(List<TableSchemaBuilder> factories) {
        // Initializes the chain starting at index 0.
        this(factories, 0);
    }

    /**
     * Private constructor to initialize a specific node in the processing chain.
     *
     * @param factories The list of all table factories.
     * @param index     The index of the current factory to be executed by this chain node.
     */
    private TableSchemaChain(List<TableSchemaBuilder> factories, int index) {
        this.factories = factories;
        this.index = index;
    }

    /**
     * Creates entity table information by invoking table factories sequentially in a chain.
     * <p>
     * It executes the factory at the current {@code index} and passes the reference to the next chain node for
     * continued processing.
     * </p>
     *
     * @param entityClass The entity class type for which the table metadata is being requested.
     * @return The entity table metadata (TableMeta) if a factory successfully handles it, or {@code null} if the end of
     *         the chain is reached without processing.
     */
    @Override
    public TableMeta createTable(Class<?> entityClass) {
        // Check if the current index is within the bounds of the factories list.
        if (index < factories.size()) {
            // Lazy initialization of the next chain node using Double-Checked Locking (DCL)
            if (next == null) {
                // First check outside synchronization for performance
                synchronized (this) {
                    if (next == null) {
                        // Second check inside synchronization for thread safety
                        // Create the next chain node with the next index (index + 1)
                        next = new TableSchemaChain(factories, index + 1);
                    }
                }
            }
            // Execute the current factory and pass the 'next' chain node as the Chain parameter.
            return factories.get(index).createTable(entityClass, next);
        }

        // If the index reaches the size of the list, the chain is finished.
        // Return null to indicate that no factory in the chain handled the entity class.
        return null;
    }

}
