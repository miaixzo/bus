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
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;

import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.UrlKit;

/**
 * URL resource access class. This class provides an implementation of the {@link Resource} interface for resources
 * identified by a {@link URL}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class UrlResource implements Resource, Serializable {

    @Serial
    private static final long serialVersionUID = 2852232772701L;

    /**
     * The URL of the resource.
     */
    protected URL url;
    /**
     * The name of the resource.
     */
    protected String name;
    /**
     * The last modified timestamp of the resource. Initialized to 0 for non-file resources.
     */
    private long lastModified = 0;

    /**
     * Constructs a {@code UrlResource} from a given {@link URI}.
     *
     * @param uri The {@link URI} of the resource.
     */
    public UrlResource(final URI uri) {
        this(UrlKit.url(uri), null);
    }

    /**
     * Constructs a {@code UrlResource} from a given {@link URL}.
     *
     * @param url The {@link URL} of the resource.
     */
    public UrlResource(final URL url) {
        this(url, null);
    }

    /**
     * Constructs a {@code UrlResource} from a given {@link URL} and a resource name. If the URL protocol is "file", the
     * last modified timestamp of the file is recorded.
     *
     * @param url  The {@link URL} of the resource, may be {@code null}.
     * @param name The name of the resource. If {@code null}, the name is derived from the URL path.
     */
    public UrlResource(final URL url, final String name) {
        this.url = url;
        if (null != url && Normal.URL_PROTOCOL_FILE.equals(url.getProtocol())) {
            this.lastModified = FileKit.file(url).lastModified();
        }
        this.name = ObjectKit.defaultIfNull(name, () -> (null != url ? FileName.getName(url.getPath()) : null));
    }

    /**
     * Returns the name of this URL resource.
     *
     * @return The name of the resource.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the URL of this resource.
     *
     * @return The URL of this resource.
     */
    @Override
    public URL getUrl() {
        return this.url;
    }

    /**
     * Returns the size of this URL resource.
     *
     * @return The size of the resource in bytes.
     */
    @Override
    public long size() {
        return UrlKit.size(this.url);
    }

    /**
     * Returns an input stream for this URL resource.
     *
     * @return An input stream for reading the resource.
     * @throws InternalException If the resource URL is {@code null} or an error occurs while opening the stream.
     */
    @Override
    public InputStream getStream() throws InternalException {
        if (null == this.url) {
            throw new InternalException("Resource URL is null!");
        }
        return UrlKit.getStream(url);
    }

    /**
     * Checks if this URL resource has been modified since it was last accessed.
     * <p>
     * This method is only applicable to file URLs. For other URL types, it always returns {@code false}.
     *
     * @return {@code true} if this is a file URL and the file has been modified, {@code false} otherwise.
     */
    @Override
    public boolean isModified() {
        // lastModified == 0 indicates that this resource is not a file resource
        return (0 != this.lastModified) && this.lastModified != getFile().lastModified();
    }

    /**
     * Retrieves the {@link File} object corresponding to this URL resource. This method is applicable only if the URL
     * protocol is "file".
     *
     * @return The {@link File} object.
     */
    public File getFile() {
        return FileKit.file(this.url);
    }

    /**
     * Returns the string representation of the URL.
     *
     * @return The URL as a string, or "null" if the URL is {@code null}.
     */
    @Override
    public String toString() {
        return (null == this.url) ? "null" : this.url.toString();
    }

    /**
     * Creates a new {@code UrlResource} relative to this resource.
     *
     * @param relativePath The relative path to the new resource.
     * @return A new {@code UrlResource} representing the relative path.
     */
    public UrlResource createRelative(final String relativePath) {
        return new UrlResource(UrlKit.getURL(getUrl(), relativePath));
    }

}
