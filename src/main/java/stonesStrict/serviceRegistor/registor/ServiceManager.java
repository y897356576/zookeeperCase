package stonesStrict.serviceRegistor.registor;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import stonesStrict.constant.Constant;
import stonesStrict.distributionLock.curator.CuratorLock;
import stonesStrict.serviceRegistor.server.service.RpcServiceAnno;
import stonesStrict.util.SelectBalance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceManager {

    private static ZooKeeper zooKeeper;
    private static final HashMap<String, Object> serviceMap = new HashMap<>();
    private static Map<String, List<String>> serviceAddressMap = new ConcurrentHashMap<>();

    /**
     * 打开zookeeper链接
     * 初始化服务注册根节点
     */
    private static void initRegistor() {
        if(zooKeeper != null) return;

        synchronized (ServiceManager.class) {
            if(zooKeeper != null) return;
            try {
                zooKeeper = new ZooKeeper(Constant.ZK_HOST,
                        4000, null);
                ServiceManager.createServiceRoot();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断并创建服务注册根节点
     * @throws Exception
     */
    private static void createServiceRoot() throws Exception {
        InterProcessMutex mutex = CuratorLock.getLock();
        mutex.acquire();
        Stat stat = zooKeeper.exists(Constant.ROOT_SERVER, false);
        if(stat == null) {
            zooKeeper.create(Constant.ROOT_SERVER, "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        mutex.release();
    }

    /**
     * 判断并创建服务名称节点
     * @param serviceName
     * @throws Exception
     */
    private static String createServiceName(String serviceName) throws Exception {
        String path = Constant.ROOT_SERVER + "/" + serviceName;
        Stat stat = zooKeeper.exists(path, false);
        if(stat != null) {
            return path;
        }
        InterProcessMutex mutex = CuratorLock.getLock();
        mutex.acquire();
        stat = zooKeeper.exists(path, false);
        if(stat == null) {
            zooKeeper.create(path, "0".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        mutex.release();
        return path;
    }

    /**
     * 创建服务地址节点
     * @param serviceNamePath
     * @param address
     * @throws KeeperException
     * @throws InterruptedException
     */
    private static String createServiceAddress(String serviceNamePath, String address) throws KeeperException, InterruptedException {
        String path = serviceNamePath + "/" + address;
        Stat stat = zooKeeper.exists(path, false);
        if(stat == null) {
            zooKeeper.create(path, "0".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        }
        return path;
    }

    /**
     * 创建服务节点
     * @param serviceName
     * @param address
     * @return
     * @throws Exception
     */
    private static String createServiceNode(String serviceName, String address) throws Exception {
        if(zooKeeper == null) {
            ServiceManager.initRegistor();
        }
        String serviceNamePath = ServiceManager.createServiceName(serviceName);
        return ServiceManager.createServiceAddress(serviceNamePath, address);
    }

    /**
     * 服务注册
     * @param service
     */
    public static void doRegiste(Object service, String address) {
        if(service == null || service.getClass().getAnnotation(RpcServiceAnno.class) == null) {
            throw new RuntimeException("需注册的服务不存在");
        }
        RpcServiceAnno anno = service.getClass().getAnnotation(RpcServiceAnno.class);
        String serviceName = anno.value().getName();
        String version = anno.version();
        if(StringUtils.isBlank(serviceName)) {
            throw new RuntimeException("注册的服务名称不能为空");
        }
        if(StringUtils.isNotBlank(version)) {
            serviceName = serviceName + "-" + version;
        }

        try {
            //创建服务名称节点
            ServiceManager.createServiceNode(serviceName, address);
        } catch (Exception e) {
            throw new RuntimeException("服务注册异常", e);
        }
        //绑定服务名称与服务实例
        serviceMap.put(serviceName, service);
    }

    /**
     * 根据服务名称获取服务实例
     * @param serviceName
     * @return
     */
    public static Object getServer(String serviceName, String version) {
        if(StringUtils.isNotBlank(version)) {
            serviceName = serviceName + "-" + version;
        }
        return serviceMap.get(serviceName);
    }


    /**
     * 根据服务名称获取服务地址
     * @param serviceName
     * @param version
     * @return
     */
    public static String getAddress(String serviceName, String version) {
        if(StringUtils.isBlank(serviceName)) {
            throw new RuntimeException("获取的服务不存在");
        }
        serviceName = Constant.ROOT_SERVER + "/" + serviceName;
        if(StringUtils.isNotBlank(version)) {
            serviceName += "-" + version;
        }

        List<String> addresses = new ArrayList<>();
        if(!serviceAddressMap.isEmpty()) {
            addresses = serviceAddressMap.get(serviceName);
        }
        if(addresses == null || addresses.isEmpty()) {
            if(zooKeeper == null) {
                ServiceManager.initRegistor();
            }
            try {
                addresses = zooKeeper.getChildren(serviceName, false);
            } catch (Exception e) {
                throw new RuntimeException("获取服务地址失败", e);
            }

            if(addresses == null || addresses.isEmpty()) {
                throw new RuntimeException("获取服务地址失败，此服务下没有地址节点");
            }
            serviceAddressMap.put(serviceName, addresses);

            //动态发现服务节点的变化
            registerWatcher(serviceName);
        }

        return SelectBalance.selectBalance(addresses).toString();
    }


    private static CuratorFramework client;
    /**
     * 初始化Curator
     */
    private static void curatorInit() {
        if(client != null) return;
        synchronized (ServiceManager.class) {
            try {
                if(client != null) return;

                client = CuratorFrameworkFactory.builder().
                        connectString(Constant.ZK_HOST).
                        sessionTimeoutMs(4000).
                        retryPolicy(new ExponentialBackoffRetry(1000,
                                10)).build();
                client.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 通过curator设置监听path的子节点变化
     * @param serviceName
     */
    private static void registerWatcher(final String serviceName) {
        if(client == null) {
            curatorInit();
        }
        PathChildrenCache childrenCache = new PathChildrenCache(client, serviceName, true);

        PathChildrenCacheListener childrenListener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                List<String> addresses = curatorFramework.getChildren().forPath(serviceName);
                serviceAddressMap.put(serviceName, addresses);
            }
        };
        childrenCache.getListenable().addListener(childrenListener);
        try {
            childrenCache.start();
        } catch (Exception e) {
            throw new RuntimeException("注册子节点监听异常" + e);
        }
    }

}
