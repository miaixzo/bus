/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
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
    private final List<AssetRegistry> assetRegistries;
    private DisposableServer disposableServer;


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
