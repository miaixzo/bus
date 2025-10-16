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
 * A processing chain for entity class table factories, designed to be thread-safe and support singleton instances.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TableSchemaChain implements TableSchemaBuilder.Chain {

    /**
     * The list of table factories.
     */
    private final List<TableSchemaBuilder> factories;

    /**
     * The next node in the processing chain.
     */
    private final TableSchemaChain next;

    /**
     * The index of the current factory in the list.
     */
    private final int index;

    /**
     * Constructs a new table factory processing chain.
     *
     * @param factories The list of table factories.
     */
    public TableSchemaChain(List<TableSchemaBuilder> factories) {
        this(factories, 0);
    }

    /**
     * Private constructor to initialize a node in the processing chain.
     *
     * @param factories The list of table factories.
     * @param index     The index of the current factory.
     */
    private TableSchemaChain(List<TableSchemaBuilder> factories, int index) {
        this.factories = factories;
        this.index = index;
        if (this.index < this.factories.size()) {
            this.next = new TableSchemaChain(factories, this.index + 1);
        } else {
            this.next = null;
        }
    }

    /**
     * Creates entity table information by invoking table factories in a chain.
     *
     * @param entityClass The entity class type.
     * @return The entity table information, or null if no factory handles it.
     */
    @Override
    public TableMeta createTable(Class<?> entityClass) {
        if (index < factories.size()) {
            return factories.get(index).createTable(entityClass, next);
        }
        return null;
    }

}
