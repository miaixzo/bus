/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io.resource;

import java.io.File;
import java.io.Serial;
import java.nio.file.Path;
import java.util.Collection;

/**
 * A composite resource that combines multiple file resources. This resource acts as a self-cycling cursor, where
 * calling {@link #next()} retrieves the subsequent resource. After all resources are consumed, {@link #reset()} can be
 * called to reset the cursor for reuse.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultiFileResource extends MultiResource {

    @Serial
    private static final long serialVersionUID = 2852232069651L;

    /**
     * Constructs a {@code MultiFileResource} with a collection of {@link File} objects.
     *
     * @param files A collection of {@link File} objects.
     */
    public MultiFileResource(final Collection<File> files) {
        add(files);
    }

    /**
     * Constructs a {@code MultiFileResource} with an array of {@link File} objects.
     *
     * @param files An array of {@link File} objects.
     */
    public MultiFileResource(final File... files) {
        add(files);
    }

    /**
     * Constructs a {@code MultiFileResource} with an array of {@link Path} objects.
     *
     * @param files An array of {@link Path} objects.
     */
    public MultiFileResource(final Path... files) {
        add(files);
    }

    /**
     * Adds one or more {@link File} resources to this composite resource.
     *
     * @param files An array of {@link File} objects to add.
     * @return This {@code MultiFileResource} instance for method chaining.
     */
    public MultiFileResource add(final File... files) {
        for (final File file : files) {
            this.add(new FileResource(file));
        }
        return this;
    }

    /**
     * Adds one or more {@link Path} resources to this composite resource.
     *
     * @param files An array of {@link Path} objects to add.
     * @return This {@code MultiFileResource} instance for method chaining.
     */
    public MultiFileResource add(final Path... files) {
        for (final Path file : files) {
            this.add(new FileResource(file));
        }
        return this;
    }

    /**
     * Adds a collection of {@link File} resources to this composite resource.
     *
     * @param files A collection of {@link File} objects to add.
     * @return This {@code MultiFileResource} instance for method chaining.
     */
    public MultiFileResource add(final Collection<File> files) {
        for (final File file : files) {
            this.add(new FileResource(file));
        }
        return this;
    }

    /**
     * Adds a single resource to this composite resource.
     *
     * @param resource The {@link Resource} to add.
     * @return This {@code MultiFileResource} instance for method chaining.
     */
    @Override
    public MultiFileResource add(final Resource resource) {
        return (MultiFileResource) super.add(resource);
    }

}
