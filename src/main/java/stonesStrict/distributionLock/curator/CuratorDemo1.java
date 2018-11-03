package stonesStrict.distributionLock.curator;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;

public class CuratorDemo1 {

    public static void main(String[] args) {
        InterProcessMutex mutex = CuratorLock.getLock();
        try {
            System.out.println("1 : try get lock");
            //获取锁
            mutex.acquire();
            //处理业务数据
            System.out.println("1 : get lock and process data");
            Thread.sleep(10000);
            //释放锁
            mutex.release();
            System.out.println("1 : release lock");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
