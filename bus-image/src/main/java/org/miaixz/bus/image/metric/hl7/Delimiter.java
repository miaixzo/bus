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
package org.miaixz.bus.image.metric.hl7;

/**
 * Defines the Delimiter values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum Delimiter {

    /**
     * The field value.
     */
    field {

        @Override
        Delimiter parent() {
            return null;
        }
    },
    /**
     * The component value.
     */
    component {

        @Override
        Delimiter parent() {
            return repeat;
        }
    },
    /**
     * The repeat value.
     */
    repeat {

        @Override
        Delimiter parent() {
            return field;
        }
    },
    /**
     * The escape value.
     */
    escape {

        @Override
        Delimiter parent() {
            return subcomponent;
        }
    },
    /**
     * The subcomponent value.
     */
    subcomponent {

        @Override
        Delimiter parent() {
            return component;
        }
    };

    /**
     * The default value.
     */
    static final String DEFAULT = "|^~\\&";

    /**
     * The escape value.
     */
    static final String ESCAPE = "FSRET";

    /**
     * Executes the attribute operation.
     *
     * @return the operation result.
     */
    String attribute() {
        return name() + "Delimiter";
    }

    /**
     * Executes the parent operation.
     *
     * @return the operation result.
     */
    abstract Delimiter parent();

}
