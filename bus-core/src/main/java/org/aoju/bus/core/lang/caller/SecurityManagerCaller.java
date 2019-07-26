package org.aoju.bus.core.lang.caller;

/**
 * 方式获取调用者
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class SecurityManagerCaller extends SecurityManager implements Caller {

    private static final int OFFSET = 1;

    @Override
    public Class<?> getCaller() {
        final Class<?>[] context = getClassContext();
        if ((OFFSET + 1) < context.length) {
            return context[OFFSET + 1];
        }
        return null;
    }

    @Override
    public Class<?> getCallers() {
        final Class<?>[] context = getClassContext();
        if ((OFFSET + 2) < context.length) {
            return context[OFFSET + 2];
        }
        return null;
    }

    @Override
    public Class<?> getCaller(int depth) {
        final Class<?>[] context = getClassContext();
        if ((OFFSET + depth) < context.length) {
            return context[OFFSET + depth];
        }
        return null;
    }

    @Override
    public boolean isCalledBy(Class<?> clazz) {
        final Class<?>[] classes = getClassContext();
        if (null != classes) {
            for (Class<?> contextClass : classes) {
                if (contextClass.equals(clazz)) {
                    return true;
                }
            }
        }
        return false;
    }

}
