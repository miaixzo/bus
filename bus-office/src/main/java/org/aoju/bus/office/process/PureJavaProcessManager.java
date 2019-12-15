package org.aoju.bus.office.process;

import org.aoju.bus.office.Builder;

/**
 * PureJava 系统流程管理器实现类.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class PureJavaProcessManager implements ProcessManager {

    /**
     * 获取{@code PureJavaProcessManager}的默认实例.
     *
     * @return 默认的{@code PureJavaProcessManager}实例.
     */
    public static PureJavaProcessManager getDefault() {
        return DefaultHolder.INSTANCE;
    }

    @Override
    public long find(final ProcessQuery query) {
        return Builder.PID_UNKNOWN;
    }

    @Override
    public void kill(final Process process, final long pid) {
        process.destroy();
    }

    private static class DefaultHolder {
        static final PureJavaProcessManager INSTANCE = new PureJavaProcessManager();
    }

}
