package org.miaixz.bus.starter.socket;

import jakarta.annotation.Resource;
import org.miaixz.bus.socket.AioQuickServer;
import org.miaixz.bus.socket.Protocol;
import org.miaixz.bus.socket.process.MessageProcessor;

import java.io.IOException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class SocketQuickService {

    private final SocketProperties properties;
    @Resource
    private MessageProcessor messageProcessor;
    @Resource
    private Protocol protocol;
    private AioQuickServer aioQuickServer;

    public SocketQuickService(SocketProperties properties) {
        this.properties = properties;
    }

    public void start() {
        this.aioQuickServer = new AioQuickServer(this.properties.getPort(), protocol, messageProcessor);
        try {
            aioQuickServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        aioQuickServer.shutdown();
    }

}
