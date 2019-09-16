/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.io.resource;

import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.core.utils.UriUtils;

import java.io.File;

/**
 * 文件资源访问对象
 *
 * @author Kimi Liu
 * @version 3.2.8
 * @since JDK 1.8
 */
public class FileResource extends UriResource {

    /**
     * 构造
     *
     * @param file 文件
     */
    public FileResource(File file) {
        this(file, file.getName());
    }

    /**
     * 构造
     *
     * @param file     文件
     * @param fileName 文件名，如果为null获取文件本身的文件名
     */
    public FileResource(File file, String fileName) {
        super(UriUtils.getURL(file), StringUtils.isBlank(fileName) ? file.getName() : fileName);
    }

    /**
     * 构造
     *
     * @param path 文件绝对路径或相对ClassPath路径，但是这个路径不能指向一个jar包中的文件
     */
    public FileResource(String path) {
        this(FileUtils.file(path));
    }

}
