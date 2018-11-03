package stonesStrict.serviceRegistor.server.service;

import stonesStrict.serviceRegistor.ISayHelloService;

@RpcServiceAnno(value = ISayHelloService.class, version = "2.0")
public class SayHelloServiceImpl2 implements ISayHelloService {

    @Override
    public String sayHello(String name) {
        System.out.println("hello "+ name + "[version-2.0]");
        return "hello "+ name + "[version-2.0]";
    }

}
