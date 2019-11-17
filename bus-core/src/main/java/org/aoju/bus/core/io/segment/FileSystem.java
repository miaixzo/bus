/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.core.io.segment;

import org.aoju.bus.core.utils.IoUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 访问分层数据存储上的读写文件 大多数调用者应该使用{@link #SYSTEM}
 * 实现,它使用主机的本地文件系统 备用
 * 实现可用于注入错误(用于测试)或转换存储的数据(用于添加)
 * 例如加密)
 *
 * @author Kimi Liu
 * @version 5.2.2
 * @since JDK 1.8+
 */
public interface FileSystem {

    FileSystem SYSTEM = new FileSystem() {
        @Override
        public Source source(File file) throws FileNotFoundException {
            return IoUtils.source(file);
        }

        @Override
        public Sink sink(File file) throws FileNotFoundException {
            try {
                return IoUtils.sink(file);
            } catch (FileNotFoundException e) {
                // Maybe the parent directory doesn't exist? Try creating it first.
                file.getParentFile().mkdirs();
                return IoUtils.sink(file);
            }
        }

        @Override
        public Sink appendingSink(File file) throws FileNotFoundException {
            try {
                return IoUtils.appendingSink(file);
            } catch (FileNotFoundException e) {
                // Maybe the parent directory doesn't exist? Try creating it first.
                file.getParentFile().mkdirs();
                return IoUtils.appendingSink(file);
            }
        }

        @Override
        public void delete(File file) throws IOException {
            // If delete() fails, make sure it's because the file didn't exist!
            if (!file.delete() && file.exists()) {
                throw new IOException("failed to delete " + file);
            }
        }

        @Override
        public boolean exists(File file) {
            return file.exists();
        }

        @Override
        public long size(File file) {
            return file.length();
        }

        @Override
        public void rename(File from, File to) throws IOException {
            delete(to);
            if (!from.renameTo(to)) {
                throw new IOException("failed to rename " + from + " to " + to);
            }
        }

        @Override
        public void deleteContents(File directory) throws IOException {
            File[] files = directory.listFiles();
            if (files == null) {
                throw new IOException("not a readable directory: " + directory);
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteContents(file);
                }
                if (!file.delete()) {
                    throw new IOException("failed to delete " + file);
                }
            }
        }
    };

    Source source(File file) throws FileNotFoundException;

    Sink sink(File file) throws FileNotFoundException;

    Sink appendingSink(File file) throws FileNotFoundException;

    void delete(File file) throws IOException;

    boolean exists(File file);

    long size(File file);

    void rename(File from, File to) throws IOException;

    void deleteContents(File directory) throws IOException;

}
