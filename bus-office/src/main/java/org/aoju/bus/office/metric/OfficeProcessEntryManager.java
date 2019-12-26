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
package org.aoju.bus.office.metric;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XHierarchicalPropertySet;
import com.sun.star.beans.XHierarchicalPropertySetInfo;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesBatch;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.bridge.LocalOfficePoolManager;
import org.aoju.bus.office.builtin.MadeInOffice;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.UnoUrl;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OfficeProcessManagerPoolEntry负责执行通过{@link LocalOfficePoolManager}提交的任务。
 * 它将向其内部{@link OfficeProcessManager}提交任务，并等待任务完成或到达配置的任务执行超时.
 * OfficeProcessManagerPoolEntry还负责在达到每个进程的最大任务数时重新启动office进程.
 *
 * @author Kimi Liu
 * @version 5.3.8
 * @since JDK 1.8+
 */
public class OfficeProcessEntryManager extends AbstractOfficeEntryManager {

    private static final String PROPPATH_USE_OPENGL = "VCL/UseOpenGL";

    private final OfficeProcessManager officeProcessManager;

    private final AtomicInteger taskCount = new AtomicInteger(0);
    private final AtomicBoolean disconnectExpected = new AtomicBoolean(false);

    /**
     * 当连接建立或关闭/丢失/来自office实例时，将通知此连接事件侦听器.
     */
    private final OfficeConnectEventListener connectionEventListener =
            new OfficeConnectEventListener() {

                // 建立联系.
                @Override
                public void connected(final OfficeConnectEvent event) {
                    // 重置任务计数并使任务执行程序可用.
                    taskCount.set(0);
                    taskExecutor.setAvailable(true);
                }

                // 连接已关闭/丢失.
                @Override
                public void disconnected(final OfficeConnectEvent event) {

                    // 使任务执行程序不可用.
                    taskExecutor.setAvailable(false);

                    // 当它来自一个预期的行为(我们已经放在调用函数之前将字段设置为true)，只需要重置即可
                    // 将期望值断开为false。当我们没有预料到的时候 断开连接后，必须重新启动office进程
                    // 将取消可能正在运行的任何任务
                    if (!disconnectExpected.compareAndSet(true, false)) {

                        // 在这里,我们没有预料到这种分离,必须重新启动office进程，取消可能正在运行的任何任务.
                        Logger.warn("Connection lost unexpectedly; attempting restart");
                        if (currentFuture != null) {
                            currentFuture.cancel(true);
                        }
                        officeProcessManager.restartDueToLostConnection();
                    }
                }
            };

    /**
     * 使用默认配置为指定的office URL创建新的协议池.
     *
     * @param unoUrl 需要创建的URL.
     */
    public OfficeProcessEntryManager(final UnoUrl unoUrl) {
        this(unoUrl, new OfficeProcessManagerEntryBuilder());
    }

    /**
     * 使用指定的配置为指定的office URL创建新的协议池.
     *
     * @param unoUrl 需要创建的URL.
     * @param config 配置.
     */
    public OfficeProcessEntryManager(
            final UnoUrl unoUrl, final OfficeProcessManagerEntryBuilder config) {
        super(config);

        // 创建处理office实例的流程管理器
        officeProcessManager = new OfficeProcessManager(unoUrl, config);

        // 监听到office实例的任何连接事件.
        officeProcessManager.getLocalOffice().addConnectionEventListener(connectionEventListener);
    }

    @Override
    public void doExecute(final MadeInOffice task) throws InstrumentException {

        final OfficeProcessManagerEntryBuilder entryConfig =
                (OfficeProcessManagerEntryBuilder) config;

        // 首先检查是否必须重新启动office进程
        final int count = taskCount.getAndIncrement();
        if (entryConfig.getMaxTasksPerProcess() > 0 && count == entryConfig.getMaxTasksPerProcess()) {

            Logger.info("Reached limit of {} maximum tasks per process; restarting...",
                    entryConfig.getMaxTasksPerProcess());
            restart();

            // 此时taskCount将是0而不是1，所以要修复它.
            taskCount.getAndIncrement();
        }
        task.execute(officeProcessManager.getLocalOffice());
    }

    @Override
    protected void handleExecuteTimeoutException(final TimeoutException timeoutEx) {
        // 是任务没有在配置的超时内完成，必须重新启动
        officeProcessManager.restartDueToTaskTimeout();
    }

    @Override
    public boolean isRunning() {
        return super.isRunning() && officeProcessManager.getLocalOffice().isConnected();
    }

    @Override
    public void doStart() throws InstrumentException {

        // 启动office流程并连接到它.
        officeProcessManager.startAndWait();

        // 这里已经成功地建立了连接。检查以禁用OpenGL的使用。如果OpenGL是开着的(LibreOffice)，有些文件无法正常加载
        final OfficeProcessManagerEntryBuilder entryConfig =
                (OfficeProcessManagerEntryBuilder) config;
        if (entryConfig.isDisableOpengl()
                && disableOpengl(officeProcessManager.getLocalOffice().getComponentContext())) {

            Logger.info("OpenGL has been disabled and a restart is required; restarting...");
            restart();
        }
    }

    @Override
    public void doStop() throws InstrumentException {
        // 从这里开始，任何原因导致与Office进程的断开都是可能的.
        disconnectExpected.set(true);

        // 现在可以停止运行的office进程
        officeProcessManager.stopAndWait();
    }

    private void restart() throws InstrumentException {
        // 执行程序不再可用
        taskExecutor.setAvailable(false);

        // 设置预期要断开连接
        disconnectExpected.set(true);

        // 重新启动office实例
        officeProcessManager.restartAndWait();
    }

    /**
     * 为指定的配置路径创建配置视图
     *
     * @param provider 服务提供者
     * @param path     路径
     * @return the object
     */
    private Object createConfigurationView(final XMultiServiceFactory provider, final String path)
            throws com.sun.star.uno.Exception {

        //创建参数:nodepath
        final PropertyValue argument = new PropertyValue();
        argument.Name = "nodepath";
        argument.Value = path;

        final Object[] arguments = new Object[1];
        arguments[0] = argument;

        // 创建视图
        return provider.createInstanceWithArguments(
                "com.sun.star.configuration.ConfigurationUpdateAccess", arguments);
    }

    private boolean disableOpengl(final XComponentContext officeContext) throws InstrumentException {
        // 有关更多选项，请参见配置注册表.
        // e.g: C:\Program Files\LibreOffice 5\share\registry\main.xcd
        try {

            // 创建UseOpenGL选项所在的根元素的视图
            final Object viewRoot =
                    createConfigurationView(
                            Lo.createInstanceMCF(
                                    officeContext,
                                    XMultiServiceFactory.class,
                                    "com.sun.star.configuration.ConfigurationProvider"),
                            "/org.openoffice.Office.Common");
            try {

                // 检查OpenGL选项是否打开
                final XHierarchicalPropertySet properties = Lo.qi(XHierarchicalPropertySet.class, viewRoot);

                final XHierarchicalPropertySetInfo propsInfo = properties.getHierarchicalPropertySetInfo();
                if (propsInfo.hasPropertyByHierarchicalName(PROPPATH_USE_OPENGL)) {
                    final boolean useOpengl =
                            (boolean) properties.getHierarchicalPropertyValue(PROPPATH_USE_OPENGL);
                    Logger.info("Use OpenGL is set to {}", useOpengl);
                    if (useOpengl) {
                        properties.setHierarchicalPropertyValue(PROPPATH_USE_OPENGL, false);
                        // 这里的视图已经应用了更改

                        final XChangesBatch updateControl = Lo.qi(XChangesBatch.class, viewRoot);
                        updateControl.commitChanges();

                        // 需要重新启动
                        return true;
                    }
                }
            } finally {
                // 完成了视图-处理它
                Lo.qi(XComponent.class, viewRoot).dispose();
            }
            // 不需要重启
            return false;

        } catch (com.sun.star.uno.Exception ex) {
            throw new InstrumentException("Unable to check if the Use OpenGL option is on.", ex);
        }
    }

}
