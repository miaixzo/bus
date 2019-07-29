package org.aoju.bus.limiter;

import java.util.Map;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface ArgumentInjector {

    Map<String, Object> inject(Object... args);
}
