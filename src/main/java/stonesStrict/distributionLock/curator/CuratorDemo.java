package stonesStrict.distributionLock.curator;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.concurrent.CountDownLatch;

public class CuratorDemo {

    /**
     * 创建两个线程，每个线程获取锁一次
     * 也可执行两个main方法创建两个进程争取锁
     * @param args
     */
    public static void main(String[] args) {
        final CountDownLatch latch = new CountDownLatch(2);

        for (int i = 0; i < 2; i++) {
            final int j = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    CuratorDemo.process(j);
                }
            }, "Thread" + i).start();
            latch.countDown();
        }
    }

    private static void process(int i) {
        InterProcessMutex mutex = CuratorLock.getLock();
        try {
            System.out.println(i + " : try get lock");
            //获取锁
            mutex.acquire();
            //处理业务数据
            System.out.println(i +" : get lock and process data");
            Thread.sleep(10000);
            //释放锁
            mutex.release();
            System.out.println(i + " : release lock");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
