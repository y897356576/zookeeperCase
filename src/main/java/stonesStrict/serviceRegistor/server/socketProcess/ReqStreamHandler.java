package stonesStrict.serviceRegistor.server.socketProcess;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ReqStreamHandler {

    /**
     * 通过Socket获取请求的数据流
     * 获取请求的服务内容并处理
     * @param socket
     */
    public static void socketHandler(Socket socket) {
        try {
            InputStream is = socket.getInputStream();
            byte[] bytes = new byte[1024];
            StringBuilder sb = new StringBuilder();
            while (is.read(bytes) != -1) {
                sb.append(new String(bytes, "utf-8"));
            }
            socket.shutdownInput();

            //获取请求的服务内容并处理
            Object resultData = DoProcess.doProcess(sb.toString());

            JSONObject json = new JSONObject();
            json.put("result", "SUCCESS");
            json.put("data", resultData);
            System.out.println("resultData : " + json.toJSONString());

            OutputStream os = socket.getOutputStream();
            os.write(JSONObject.toJSONString(json).getBytes());
            socket.shutdownOutput();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
