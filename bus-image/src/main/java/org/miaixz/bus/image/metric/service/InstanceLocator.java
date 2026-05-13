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
package org.miaixz.bus.image.metric.service;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents the InstanceLocator type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class InstanceLocator implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852276716582L;

    /**
     * The cuid value.
     */
    public final String cuid;

    /**
     * The iuid value.
     */
    public final String iuid;

    /**
     * The tsuid value.
     */
    public final String tsuid;

    /**
     * The uri value.
     */
    public final String uri;

    /**
     * The object value.
     */
    private Object object;

    /**
     * Creates a new instance.
     *
     * @param cuid  the cuid.
     * @param iuid  the iuid.
     * @param tsuid the tsuid.
     * @param uri   the uri.
     */
    public InstanceLocator(String cuid, String iuid, String tsuid, String uri) {
        this.cuid = cuid;
        this.iuid = iuid;
        this.tsuid = tsuid;
        this.uri = uri;
    }

    /**
     * Gets the object.
     *
     * @return the object.
     */
    public final Object getObject() {
        return object;
    }

    /**
     * Sets the object.
     *
     * @param object the object.
     * @return the operation result.
     */
    public final InstanceLocator setObject(Object object) {
        this.object = object;
        return this;
    }

    /**
     * Gets the file.
     *
     * @return the file.
     */
    public File getFile() {
        try {
            return new File(new URI(uri));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

}
