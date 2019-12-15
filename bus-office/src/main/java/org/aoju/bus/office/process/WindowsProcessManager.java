package org.aoju.bus.office.process;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 用于Windows的{@link ProcessManager}实现.
 * 需要wmic.exe和taskkill.exe，至少在Windows XP、Windows Vista和Windows 7上可用(家庭版除外)
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class WindowsProcessManager extends AbstractProcessManager {

    private static final Pattern PROCESS_GET_LINE =
            Pattern.compile("^\\s*(?<CommanLine>.*?)\\s+(?<Pid>\\d+)\\s*$");

    /**
     * 获取{@code WindowsProcessManager}的默认实例.
     *
     * @return 默认的{@code WindowsProcessManager}实例.
     */
    public static WindowsProcessManager getDefault() {
        return DefaultHolder.INSTANCE;
    }

    @Override
    protected String[] getRunningProcessesCommand(final String process) {
        return new String[]{
                "cmd", "/c", "wmic process where(name like '" + process + "%') get commandline,processid"
        };
    }

    @Override
    protected Pattern getRunningProcessLinePattern() {
        return PROCESS_GET_LINE;
    }

    /**
     * 获取需要的命令是否对Windows操作系统可用.
     *
     * @return {@code true}如果需要的命令可用，{@code false}否则.
     */
    public boolean isUsable() {
        try {
            execute(new String[]{"wmic", "quit"});
            execute(new String[]{"taskkill", "/?"});
            return true;
        } catch (IOException ioEx) {
            return false;
        }
    }

    @Override
    public void kill(final Process process, final long pid) throws IOException {
        execute(new String[]{"taskkill", "/t", "/f", "/pid", String.valueOf(pid)});
    }

    private static class DefaultHolder {
        static final WindowsProcessManager INSTANCE = new WindowsProcessManager();
    }

}
