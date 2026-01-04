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
 * @since Java 17+
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
        return cursor < resources.size();
    }

    /**
     * Moves to the next resource in the iteration.
     *
     * @return This {@code MultiResource} instance after advancing the cursor.
     * @throws ConcurrentModificationException If there are no more resources to iterate over.
     */
    @Override
    public synchronized Resource next() {
        if (cursor >= resources.size()) {
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
