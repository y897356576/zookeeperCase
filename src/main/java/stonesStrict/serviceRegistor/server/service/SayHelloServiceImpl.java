package stonesStrict.serviceRegistor.server.service;

import stonesStrict.serviceRegistor.ISayHelloService;

@RpcServiceAnno(value = ISayHelloService.class, version = "1.0")
public class SayHelloServiceImpl implements ISayHelloService {

    @Override
    public String sayHello(String name) {
        System.out.println("hello "+ name + "[version-1.0]");
        return "hello "+ name + "[version-1.0]";
    }

}
