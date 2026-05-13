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
package org.miaixz.bus.image.metric.hl7.api;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.metric.hl7.net.HL7Application;
import org.miaixz.bus.image.metric.hl7.net.HL7ApplicationInfo;

/**
 * Defines the HL7Configuration contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface HL7Configuration {

    /**
     * Executes the register hl7 application operation.
     *
     * @param name the name.
     * @return true if the condition is met; otherwise false.
     * @throws InternalException if the operation cannot be completed.
     */
    boolean registerHL7Application(String name) throws InternalException;

    /**
     * Executes the unregister hl7 application operation.
     *
     * @param name the name.
     * @throws InternalException if the operation cannot be completed.
     */
    void unregisterHL7Application(String name) throws InternalException;

    /**
     * Finds the hl7 application.
     *
     * @param name the name.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    HL7Application findHL7Application(String name) throws InternalException;

    /**
     * Executes the list registered hl7 application names operation.
     *
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    String[] listRegisteredHL7ApplicationNames() throws InternalException;

    /**
     * Provides DICOM processing details.
     *
     * @param keys the keys.
     * @return the result.
     * @throws InternalException if the operation cannot be completed.
     */
    HL7ApplicationInfo[] listHL7AppInfos(HL7ApplicationInfo keys) throws InternalException;

}
