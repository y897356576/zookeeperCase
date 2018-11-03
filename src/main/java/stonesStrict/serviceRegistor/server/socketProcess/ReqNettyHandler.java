package stonesStrict.serviceRegistor.server.socketProcess;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class ReqNettyHandler extends ChannelHandlerAdapter {

    /**
     * 在与一个客户端连接时触发
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("服务端与客户端 " + ctx.channel().toString() + " 建立链接\r\n");
    }

    /**
     *
     * @param ctx 与客户端通信的通道
     * @param msg 客户端发送的信息主体
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String content = ((ByteBuf) msg).toString(CharsetUtil.UTF_8);
        System.out.println("服务端收到的客户端 " + ctx.channel().toString() + " 的消息是：" + content);

        Object resultData = DoProcess.doProcess(content);

        JSONObject json = new JSONObject();
        json.put("result", "SUCCESS");
        json.put("data", resultData);
        System.out.println("resultData : " + json.toJSONString());

        ByteBuf responseBuf = Unpooled.copiedBuffer(JSONObject.toJSONString(json).getBytes());
        //通过与客户端的通道回写信息
        ctx.write(responseBuf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //1、向客户端写入空字符串
        //2、向客户端发送写入的信息
        //3、添加监听，促使客户端关闭链接
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("主机：" + ctx.channel().remoteAddress()+ " 出现异常；" + cause.getMessage());
        ctx.close();
    }
}
