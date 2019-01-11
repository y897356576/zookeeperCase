package stonesStrict.constant;

public class Constant {

    //zookeeper服务地址
    public static final String ZK_HOST = "127.0.0.1:2181";
    //public static final String ZK_HOST = "10.17.130.86:2181,10.17.130.87:2181,10.17.130.87:2182"; //集群配置

    //Curator锁根节点
    public static final String CURATOR_ROOT = "curator";
    public static final String CURATOR_LOCK = "/locks";

    //锁根节点
    public static final String ROOT_LOCK = "/locks";

    //服务根节点
    public static final String ROOT_SERVER = "/servers";
}
