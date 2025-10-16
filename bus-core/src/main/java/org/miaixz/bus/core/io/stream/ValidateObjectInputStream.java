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
package org.miaixz.bus.core.io.stream;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import org.miaixz.bus.core.xyz.CollKit;

/**
 * An {@link ObjectInputStream} extension that provides class validation to prevent deserialization vulnerabilities.
 * This stream allows defining a whitelist and blacklist of classes that are permitted or forbidden during
 * deserialization.
 * <p>
 * For more details, refer to: <a href="https://xz.aliyun.com/t/41/">https://xz.aliyun.com/t/41/</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ValidateObjectInputStream extends ObjectInputStream {

    /**
     * A set of class names that are explicitly allowed for deserialization (whitelist).
     */
    private Set<String> whiteClassSet;
    /**
     * A set of class names that are explicitly forbidden for deserialization (blacklist).
     */
    private Set<String> blackClassSet;

    /**
     * Constructs a new {@code ValidateObjectInputStream} that reads from the specified {@link InputStream}. Optionally,
     * initial whitelist classes can be provided.
     *
     * @param inputStream   The input stream to read serialized objects from.
     * @param acceptClasses An array of classes to be added to the whitelist. Only these classes and their subclasses
     *                      will be allowed for deserialization, unless explicitly refused.
     * @throws IOException If an I/O error occurs while reading stream header.
     */
    public ValidateObjectInputStream(final InputStream inputStream, final Class<?>... acceptClasses)
            throws IOException {
        super(inputStream);
        accept(acceptClasses);
    }

    /**
     * Adds classes to the blacklist, preventing them from being deserialized.
     *
     * @param refuseClasses An array of classes whose deserialization should be forbidden.
     */
    public void refuse(final Class<?>... refuseClasses) {
        if (null == this.blackClassSet) {
            this.blackClassSet = new HashSet<>();
        }
        for (final Class<?> refuseClass : refuseClasses) {
            this.blackClassSet.add(refuseClass.getName());
        }
    }

    /**
     * Adds classes to the whitelist, allowing them to be deserialized.
     *
     * @param acceptClasses An array of classes whose deserialization should be allowed.
     */
    public void accept(final Class<?>... acceptClasses) {
        if (null == this.whiteClassSet) {
            this.whiteClassSet = new HashSet<>();
        }
        for (final Class<?> acceptClass : acceptClasses) {
            this.whiteClassSet.add(acceptClass.getName());
        }
    }

    /**
     * Overrides the default class resolution mechanism to validate classes against the whitelist and blacklist. Only
     * classes that pass the validation will be allowed to be deserialized.
     *
     * @param desc The {@link ObjectStreamClass} of the class to be resolved.
     * @return The {@link Class} object corresponding to {@code desc}.
     * @throws IOException            If an I/O error occurs.
     * @throws ClassNotFoundException If the class cannot be found or if it fails validation.
     * @throws InvalidClassException  If the class is not allowed for deserialization based on the whitelist/blacklist.
     */
    @Override
    protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        validateClassName(desc.getName());
        return super.resolveClass(desc);
    }

    /**
     * Validates the given class name against the configured whitelist and blacklist. If the class name is in the
     * blacklist, or not in the whitelist (and whitelist is not empty), an {@link InvalidClassException} is thrown.
     *
     * @param className The fully qualified name of the class to validate.
     * @throws InvalidClassException If the class is not authorized for deserialization.
     */
    private void validateClassName(final String className) throws InvalidClassException {
        // Check against blacklist first
        if (CollKit.isNotEmpty(this.blackClassSet)) {
            if (this.blackClassSet.contains(className)) {
                throw new InvalidClassException("Unauthorized deserialization attempt by black list", className);
            }
        }

        // If whitelist is empty, all classes are allowed (after blacklist check).
        if (CollKit.isEmpty(this.whiteClassSet)) {
            return;
        }

        // Java internal classes are always allowed if a whitelist is present.
        if (className.startsWith("java.")) {
            return;
        }

        // Check against whitelist
        if (this.whiteClassSet.contains(className)) {
            return;
        }

        // If not in whitelist and whitelist is not empty, then it's unauthorized.
        throw new InvalidClassException("Unauthorized deserialization attempt", className);
    }

}
