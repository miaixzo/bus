/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.office.builtin;

import java.io.File;
import java.io.OutputStream;

/**
 * 具有指定转换源的转换作业.
 *
 * @author Kimi Liu
 * @version 5.8.0
 * @since JDK 1.8+
 */
public interface JobWithSource {

    /**
     * 将当前转换配置为将结果写入指定的目标.
     *
     * @param target 将写入转换结果的文件。现有文件将被覆盖
     *               如果文件被JVM或任何其他应用程序锁定或不可写，则会抛出异常
     * @return 当前转换规范.
     */
    OptionalTarget to(File target);

    /**
     * 将当前转换配置为将结果写入指定的{@link OutputStream}.
     * 在写入转换之后，流将被关闭.
     *
     * @param target 写入转换结果的输出流.
     * @return 当前转换规范.
     */
    RequiredTarget to(OutputStream target);

    /**
     * 将当前转换配置为将结果写入指定的{@link OutputStream}.
     * 在写入转换之后，流将被关闭.
     *
     * @param target      写入转换结果的输出流.
     * @param closeStream 确定写入结果后输出流是否关闭.
     * @return 当前转换规范.
     */
    RequiredTarget to(OutputStream target, boolean closeStream);

}
