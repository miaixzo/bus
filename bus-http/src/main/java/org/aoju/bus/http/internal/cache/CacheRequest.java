package org.aoju.bus.http.internal.cache;

import org.aoju.bus.core.io.Sink;

import java.io.IOException;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface CacheRequest {

    Sink body() throws IOException;

    void abort();

}
