package stonesStrict.serviceRegistor.server.socketProcess;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketListenAndDoProcess {

    /**
     * 监听端口并处理请求
     * 通过ServerSocket的方式
     * @param port
     */
    public static void listenAndProcessByBio(int port) {
        ServerSocket serverSocket = null;
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[" + port + "] 端口监听中(Stream)...");
            while (true) {
                final Socket socket = serverSocket.accept();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        ReqStreamHandler.socketHandler(socket);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 监听端口并处理请求
     * 通过Netty的方式
     * @param port
     */
    public static void listenAndProcessByNio(int port) {
        ChannelHandlerAdapter processor = new ReqNettyHandler();
        EventLoopGroup group = new NioEventLoopGroup(); //为非阻塞模式使用NioEventLoopGroup
        try {
            ServerBootstrap b = new ServerBootstrap();  //创建ServerBootstrap
            b.group(group)
                    .channel(NioServerSocketChannel.class)  //使用NioEventLoopGroup以允许非阻塞模式
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() { //指定ChannelInitializer，对于每个已接受的连接都调用它创建新的Channel
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(processor);
                        }
                    });
            System.out.println("[" + port + "] 端口监听中(Netty)...");
            ChannelFuture f = b.bind().sync();  //绑定服务器以接受连接
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();  //释放所有的资源
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
