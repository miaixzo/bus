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
package org.miaixz.bus.vortex.provider;

import org.miaixz.bus.core.lang.exception.LicenseException;

/**
 * License validation provider interface.
 * <p>
 * Defines the core functionality for validating license effectiveness. Implementations of this interface should contain
 * specific license validation logic, such as checking validity period, bound hardware information, domain names, etc.
 * </p>
 */
public interface LicenseProvider {

    /**
     * Executes the license validation operation.
     * <p>
     * <b>Implementation Contract:</b>
     * <ul>
     * <li>If the license is valid for the given principal, this method should return normally without any
     * operation.</li>
     * <li>If the license is invalid (e.g., expired, principal mismatch, signature error, etc.), this method should
     * throw {@link LicenseException} or another runtime exception to interrupt the operation.</li>
     * </ul>
     *
     * @param principal The entity identifier used for license validation, e.g., a domain name (e.g., "example.com:443")
     *                  or a company name (e.g., "Acme Corporation").
     * @return {@code true} if the license is valid, {@code false} otherwise. The default implementation returns
     *         {@code true}.
     * @throws LicenseException If license validation fails.
     */
    default boolean validate(String principal) {
        // The default implementation is empty, allowing license checks to be disabled in some environments.
        // Specific validation logic should be provided by the implementing class.
        return true;
    }

}
