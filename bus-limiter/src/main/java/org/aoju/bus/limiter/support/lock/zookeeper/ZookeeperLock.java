package org.aoju.bus.limiter.support.lock.zookeeper;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.limiter.support.lock.Lock;
import org.aoju.bus.logger.Logger;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
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
            throw new InstrumentException(e);
        }
    }

    @Override
    public String getLimiterName() {
        return lockName;
    }
}
