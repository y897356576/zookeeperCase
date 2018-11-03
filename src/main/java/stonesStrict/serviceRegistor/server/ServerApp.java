package stonesStrict.serviceRegistor.server;

import stonesStrict.serviceRegistor.ISayHelloService;
import stonesStrict.serviceRegistor.server.service.SayHelloServiceImpl;
import stonesStrict.serviceRegistor.server.service.SayHelloServiceImpl2;
import stonesStrict.serviceRegistor.registor.ServiceManager;
import stonesStrict.serviceRegistor.server.socketProcess.SocketListenAndDoProcess;

public class ServerApp {

    public static void main(String[] args) throws Exception {
        ISayHelloService service = new SayHelloServiceImpl();
        ISayHelloService service2 = new SayHelloServiceImpl2();

        ServiceManager.doRegiste(service, "127.0.0.1:8081");
        ServiceManager.doRegiste(service2, "10.17.130.233:8082");

        new Thread(() -> {
            SocketListenAndDoProcess.listenAndProcessByBio(8081);
        }, "Thread-1").start();

        new Thread(() -> {
            SocketListenAndDoProcess.listenAndProcessByNio(8082);
        }, "Thread-2").start();

        System.in.read();
    }



}
