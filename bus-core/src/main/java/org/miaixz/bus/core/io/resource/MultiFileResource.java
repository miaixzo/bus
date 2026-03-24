/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 21+
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
