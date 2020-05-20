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
package org.aoju.bus.starter.office;

import lombok.Data;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.magic.family.FormatProperties;
import org.aoju.bus.starter.BusXExtend;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 文档预览配置类
 *
 * @author Kimi Liu
 * @version 5.9.2
 * @since JDK 1.8+
 */
@Data
@ConfigurationProperties(prefix = BusXExtend.OFFICE)
public class OfficeProperties {

    /**
     * 表示office主目录。如果没有设置，
     * 将自动检测office默认安装目录，
     * 首先检测LibreOffice最新版本.
     */
    private String officeHome;

    /**
     * 处理线程使用的以逗号分隔的端口列表.
     * office实例的数量等于端口的数量，
     * 因为每个端口号将启动一个office进程.
     */
    private String portNumbers = StringUtils.toString(Builder.DEFAULT_PORT_NUMBER);

    /**
     * 将创建临时office配置文件的目录。如果未设置，
     * 则默认为java.io.tmpdir指定的系统临时目录
     */
    private String workingDir;

    /**
     * 模板配置文件目录，以便在启动已处理的office时将其复制到已创建的office配置文件目录.
     */
    private String templateProfileDir;

    /**
     * 指示当同一连接字符串的office进程已经存在时，是否必须终止现有的office进程.
     */
    private boolean killExistingProcess = true;

    /**
     * 过程超时时间(单位为毫秒). 尝试执行office流程调用时使用(启动/终止).
     */
    private long processTimeout = 120000L;

    /**
     * 进程重试间隔(毫秒).用于office进程调用尝试之间的等待(启动/终止).
     */
    private long processRetryInterval = 250L;

    /**
     * 允许处理任务的最大时间. 如果任务的处理时间长于此超时，则此任务将中止并处理下一个任务.
     */
    private long taskExecutionTimeout = 120000L;

    /**
     * 在重新启动之前，office进程可以执行的最大任务数.
     */
    private int maxTasksPerProcess = 200;

    /**
     * 转换队列中任务的最大生存时间. 如果等待时间长于此超时，则任务将从队列中删除.
     */
    private long taskQueueTimeout = 30000L;

    /**
     * 显式office流程管理器的类名。提供的流程管理器的类型。
     * 该类必须实现org.aoju.bus.office.process.ProcessManager接口
     */
    private String processManagerClass;

    /**
     * 包含默认支持的文档格式的注册表的路径.
     */
    private String documentFormatRegistry;

    /**
     * 加载(打开)和存储(保存)文档所需的自定义属性.
     */
    private Map<String, FormatProperties> formatOptions;

    /**
     * 指向LibreOffice在线服务器的URL.
     */
    private String url;

    /**
     * 管理器的池大小.
     */
    private int poolSize = 1;

}
