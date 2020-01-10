package org.aoju.bus.metric.builtin;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.aoju.bus.logger.Logger;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * 启动后监听
 *
 * @author tanghc
 */
public class NettyServerListener implements ChannelFutureListener {

    private NettyServer nettyServer;

    public NettyServerListener(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    @Override
    public void operationComplete(ChannelFuture future) {
        this.addShutdownListner();
    }

    // 进程关闭后监听
    protected void addShutdownListner() {
        SignalHandler signalHandler = signal -> {
            Logger.info("收到进程信号量：{}", signal.getName());
            Thread t = new Thread(new ShutdownHook(), "ShutdownHookServer-Thread");
            Runtime.getRuntime().addShutdownHook(t);
            Runtime.getRuntime().exit(0);
        };
        Signal.handle(new Signal(getOSSignalKill()), signalHandler);
    }

    class ShutdownHook implements Runnable {
        @Override
        public void run() {
            Logger.info("进程退出，关闭Netty服务端...");
            nettyServer.shutdown();
        }
    }

    private String getOSSignalKill() {
        return System.getProperties().getProperty("os.name").
                toLowerCase().startsWith("win")
                ? System.getProperty("term.sig", "INT") //windows下 Ctrl + C
                : System.getProperty("term.sig", "USR2"); // Linux下（等价于kill -12 pid
    }
    
}
