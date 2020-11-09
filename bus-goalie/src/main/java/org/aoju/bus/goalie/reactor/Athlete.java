package org.aoju.bus.goalie.reactor;

import org.aoju.bus.core.collection.ConcurrentHashSet;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.logger.Logger;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.List;
import java.util.Set;

/**
 * 服务端
 *
 * @author Justubborn
 * @since 2020/10/27
 */
public class Athlete {

  private final HttpServer httpServer;
  private final Set<Asset> assets = new ConcurrentHashSet<>();
  private DisposableServer disposableServer;
  private List<AssetRegistry> assetRegistries;

  public Athlete(HttpServer httpServer, List<AssetRegistry> assetRegistries) {
    this.httpServer = httpServer;
    this.assetRegistries = assetRegistries;
  }

  public Set<Asset> getAssets() {
    return assets;
  }

  private void init() {
    if (CollKit.isNotEmpty(assetRegistries)) {
      assetRegistries.forEach(assetRegistry -> {
        assets.addAll(assetRegistry.init());
      });
    }
    disposableServer = httpServer.bindNow();
    Logger.info("reactor server start on port:{} success", disposableServer.port());
  }

  private void destroy() {
    disposableServer.disposeNow();
    Logger.info("reactor server stop on port:{} success", disposableServer.port());
  }

}
