package stonesStrict.distributionLock.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import stonesStrict.constant.Constant;

public class CuratorLock {

    private static CuratorFramework client;

    private static void curatorInit() {
        if(client != null) {
            return;
        }
        client = CuratorFrameworkFactory.builder().
                connectString(Constant.ZK_HOST).
                sessionTimeoutMs(4000).
                retryPolicy(new ExponentialBackoffRetry(1000, 3)).
                namespace(Constant.CURATOR_ROOT).build();
        client.start();
    }

    public static InterProcessMutex getLock() {
        if(client == null) {
            CuratorLock.curatorInit();
        }
        InterProcessMutex mutex = new InterProcessMutex(client, Constant.CURATOR_LOCK);
        return mutex;
    }

    private static void curatirClose() {
        if(client != null) {
            client.close();
        }
    }
}
