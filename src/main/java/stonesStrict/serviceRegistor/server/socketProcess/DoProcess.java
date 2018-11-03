package stonesStrict.serviceRegistor.server.socketProcess;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import stonesStrict.serviceRegistor.registor.ServiceManager;

import java.lang.reflect.Method;

public class DoProcess {

    /**
     * 获取请求的服务内容
     * 处理请求并返回响应
     * @param content
     * @return
     * @throws Exception
     */
    public static Object doProcess(String content) throws Exception {
        if(StringUtils.isBlank(content)) {
            return null;
        }
        RpcRequest req = JSONObject.parseObject(content, RpcRequest.class);
        Object server = ServiceManager.getServer(req.getServiceName(), req.getVersion());
        if(server == null) {
            throw new RuntimeException("访问的服务不存在: serviceName(" + req.getServiceName() + ") version(" + req.getVersion() + ")");
        }
        Class[] classes = new Class[req.getArgs().length];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = req.getArgs()[i].getClass();
        }

        Method method = server.getClass().getMethod(req.getMethod(), classes);
        if(method == null) {
            throw new RuntimeException("访问的服务不存在: serviceName(" + req.getServiceName() + ") version(" + req.getVersion() + ")");
        }
        Object resultData = method.invoke(server, req.getArgs());
        return resultData;
    }

}
