package stonesStrict.distributionLock.distribution;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import stonesStrict.constant.Constant;

public class DistributionLockInit {

    public static void init() {
        try {
            ZooKeeper zooKeeper = new ZooKeeper(Constant.ZK_HOST,
                    4000, null);
            Stat stat = zooKeeper.exists(Constant.ROOT_LOCK, false);
            if(stat == null) {
                zooKeeper.create(Constant.ROOT_LOCK, "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
