/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.builtin.hardware.common;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.builtin.hardware.Printer;

/**
 * Abstract base class for Printer implementations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public abstract class AbstractPrinter implements Printer {

    /**
     * The name value.
     */
    private final String name;
    /**
     * The driverName value.
     */
    private final String driverName;
    /**
     * The description value.
     */
    private final String description;
    /**
     * The status value.
     */
    private final PrinterStatus status;
    /**
     * The statusReason value.
     */
    private final String statusReason;
    /**
     * The isDefault value.
     */
    private final boolean isDefault;
    /**
     * The isLocal value.
     */
    private final boolean isLocal;
    /**
     * The portName value.
     */
    private final String portName;

    /**
     * Creates a new AbstractPrinter instance.
     *
     * @param name         the name
     * @param driverName   the driver name
     * @param description  the description
     * @param status       the status
     * @param statusReason the status reason
     * @param isDefault    the is default
     * @param isLocal      the is local
     * @param portName     the port name
     */
    protected AbstractPrinter(String name, String driverName, String description, PrinterStatus status,
            String statusReason, boolean isDefault, boolean isLocal, String portName) {
        this.name = name;
        this.driverName = driverName;
        this.description = description;
        this.status = status;
        this.statusReason = statusReason;
        this.isDefault = isDefault;
        this.isLocal = isLocal;
        this.portName = portName;
    }

    /**
     * Returns the name.
     *
     * @return the get name result
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the driver name.
     *
     * @return the get driver name result
     */
    @Override
    public String getDriverName() {
        return driverName;
    }

    /**
     * Returns the description.
     *
     * @return the get description result
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Returns the status.
     *
     * @return the get status result
     */
    @Override
    public PrinterStatus getStatus() {
        return status;
    }

    /**
     * Returns the status reason.
     *
     * @return the get status reason result
     */
    @Override
    public String getStatusReason() {
        return statusReason;
    }

    /**
     * Returns whether the default condition is true.
     *
     * @return the is default result
     */
    @Override
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Returns whether the local condition is true.
     *
     * @return the is local result
     */
    @Override
    public boolean isLocal() {
        return isLocal;
    }

    /**
     * Returns the port name.
     *
     * @return the get port name result
     */
    @Override
    public String getPortName() {
        return portName;
    }

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        return "Printer [name=" + name + ", driverName=" + driverName + ", description=" + description + ", status="
                + status + ", statusReason=" + statusReason + ", isDefault=" + isDefault + ", isLocal=" + isLocal
                + ", portName=" + redactPortName(portName) + "]";
    }

    /**
     * Returns the redact port name result.
     *
     * @param portName the port name
     * @return the redact port name result
     */
    private static String redactPortName(String portName) {
        if (portName == null || portName.isEmpty()) {
            return "";
        }
        int schemeEnd = portName.indexOf("://");
        if (schemeEnd > 0) {
            int atIndex = portName.indexOf('@', schemeEnd + 3);
            if (atIndex > schemeEnd + 3) {
                return portName.substring(0, schemeEnd + 3) + "***" + portName.substring(atIndex);
            }
        }
        return portName;
    }

}
