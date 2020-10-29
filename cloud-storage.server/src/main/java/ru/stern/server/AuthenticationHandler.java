package ru.stern.server;

import ru.stern.common.Сommands;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

public class AuthenticationHandler extends ChannelInboundHandlerAdapter {

    private DatabaseService dbs;
    private AuthState currentState = AuthState.COM;
    private String login;
    private int loginLength;

    public AuthenticationHandler(DatabaseService dbs){
        this.dbs = dbs;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        ByteBuf sendBuf = null;
        while (buf.readableBytes() > 0) {
            if (currentState == AuthState.COM) {
                byte readed = buf.readByte();
                if(readed == Сommands.AUTH_REQUEST) {
                    currentState = AuthState.AUTH_LOGIN_LENGTH;
                    Server.logger.info("PROCESS: Start authentication.");
                } else {
                    Server.logger.error("Invalid first byte.");
                }
            }

            if (currentState == AuthState.AUTH_LOGIN_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    loginLength = buf.readInt();
                    Server.logger.info("PROCESS: Get login length.");
                    currentState = AuthState.AUTH_LOGIN;
                }
            }

            if (currentState == AuthState.AUTH_LOGIN) {
                if (buf.readableBytes() >= loginLength) {
                    byte[] fileName = new byte[loginLength];
                    buf.readBytes(fileName);
                    login = new String(fileName, StandardCharsets.UTF_8);
                    Server.logger.info("PROCESS: Login received - {} .", login);
                    currentState = AuthState.AUTH_PASS;
                }
            }

            if (currentState == AuthState.AUTH_PASS) {
                if (buf.readableBytes() >= 4) {
                    int hashPass = buf.readInt();
                    Server.logger.info("PROCESS: Password received - {} .", hashPass);
                    sendBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                    if(dbs.checkPassword(login, hashPass)){
                        ctx.pipeline().addLast(new InServerHandler(login));
                        ctx.pipeline().get(OutServerHandler.class).setUserPath(login);
                        ctx.pipeline().remove(this);
                        sendBuf.writeByte(Сommands.AUTH_SUCCESS);
                        Server.logger.info("PROCESS: Successfully authentication - {}.", login);
                    } else {
                        Server.logger.info("PROCESS: Failed authentication.");
                        sendBuf.writeByte(Сommands.AUTH_FAILED);
                    }
                    ctx.writeAndFlush(sendBuf);
                    currentState = AuthState.COM;
                }
            }

            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Server.logger.info("PROCESS: Client connected.");
    }
}
