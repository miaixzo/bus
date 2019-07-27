package org.aoju.bus.http.internal.http;

import org.aoju.bus.core.consts.MediaType;
import org.aoju.bus.core.io.BufferedSource;
import org.aoju.bus.http.ResponseBody;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class RealResponseBody extends ResponseBody {
    /**
     * Use a string to avoid parsing the content type until needed. This also defers problems caused
     * by malformed content types.
     */
    private final String contentType;
    private final long contentLength;
    private final BufferedSource source;

    public RealResponseBody(
            String contentType, long contentLength, BufferedSource source) {
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.source = source;
    }

    @Override
    public MediaType contentType() {
        return contentType != null ? MediaType.get(contentType) : null;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    @Override
    public BufferedSource source() {
        return source;
    }

}
