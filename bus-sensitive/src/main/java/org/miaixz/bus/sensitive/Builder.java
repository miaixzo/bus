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
package org.miaixz.bus.sensitive;

import java.lang.annotation.Annotation;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.text.dfa.Sensitive;

/**
 * A utility class for applying desensitization strategies. It provides static methods for common desensitization tasks,
 * primarily intended for processing individual objects or strings.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Builder extends Sensitive {

    /**
     * Constant for enabling all processing (e.g., desensitization and encryption).
     */
    public static final String ALL = "ALL";
    /**
     * Constant for enabling only desensitization.
     */
    public static final String SENS = "SENS";
    /**
     * Constant for enabling only data security (encryption/decryption).
     */
    public static final String SAFE = "SAFE";
    /**
     * Constant for processing on input/write operations (e.g., encryption).
     */
    public static final String IN = "IN";
    /**
     * Constant for processing on output/read operations (e.g., decryption).
     */
    public static final String OUT = "OUT";
    /**
     * Constant for performing no processing.
     */
    public static final String NOTHING = "NOTHING";
    /**
     * Constant for applying global encryption rules.
     */
    public static final String OVERALL = "OVERALL";

    /**
     * Applies desensitization to the given object. This method creates a new provider instance for each call to ensure
     * thread safety.
     *
     * @param object The original object.
     * @param <T>    The type of the object.
     * @return The desensitized object.
     */
    public static <T> T on(Object object) {
        return on(object, null, false);
    }

    /**
     * Applies desensitization to the given object.
     *
     * @param object The original object.
     * @param clone  If true, a deep copy of the object is created before desensitization.
     * @param <T>    The type of the object.
     * @return The desensitized object.
     */
    public static <T> T on(Object object, boolean clone) {
        return on(object, null, clone);
    }

    /**
     * Applies desensitization to the given object based on a specific annotation context.
     *
     * @param object     The original object.
     * @param annotation The annotation providing context for the operation.
     * @param <T>        The type of the object.
     * @return The desensitized object.
     */
    public static <T> T on(Object object, Annotation annotation) {
        return (T) Instances.singletion(Provider.class).on(object, annotation, false);
    }

    /**
     * Serializes the object to a JSON string after applying desensitization.
     *
     * @param object The object to process and serialize.
     * @return The desensitized JSON string.
     */
    public static String json(Object object) {
        return Instances.singletion(Provider.class).json(object, null);
    }

    /**
     * Applies desensitization to the given object with full options.
     *
     * @param object     The original object.
     * @param annotation The annotation providing context for the operation.
     * @param clone      If true, a deep copy of the object is created before desensitization.
     * @param <T>        The type of the object.
     * @return The desensitized object.
     */
    public static <T> T on(Object object, Annotation annotation, boolean clone) {
        return (T) Instances.singletion(Provider.class).on(object, annotation, clone);
    }

}
