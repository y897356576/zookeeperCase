package stonesStrict.distributionLock.distribution;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import stonesStrict.constant.Constant;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * zookeeper实现分布式锁
 */
public class DistributionLock implements Lock {

    private ZooKeeper zooKeeper;

    public DistributionLock() {
        try {
            zooKeeper = new ZooKeeper(Constant.ZK_HOST,
                    4000, null);
            Stat stat = zooKeeper.exists(Constant.ROOT_LOCK, false);
            if(stat == null) {
                zooKeeper.create(Constant.ROOT_LOCK, "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建当前节点
     * 尝试用当前节点获取锁
     * 成功则处理数据
     * 失败则等待获取数据
     * @return
     */
    @Override
    public void lock() {
        try {
            String currentLock = zooKeeper.create(Constant.ROOT_LOCK + "/", "0".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            if(this.tryLock(currentLock)) {
                System.out.println(Thread.currentThread().getName() + " : " + currentLock + "获取锁成功");
            } else {
                if(this.waitForLock(currentLock)) {
                    System.out.println(Thread.currentThread().getName() + " : " + currentLock + "经等待后，获取锁成功");
                }
            }
        } catch (KeeperException|InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取所有锁节点
     * 比较最小节点与当前节点的大小
     * 若当前节点为最小节点，则获取锁成功，返回true；
     * 若当前节点不为最小节点，则返回false
     * @return
     */
    private boolean tryLock(String currentLock) {
        try {
            System.out.println(Thread.currentThread().getName() + " : " + currentLock + " 尝试获取锁");

            List<String> locks = zooKeeper.getChildren(Constant.ROOT_LOCK, false);
            TreeSet<String> sortSet = new TreeSet<>();
            for(String lock : locks) {
                sortSet.add(Constant.ROOT_LOCK + "/" + lock);
            }

            String firstLock = sortSet.first();
            if(firstLock.equals(currentLock)) {
                return true;
            }
        } catch (KeeperException|InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取比当前节点小1的节点并监听该节点
     * 该节点失效后重新尝试用当前节点获取锁
     * @return
     */
    private Boolean waitForLock(String currentLock) throws KeeperException, InterruptedException {
        List<String> locks = zooKeeper.getChildren(Constant.ROOT_LOCK, false);
        TreeSet<String> sortSet = new TreeSet<>();
        for(String lock : locks) {
            sortSet.add(Constant.ROOT_LOCK + "/" + lock);
        }
        SortedSet<String> sortedSet = sortSet.headSet(currentLock);
        if(sortedSet.isEmpty()) {
            return true;
        }

        String waitLock = sortedSet.last();

        final CountDownLatch latch = new CountDownLatch(1);
        Stat waitStat = zooKeeper.exists(waitLock, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                latch.countDown();
            }
        });
        if(waitStat != null) {
            System.out.println(Thread.currentThread().getName() + " : " + currentLock + " 等待 " + waitLock + "释放锁");
            latch.await();
        }
        return this.tryLock(currentLock);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {

    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
