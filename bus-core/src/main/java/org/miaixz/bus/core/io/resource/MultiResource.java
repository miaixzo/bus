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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ListKit;

/**
 * A composite resource that combines multiple resources. This resource acts as a self-cycling cursor, where calling
 * {@link #next()} retrieves the subsequent resource. After all resources are consumed, {@link #reset()} can be called
 * to reset the cursor for reuse.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MultiResource implements Resource, Iterable<Resource>, Iterator<Resource>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852232202695L;

    /**
     * The list of resources managed by this composite resource.
     */
    private final List<Resource> resources;
    /**
     * The current cursor position within the list of resources.
     */
    private int cursor;

    /**
     * Constructs a {@code MultiResource} with an array of resources.
     *
     * @param resources An array of {@link Resource} objects.
     */
    public MultiResource(final Resource... resources) {
        this(ListKit.of(resources));
    }

    /**
     * Constructs a {@code MultiResource} with a collection of resources.
     *
     * @param resources A {@link Collection} of {@link Resource} objects.
     */
    public MultiResource(final Collection<Resource> resources) {
        if (resources instanceof List) {
            this.resources = (List<Resource>) resources;
        } else {
            this.resources = ListKit.of(resources);
        }
    }

    /**
     * Returns the name of the current resource pointed to by the cursor.
     *
     * @return The name of the current resource.
     */
    @Override
    public String getName() {
        return resources.get(cursor).getName();
    }

    /**
     * Returns the URL of the current resource pointed to by the cursor.
     *
     * @return The URL of the current resource.
     */
    @Override
    public URL getUrl() {
        return resources.get(cursor).getUrl();
    }

    /**
     * Returns the size of the current resource pointed to by the cursor.
     *
     * @return The size of the current resource in bytes.
     */
    @Override
    public long size() {
        return resources.get(cursor).size();
    }

    /**
     * Returns an input stream for the current resource pointed to by the cursor.
     *
     * @return An input stream for reading the current resource.
     */
    @Override
    public InputStream getStream() {
        return resources.get(cursor).getStream();
    }

    /**
     * Checks if the current resource pointed to by the cursor has been modified.
     *
     * @return {@code true} if the current resource has been modified, {@code false} otherwise.
     */
    @Override
    public boolean isModified() {
        return resources.get(cursor).isModified();
    }

    /**
     * Returns a buffered reader for the current resource pointed to by the cursor.
     *
     * @param charset The character set to use for reading.
     * @return A buffered reader for the current resource.
     */
    @Override
    public BufferedReader getReader(final Charset charset) {
        return resources.get(cursor).getReader(charset);
    }

    /**
     * Reads the content of the current resource pointed to by the cursor as a string.
     *
     * @param charset The character set to use for reading.
     * @return The content of the current resource as a string.
     * @throws InternalException If an error occurs while reading the resource.
     */
    @Override
    public String readString(final Charset charset) throws InternalException {
        return resources.get(cursor).readString(charset);
    }

    /**
     * Reads the content of the current resource pointed to by the cursor as a string using the default charset.
     *
     * @return The content of the current resource as a string.
     * @throws InternalException If an error occurs while reading the resource.
     */
    @Override
    public String readString() throws InternalException {
        return resources.get(cursor).readString();
    }

    /**
     * Reads the content of the current resource pointed to by the cursor as a byte array.
     *
     * @return The content of the current resource as a byte array.
     * @throws InternalException If an error occurs while reading the resource.
     */
    @Override
    public byte[] readBytes() throws InternalException {
        return resources.get(cursor).readBytes();
    }

    /**
     * Returns an iterator over all resources in this collection.
     *
     * @return An iterator over all resources.
     */
    @Override
    public Iterator<Resource> iterator() {
        return resources.iterator();
    }

    /**
     * Checks if there are more resources to iterate over.
     *
     * @return {@code true} if there are more resources, {@code false} otherwise.
     */
    @Override
    public boolean hasNext() {
        return Math.max(cursor, 0) < resources.size();
    }

    /**
     * Moves to the next resource in the iteration.
     *
     * @return This {@code MultiResource} instance after advancing the cursor.
     * @throws ConcurrentModificationException If there are no more resources to iterate over.
     */
    @Override
    public synchronized Resource next() {
        if (!hasNext()) {
            throw new ConcurrentModificationException();
        }
        this.cursor++;
        return this;
    }

    /**
     * Removes the current resource from the collection.
     */
    @Override
    public void remove() {
        this.resources.remove(this.cursor);
    }

    /**
     * Resets the cursor to the beginning of the resource list, allowing for re-iteration.
     */
    public synchronized void reset() {
        this.cursor = 0;
    }

    /**
     * Adds a single resource to the end of the resource list.
     *
     * @param resource The {@link Resource} to add.
     * @return This {@code MultiResource} instance for method chaining.
     */
    public MultiResource add(final Resource resource) {
        this.resources.add(resource);
        return this;
    }

    /**
     * Adds multiple resources from an iterable collection to the end of the resource list.
     *
     * @param iterable An {@link Iterable} collection of {@link Resource} objects to add.
     * @return This {@code MultiResource} instance for method chaining.
     */
    public MultiResource addAll(final Iterable<? extends Resource> iterable) {
        iterable.forEach((this::add));
        return this;
    }

}
