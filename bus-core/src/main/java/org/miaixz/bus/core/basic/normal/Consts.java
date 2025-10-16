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
package org.miaixz.bus.core.basic.normal;

import org.miaixz.bus.core.lang.Normal;

/**
 * Defines business-related constants, providing unified identifiers and key names for use across the application.
 * <p>
 * This class contains commonly used constants such as status codes, type identifiers, and response map keys, aiming to
 * maintain code consistency and maintainability. These constants can be used in scenarios like representing business
 * states, entity types, API response formats, etc.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Consts extends Normal {

    /**
     * General-purpose numeric identifier -1.
     * <p>
     * Can be used to represent various business concepts such as:
     * <ul>
     * <li>Status codes (e.g., disabled, deleted, invalid)</li>
     * <li>Type identifiers (e.g., special categories)</li>
     * <li>Priority levels (e.g., critical)</li>
     * <li>Hierarchical depths (e.g., root level)</li>
     * <li>Configuration values (e.g., fallback defaults)</li>
     * </ul>
     */
    public static final Integer MINUS_ONE = -1;

    /**
     * General-purpose numeric identifier 0.
     * <p>
     * Can be used to represent various business concepts such as:
     * <ul>
     * <li>Status codes (e.g., initial, default, pending)</li>
     * <li>Type identifiers (e.g., base categories)</li>
     * <li>Priority levels (e.g., normal)</li>
     * <li>Hierarchical depths (e.g., ground level)</li>
     * <li>Configuration values (e.g., neutral settings)</li>
     * </ul>
     */
    public static final Integer ZERO = 0;

    /**
     * General-purpose numeric identifier 1.
     * <p>
     * Can be used to represent various business concepts such as:
     * <ul>
     * <li>Status codes (e.g., active, enabled, success)</li>
     * <li>Type identifiers (e.g., primary categories)</li>
     * <li>Priority levels (e.g., high)</li>
     * <li>Hierarchical depths (e.g., first level)</li>
     * <li>Configuration values (e.g., primary options)</li>
     * </ul>
     */
    public static final Integer ONE = 1;

    /**
     * General-purpose numeric identifier 2.
     * <p>
     * Can be used to represent various business concepts such as:
     * <ul>
     * <li>Status codes (e.g., secondary, warning)</li>
     * <li>Type identifiers (e.g., secondary categories)</li>
     * <li>Priority levels (e.g., medium-high)</li>
     * <li>Hierarchical depths (e.g., second level)</li>
     * <li>Configuration values (e.g., alternative settings)</li>
     * </ul>
     */
    public static final Integer TWO = 2;

    /**
     * General-purpose numeric identifier 3.
     * <p>
     * Can be used to represent various business concepts such as:
     * <ul>
     * <li>Status codes (e.g., tertiary, processing)</li>
     * <li>Type identifiers (e.g., tertiary categories)</li>
     * <li>Priority levels (e.g., medium)</li>
     * <li>Hierarchical depths (e.g., third level)</li>
     * <li>Configuration values (e.g., additional options)</li>
     * </ul>
     */
    public static final Integer THREE = 3;

    /**
     * General-purpose numeric identifier 4.
     * <p>
     * Can be used to represent various business concepts such as:
     * <ul>
     * <li>Status codes (e.g., quaternary, informational)</li>
     * <li>Type identifiers (e.g., fourth-level categories)</li>
     * <li>Priority levels (e.g., medium-low)</li>
     * <li>Hierarchical depths (e.g., fourth level)</li>
     * <li>Configuration values (e.g., extended settings)</li>
     * </ul>
     */
    public static final Integer FOUR = 4;

    /**
     * General-purpose numeric identifier 5.
     * <p>
     * Can be used to represent various business concepts such as:
     * <ul>
     * <li>Status codes (e.g., quinary, transitional)</li>
     * <li>Type identifiers (e.g., fifth-level categories)</li>
     * <li>Priority levels (e.g., low)</li>
     * <li>Hierarchical depths (e.g., fifth level)</li>
     * <li>Configuration values (e.g., supplementary options)</li>
     * </ul>
     */
    public static final Integer FIVE = 5;

    /**
     * General-purpose numeric identifier 6.
     * <p>
     * Can be used to represent various business concepts such as:
     * <ul>
     * <li>Status codes (e.g., senary, intermediate)</li>
     * <li>Type identifiers (e.g., sixth-level categories)</li>
     * <li>Priority levels (e.g., very low)</li>
     * <li>Hierarchical depths (e.g., sixth level)</li>
     * <li>Configuration values (e.g., auxiliary settings)</li>
     * </ul>
     */
    public static final Integer SIX = 6;

    /**
     * General-purpose numeric identifier 7.
     * <p>
     * Can be used to represent various business concepts such as:
     * <ul>
     * <li>Status codes (e.g., septenary, provisional)</li>
     * <li>Type identifiers (e.g., seventh-level categories)</li>
     * <li>Priority levels (e.g., minimal)</li>
     * <li>Hierarchical depths (e.g., seventh level)</li>
     * <li>Configuration values (e.g., optional settings)</li>
     * </ul>
     */
    public static final Integer SEVEN = 7;

    /**
     * General-purpose numeric identifier 8.
     * <p>
     * Can be used to represent various business concepts such as:
     * <ul>
     * <li>Status codes (e.g., octonary, temporary)</li>
     * <li>Type identifiers (e.g., eighth-level categories)</li>
     * <li>Priority levels (e.g., negligible)</li>
     * <li>Hierarchical depths (e.g., eighth level)</li>
     * <li>Configuration values (e.g., rarely used options)</li>
     * </ul>
     */
    public static final Integer EIGHT = 8;

    /**
     * General-purpose numeric identifier 9.
     * <p>
     * Can be used to represent various business concepts such as:
     * <ul>
     * <li>Status codes (e.g., nonary, final)</li>
     * <li>Type identifiers (e.g., ninth-level categories)</li>
     * <li>Priority levels (e.g., lowest)</li>
     * <li>Hierarchical depths (e.g., deepest level)</li>
     * <li>Configuration values (e.g., exceptional cases)</li>
     * </ul>
     */
    public static final Integer NINE = 9;

    /**
     * Key name for error code in response results.
     * <p>
     * In API responses or service call results, this key identifies the operation status code, typically used alongside
     * HTTP status codes to provide more detailed error classification.
     * 
     */
    public static final String ERRCODE = "errcode";

    /**
     * Key name for error message in response results.
     * <p>
     * In API responses or service call results, this key provides human-readable error descriptions, helping developers
     * and end-users understand the issue.
     */
    public static final String ERRMSG = "errmsg";

    /**
     * Key name for data payload in response results.
     * <p>
     * In API responses or service call results, this key carries the actual data content, which can be a single object,
     * array, or complex data structure.
     */
    public static final String DATA = "data";

}
