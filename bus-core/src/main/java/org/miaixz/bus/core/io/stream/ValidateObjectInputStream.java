/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * 带有类验证的对象流，用于避免反序列化漏洞 详细见：https://xz.aliyun.com/t/41/
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ValidateObjectInputStream extends ObjectInputStream {

    private Set<String> whiteClassSet;
    private Set<String> blackClassSet;

    /**
     * 构造
     *
     * @param inputStream   流
     * @param acceptClasses 白名单的类
     * @throws IOException IO异常
     */
    public ValidateObjectInputStream(final InputStream inputStream, final Class<?>... acceptClasses)
            throws IOException {
        super(inputStream);
        accept(acceptClasses);
    }

    /**
     * 禁止反序列化的类，用于反序列化验证
     *
     * @param refuseClasses 禁止反序列化的类
     */
    public void refuse(final Class<?>... refuseClasses) {
        if (null == this.blackClassSet) {
            this.blackClassSet = new HashSet<>();
        }
        for (final Class<?> acceptClass : refuseClasses) {
            this.blackClassSet.add(acceptClass.getName());
        }
    }

    /**
     * 接受反序列化的类，用于反序列化验证
     *
     * @param acceptClasses 接受反序列化的类
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
     * 只允许反序列化SerialObject class
     */
    @Override
    protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        validateClassName(desc.getName());
        return super.resolveClass(desc);
    }

    /**
     * 验证反序列化的类是否合法
     *
     * @param className 类名
     * @throws InvalidClassException 非法类
     */
    private void validateClassName(final String className) throws InvalidClassException {
        // 黑名单
        if (CollKit.isNotEmpty(this.blackClassSet)) {
            if (this.blackClassSet.contains(className)) {
                throw new InvalidClassException("Unauthorized deserialization attempt by black list", className);
            }
        }

        if (CollKit.isEmpty(this.whiteClassSet)) {
            return;
        }
        if (className.startsWith("java.")) {
            // java中的类默认在白名单中
            return;
        }
        if (this.whiteClassSet.contains(className)) {
            return;
        }

        throw new InvalidClassException("Unauthorized deserialization attempt", className);
    }

}
