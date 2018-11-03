package stonesStrict.serviceRegistor.client;

import stonesStrict.serviceRegistor.ISayHelloService;
import stonesStrict.serviceRegistor.client.SendRequest.RpcRequest;
import stonesStrict.serviceRegistor.client.SendRequest.SendRequest;
import stonesStrict.serviceRegistor.client.proxy.RpcClientProxy;

public class ClientApp {

    public static void main(String[] args) {
        RpcRequest request = new RpcRequest();
        request.setServiceName("stonesStrict.serviceRegistor.ISayHelloService");
        request.setMethod("sayHello");
        request.setArgs(new Object[]{"stone"});
        request.setVersion("2.0");

        SendRequest.sendRequest(request);

        ISayHelloService service = RpcClientProxy.remoteClientProxy(ISayHelloService.class, "1.0");
        service.sayHello("stone");
    }

}
