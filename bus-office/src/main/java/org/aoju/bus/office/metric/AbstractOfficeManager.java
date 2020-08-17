/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.office.metric;

import org.aoju.bus.core.key.ObjectID;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 所有{@link OfficeManager}的基类
 *
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8+
 */
public abstract class AbstractOfficeManager implements OfficeManager, TemporaryFileMaker {

    protected final OfficeManagerBuilder config;
    private final AtomicLong tempFileCounter;
    private File tempDir;

    /**
     * 使用指定的设置构造类的新实例
     *
     * @param config 配置信息
     */
    protected AbstractOfficeManager(final OfficeManagerBuilder config) {
        super();
        this.config = config;
        tempFileCounter = new AtomicLong(0);
    }

    /**
     * 在指定目录下创建临时目录
     *
     * @param workingDir 要在其下创建临时目录的目录
     * @return 创建的目录
     */
    protected static File makeTempDir(final File workingDir) {
        final File tempDir = new File(workingDir, "converter_" + ObjectID.id());
        tempDir.mkdir();
        if (!tempDir.isDirectory()) {
            throw new IllegalStateException(String.format("Cannot create temp directory: %s", tempDir));
        }
        return tempDir;
    }

    @Override
    public File makeTemporaryFile() {
        return new File(tempDir, "tempfile_" + tempFileCounter.getAndIncrement());
    }

    @Override
    public File makeTemporaryFile(final String extension) {
        return new File(tempDir, "tempfile_" + tempFileCounter.getAndIncrement() + Symbol.DOT + extension);
    }

    /**
     * 创建临时目录
     */
    protected void makeTempDir() {
        deleteTempDir();
        tempDir = makeTempDir(config.getWorkingDir());
    }

    /**
     * 删除临时目录
     */
    protected void deleteTempDir() {
        if (tempDir != null) {
            Logger.debug("Deleting temporary directory '{}'", tempDir);
            try {
                FileKit.delete(tempDir);
            } catch (InstrumentException ioEx) {
                Logger.error("Could not temporary profileDir: {}", ioEx.getMessage());
            }
        }
    }

    /**
     * 构造{@link AbstractOfficeManager}的生成器
     */
    public abstract static class AbstractOfficeManagerBuilder<
            B extends AbstractOfficeManagerBuilder<B>> {

        protected boolean install;
        protected File workingDir;

        protected AbstractOfficeManagerBuilder() {
            super();
        }

        /**
         * 指定由该生成器创建的office管理器是否将随后设置{@link InstalledOfficeHolder}类的唯一实例
         * 注意，如果{@code InstalledOfficeManagerHolder}类已经持有{@code OfficeManager}实例，
         * 则现有管理器的所有者负责停止它.
         * 默认: false
         *
         * @return 当前实例信息
         */
        public B install() {
            this.install = true;
            return (B) this;
        }

        /**
         * 指定创建临时文件和目录的目录
         * 默认: 临时目录由<code>java.io.tmpdir</code>确定
         *
         * @param workingDir 要设置的新工作目录
         * @return 当前实例信息.
         */
        public B workingDir(final File workingDir) {
            this.workingDir = workingDir;
            return (B) this;
        }

        /**
         * 指定创建临时文件和目录的目录
         * 默认: 临时目录由<code>java.io.tmpdir</code>确定
         *
         * @param workingDir 要设置的新工作目录
         * @return 当前实例信息.
         */
        public B workingDir(final String workingDir) {
            return StringKit.isBlank(workingDir) ? (B) this : workingDir(new File(workingDir));
        }

        /**
         * 创建此生成器指定的管理器
         *
         * @return 由该生成器指定的管理
         */
        protected abstract AbstractOfficeManager build();
    }

}
