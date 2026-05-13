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
package org.miaixz.bus.image.metric.hl7.net;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents the HL7ApplicationExtension type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HL7ApplicationExtension implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852267557616L;

    /**
     * The hl7 app value.
     */
    protected HL7Application hl7App;

    /**
     * Gets the hl7 application.
     *
     * @return the hl7 application.
     */
    public final HL7Application getHL7Application() {
        return hl7App;
    }

    /**
     * Sets the hl7 application.
     *
     * @param hl7App the hl7 app.
     */
    void setHL7Application(HL7Application hl7App) {
        if (hl7App != null && this.hl7App != null)
            throw new IllegalStateException("already owned by HL7 Application: " + hl7App.getApplicationName());
        this.hl7App = hl7App;
    }

    /**
     * Executes the reconfigure operation.
     *
     * @param from the from.
     */
    public void reconfigure(HL7ApplicationExtension from) {

    }

}
