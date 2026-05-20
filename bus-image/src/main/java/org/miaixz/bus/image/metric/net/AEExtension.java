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
package org.miaixz.bus.image.metric.net;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents the AEExtension type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AEExtension implements Serializable {

    /**
     * Constructs a new AEExtension instance.
     */
    public AEExtension() {
        // No initialization required.
    }

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852269683081L;

    /**
     * The ae value.
     */
    protected ApplicationEntity ae;

    /**
     * Gets the application entity.
     *
     * @return the application entity.
     */
    public final ApplicationEntity getApplicationEntity() {
        return ae;
    }

    /**
     * Sets the application entity.
     *
     * @param ae the ae.
     */
    void setApplicationEntity(ApplicationEntity ae) {
        if (ae != null && this.ae != null)
            throw new IllegalStateException("already owned by AE: " + ae.getAETitle());
        this.ae = ae;
    }

    /**
     * Executes the reconfigure operation.
     *
     * @param from the from.
     */
    public void reconfigure(AEExtension from) {

    }

}
