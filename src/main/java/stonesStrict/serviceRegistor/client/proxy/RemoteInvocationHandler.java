package stonesStrict.serviceRegistor.client.proxy;

import stonesStrict.serviceRegistor.client.SendRequest.RpcRequest;
import stonesStrict.serviceRegistor.client.SendRequest.SendRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RemoteInvocationHandler implements InvocationHandler {

    private String version;

    public RemoteInvocationHandler(String version) {
        this.version = version;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();
        request.setServiceName(method.getDeclaringClass().getName());
        request.setMethod(method.getName());
        request.setArgs(args);
        request.setVersion(version);

        return SendRequest.sendRequest(request);
    }

}
