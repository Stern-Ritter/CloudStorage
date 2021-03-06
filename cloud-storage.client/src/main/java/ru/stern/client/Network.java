package ru.stern.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.CountDownLatch;

public class Network {
    private final static String IP_ADDRESS = "localhost";
    private final static int PORT = 8189;
    private static Network connector = new Network();
    private Channel currentChannel;

    public static Network getInstance(){
        return connector;
    }

    private Network(){
    }

    public Channel getCurrentChannel(){
        return currentChannel;
    }

    public void start(CountDownLatch connectionStarted){
        new Thread(() -> {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap clientBootstrap = new Bootstrap();
                clientBootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .remoteAddress(IP_ADDRESS, PORT)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline().addLast(new InClientHandler());
                                currentChannel = socketChannel;
                            }
                        });
                ChannelFuture channelFuture = clientBootstrap.connect().sync();
                connectionStarted.countDown();
                channelFuture.channel().closeFuture().sync();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }
        }).start();
    }

    public void stop (){
        currentChannel.close();
        System.out.println("PROCESS: Connection close.");
    }
}
