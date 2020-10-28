package org.aoju.bus.goalie.reactor;

import org.aoju.bus.logger.Logger;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/**
 * 服务端
 *
 * @author Justubborn
 * @since 2020/10/27
 */
public class Athlete {

    private final HttpServer httpServer;

    private DisposableServer disposableServer;

    public Athlete(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    private void init() {
        disposableServer = httpServer.bindNow();
        Logger.info("reactor server start on port:{} success",disposableServer.port());
    }

    private void destroy() {
        disposableServer.disposeNow();
        Logger.info("reactor server stop on port:{} success",disposableServer.port());
    }

}
