/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.limiter.support.lock.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.limiter.support.lock.Lock;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZookeeperLock extends Lock {

    private String lockName;

    private String basePath;

    private CuratorFramework client;


    public ZookeeperLock(String lockName, String basePath, CuratorFramework client) {
        this.lockName = lockName;
        this.basePath = basePath;
        this.client = client;
        if (!client.getState().equals(CuratorFrameworkState.STARTED)) {
            client.start();
        }

        Logger.info("zookeeper lock named {} start success!", lockName);
    }

    public ZookeeperLock(String lockName, CuratorFramework client) {
        this(lockName, "/locks/", client);
    }

    @Override
    public boolean lock(Object key) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(basePath + key.toString());
            return true;
        } catch (KeeperException.NodeExistsException e) {
            Logger.info("lock fail on {}", key);
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void unlock(Object key) {

        try {
            client.delete().forPath(basePath + key.toString());
        } catch (KeeperException.NodeExistsException e) {
            throw new IllegalMonitorStateException("You do not own the lock: " + key);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    @Override
    public String getLimiterName() {
        return lockName;
    }
}
