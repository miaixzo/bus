package org.aoju.bus.http.internal;

import org.aoju.bus.core.utils.StringUtils;

/**
 * Runnable implementation which always sets its thread name.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class NamedRunnable implements Runnable {

    protected final String name;

    public NamedRunnable(String format, Object... args) {
        this.name = StringUtils.format(format, args);
    }

    @Override
    public final void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(name);
        try {
            execute();
        } finally {
            Thread.currentThread().setName(oldName);
        }
    }

    protected abstract void execute();

}
