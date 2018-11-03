package stonesStrict.serviceRegistor.client.SendRequest;

import com.alibaba.fastjson.JSONObject;
import stonesStrict.serviceRegistor.registor.ServiceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SendRequest {

    public static Object sendRequest(RpcRequest request) {
        Object result = null;
        String address = ServiceManager.getAddress(request.getServiceName(), request.getVersion());
        String host = address.split(":")[0];
        Integer port = Integer.valueOf(address.split(":")[1]);

        try {
            Socket socket = new Socket(host, port);
            OutputStream os = socket.getOutputStream();
            os.write(JSONObject.toJSONString(request).getBytes());
            socket.shutdownOutput();
            System.out.println("发送请求至->[" + address + "]");
            System.out.println("请求内容为：" + JSONObject.toJSONString(request));

            InputStream is = socket.getInputStream();
            byte[] bytes = new byte[1024];
            StringBuilder sb = new StringBuilder();
            while (is.read(bytes) != -1) {
                sb.append(new String(bytes, "utf-8"));
            }
            socket.shutdownInput();
            System.out.println("请求响应为：" + sb.toString());
            result = sb.toString();

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
