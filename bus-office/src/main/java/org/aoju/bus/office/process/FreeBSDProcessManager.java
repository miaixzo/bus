package org.aoju.bus.office.process;

/**
 * 用于FreeBSD的{@link ProcessManager}实现
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class FreeBSDProcessManager extends UnixProcessManager {

    /**
     * 获取{@code FreeBSDProcessManager}的默认实例.
     *
     * @return 默认的{@code FreeBSDProcessManager}实例.
     */
    public static FreeBSDProcessManager getDefault() {
        return DefaultHolder.INSTANCE;
    }

    @Override
    protected String[] getRunningProcessesCommand(final String process) {
        return new String[]{
                "/bin/sh",
                "-c",
                "/bin/ps -e -o pid,args | /usr/bin/grep " + process + " | /usr/bin/grep -v grep"
        };
    }

    private static class DefaultHolder {
        static final FreeBSDProcessManager INSTANCE = new FreeBSDProcessManager();
    }

}
