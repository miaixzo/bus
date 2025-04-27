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
package org.miaixz.bus.core.lang.reflect;

import java.lang.reflect.Member;

import org.miaixz.bus.core.lang.Assert;

/**
 * 类成员，用于获取类的修饰符等，如：
 * 
 * <pre>
 * ClassMember member = new ClassMember(String.class);
 * Console.log(member.getModifiers());
 * </pre>
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClassMember implements Member {

    private final Class<?> clazz;

    /**
     * 构造
     *
     * @param clazz 类
     */
    public ClassMember(final Class<?> clazz) {
        this.clazz = Assert.notNull(clazz);
    }

    /**
     * 静态工厂方法，用于创建ClassMember对象
     *
     * @param clazz 类
     * @return ClassMember对象
     */
    public static ClassMember of(final Class<?> clazz) {
        return new ClassMember(clazz);
    }

    @Override
    public Class<?> getDeclaringClass() {
        return this.clazz;
    }

    @Override
    public String getName() {
        return this.clazz.getName();
    }

    @Override
    public int getModifiers() {
        return this.clazz.getModifiers();
    }

    @Override
    public boolean isSynthetic() {
        return this.clazz.isSynthetic();
    }

}
