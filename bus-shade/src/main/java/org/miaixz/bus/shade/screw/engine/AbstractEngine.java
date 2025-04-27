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
package org.miaixz.bus.shade.screw.engine;

import java.io.File;
import java.io.IOException;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.shade.screw.Builder;

import lombok.Getter;
import lombok.Setter;

/**
 * 模板引擎抽象类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public abstract class AbstractEngine implements TemplateEngine {

    /**
     * 模板配置
     */
    private EngineConfig engineConfig;

    private AbstractEngine() {
    }

    public AbstractEngine(EngineConfig engineConfig) {
        Assert.notNull(engineConfig, "EngineConfig can not be empty!");
        this.engineConfig = engineConfig;
    }

    /**
     * 获取文件，文件名格式为，数据库名_版本号.文件类型
     *
     * @param docName 文档名称
     * @return {@link String}
     */
    protected File getFile(String docName) {
        File file;
        // 如果没有填写输出路径，默认当前项目路径下的doc目录
        if (StringKit.isBlank(getEngineConfig().getFileOutputDir())) {
            String dir = System.getProperty("user.dir");
            file = new File(dir + "/doc");
        } else {
            file = new File(getEngineConfig().getFileOutputDir());
        }
        // 不存在创建
        if (!file.exists()) {
            // 创建文件夹
            boolean mkdir = file.mkdirs();
        }
        // 文件后缀
        String suffix = getEngineConfig().getFileType().getFileSuffix();
        file = new File(file, docName + suffix);
        // 设置文件产生位置
        getEngineConfig().setFileOutputDir(file.getParent());
        return file;
    }

    /**
     * 打开文档生成的输出目录
     */
    protected void openOutputDir() {
        // 是否打开，如果是就打开输出路径
        if (getEngineConfig().isOpenOutputDir() && StringKit.isNotBlank(getEngineConfig().getFileOutputDir())) {
            try {
                // 获取系统信息
                String osName = System.getProperty("os.name");
                if (null != osName) {
                    if (osName.contains(Builder.MAC)) {
                        Runtime.getRuntime().exec("open " + getEngineConfig().getFileOutputDir());
                    } else if (osName.contains(Builder.WINDOWS)) {
                        Runtime.getRuntime().exec("cmd /c start " + getEngineConfig().getFileOutputDir());
                    }
                }
            } catch (IOException e) {
                throw new InternalException(e);
            }
        }
    }

}
