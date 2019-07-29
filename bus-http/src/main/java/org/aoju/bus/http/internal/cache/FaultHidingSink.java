package org.aoju.bus.http.internal.cache;

import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.io.ForwardingSink;
import org.aoju.bus.core.io.Sink;

import java.io.IOException;

/**
 * A sink that never throws IOExceptions, even if the underlying sink does.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
class FaultHidingSink extends ForwardingSink {

    private boolean hasErrors;

    FaultHidingSink(Sink delegate) {
        super(delegate);
    }

    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        if (hasErrors) {
            source.skip(byteCount);
            return;
        }
        try {
            super.write(source, byteCount);
        } catch (IOException e) {
            hasErrors = true;
            onException(e);
        }
    }

    @Override
    public void flush() throws IOException {
        if (hasErrors) return;
        try {
            super.flush();
        } catch (IOException e) {
            hasErrors = true;
            onException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (hasErrors) return;
        try {
            super.close();
        } catch (IOException e) {
            hasErrors = true;
            onException(e);
        }
    }

    protected void onException(IOException e) {
    }
}
