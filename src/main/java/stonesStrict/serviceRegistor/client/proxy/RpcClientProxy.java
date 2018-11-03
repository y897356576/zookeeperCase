package stonesStrict.serviceRegistor.client.proxy;

import java.lang.reflect.Proxy;

public class RpcClientProxy {

    public static <T>T remoteClientProxy(Class<T> classes, String version) {
        return (T) Proxy.newProxyInstance(classes.getClassLoader(), new Class[]{classes},
                new RemoteInvocationHandler(version));
    }

}
