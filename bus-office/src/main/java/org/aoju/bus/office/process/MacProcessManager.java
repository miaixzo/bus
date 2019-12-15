package org.aoju.bus.office.process;

/**
 * 用于MAC的{@link ProcessManager}实现
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class MacProcessManager extends UnixProcessManager {

    /**
     * 获取{@code MacProcessManager}的默认实例.
     *
     * @return 默认的{@code MacProcessManager}实例.
     */
    public static MacProcessManager getDefault() {
        return DefaultHolder.INSTANCE;
    }

    @Override
    protected String[] getRunningProcessesCommand(final String process) {

        return new String[]{
                "/bin/bash",
                "-c",
                "/bin/ps -e -o pid,command | /usr/bin/grep " + process + " | /usr/bin/grep -v grep"
        };
    }

    private static class DefaultHolder {
        static final MacProcessManager INSTANCE = new MacProcessManager();
    }

}
