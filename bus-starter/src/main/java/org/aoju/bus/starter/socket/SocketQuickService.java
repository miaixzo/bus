package org.aoju.bus.starter.socket;

import org.aoju.bus.socket.Protocol;
import org.aoju.bus.socket.QuickAioServer;
import org.aoju.bus.socket.process.MessageProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author Kimi Liu
 * @version 6.1.5
 * @since JDK 1.8+
 */
public class SocketQuickService {

    private final SocketProperties properties;
    @Autowired
    private MessageProcessor messageProcessor;
    @Autowired
    private Protocol protocol;
    private QuickAioServer quickAioServer;

    public SocketQuickService(SocketProperties properties) {
        this.properties = properties;
    }

    public void start() {
        this.quickAioServer = new QuickAioServer(this.properties.getPort(), protocol, messageProcessor);
        try {
            quickAioServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        quickAioServer.shutdown();
    }

}
