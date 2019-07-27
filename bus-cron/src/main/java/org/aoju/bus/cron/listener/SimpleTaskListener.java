package org.aoju.bus.cron.listener;

import org.aoju.bus.cron.TaskExecutor;

/**
 * 简单监听实现，不做任何操作<br>
 * 继承此监听后实现需要的方法即可
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class SimpleTaskListener implements TaskListener {

    @Override
    public void onStart(TaskExecutor executor) {
    }

    @Override
    public void onSucceeded(TaskExecutor executor) {

    }

    @Override
    public void onFailed(TaskExecutor executor, Throwable exception) {

    }

}
