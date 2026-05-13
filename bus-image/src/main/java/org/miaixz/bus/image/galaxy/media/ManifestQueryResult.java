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
package org.miaixz.bus.image.galaxy.media;

import java.util.Collection;
import java.util.Objects;

/**
 * Manifest query result enriched with WADO retrieval parameters and viewer message metadata.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ManifestQueryResult extends Manifest {

    /**
     * The wado parameters value.
     */
    private final WadoParameters wadoParameters;

    /**
     * The viewer message value.
     */
    private ViewerMessage viewerMessage;

    /**
     * Creates a new instance.
     *
     * @param wadoParameters the wado parameters.
     */
    public ManifestQueryResult(WadoParameters wadoParameters) {
        this(null, wadoParameters);
    }

    /**
     * Creates a new instance.
     *
     * @param patients       the patients.
     * @param wadoParameters the wado parameters.
     */
    public ManifestQueryResult(Collection<ManifestPatient> patients, WadoParameters wadoParameters) {
        super(patients);
        this.wadoParameters = Objects.requireNonNull(wadoParameters, "WADO parameters cannot be null");
    }

    /**
     * Gets the wado parameters.
     *
     * @return the wado parameters.
     */
    public WadoParameters getWadoParameters() {
        return wadoParameters;
    }

    /**
     * Gets the viewer message.
     *
     * @return the viewer message.
     */
    public ViewerMessage getViewerMessage() {
        return viewerMessage;
    }

    /**
     * Sets the viewer message.
     *
     * @param viewerMessage the viewer message.
     */
    public void setViewerMessage(ViewerMessage viewerMessage) {
        this.viewerMessage = viewerMessage;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "ManifestQueryResult{" + "patientCount=" + getPatientCount() + ", wadoParameters=" + wadoParameters
                + ", hasViewerMessage=" + (viewerMessage != null) + '}';
    }

}
