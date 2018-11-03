package stonesStrict.distributionLock.distribution;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class DistributionDemo {

    /**
     * 创建五个线程，每个线程获取锁两次
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        DistributionLockInit.init();
        final CountDownLatch latch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            final DistributionLock lock = new DistributionLock();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        latch.await();
                        lock.lock();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Thread" + i).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        latch.await();
                        lock.lock();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Thread" + i).start();
            latch.countDown();
        }
        System.in.read();
    }

}
