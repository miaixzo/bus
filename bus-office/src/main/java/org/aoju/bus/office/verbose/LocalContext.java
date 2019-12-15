package org.aoju.bus.office.verbose;

import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.uno.XComponentContext;
import org.aoju.bus.office.Context;

/**
 * 表示用于本地转换的office上下文
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface LocalContext extends Context {

    /**
     * 获取此上下文的office组件加载程序.
     *
     * @return 组件加载程序.
     */
    XComponentLoader getComponentLoader();

    /**
     * 获取此上下文的office组件上下文.
     *
     * @return 组件的上下文.
     */
    XComponentContext getComponentContext();

    /**
     * 获取此上下文的office桌面.
     *
     * @return 桌面.
     */
    XDesktop getDesktop();

}
