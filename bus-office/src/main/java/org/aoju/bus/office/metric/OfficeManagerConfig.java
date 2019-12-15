package org.aoju.bus.office.metric;

import java.io.File;

/**
 * 这个类提供了{@link AbstractOfficeManager}的配置.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface OfficeManagerConfig {

    /**
     * 获取将在处理流时创建临时文件的目录.
     * 默认: 系统临时目录<code>java.io.tmpdir</code>确定.
     *
     * @return 工作目录.
     */
    File getWorkingDir();

    /**
     * 设置将在处理流时创建临时文件的目录.
     *
     * @param workingDir 新的工作目录.
     */
    void setWorkingDir(final File workingDir);

}
