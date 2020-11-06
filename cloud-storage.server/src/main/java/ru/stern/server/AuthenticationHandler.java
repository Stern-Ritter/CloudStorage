package ru.stern.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.stern.common.Commands;

import java.nio.charset.StandardCharsets;

public class AuthenticationHandler extends ChannelInboundHandlerAdapter {

    private DatabaseService dbs;
    private AuthState currentState = AuthState.COMMAND;
    private String login;
    private int loginLength;
    private int passwordLength;

    public AuthenticationHandler(DatabaseService dbs){
        this.dbs = dbs;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        ByteBuf sendBuf = null;
        while (buf.readableBytes() > 0) {
            if (currentState == AuthState.COMMAND) {
                byte readed = buf.readByte();
                if(readed == Commands.AUTH_REQUEST) {
                    currentState = AuthState.AUTH_LOGIN_LENGTH;
                    Server.logger.info("PROCESS: Start authentication.");
                } else if(readed == Commands.REG_REQUEST){
                    currentState = AuthState.REG_LOGIN_LENGTH;
                    Server.logger.info("PROCESS: Start registration.");
                } else if (readed == Commands.DISCONNECT_REQUEST){
                    Server.logger.info("PROCESS: Start disconnect.");
                    ctx.close();
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

            if (currentState == AuthState.REG_LOGIN_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    loginLength = buf.readInt();
                    Server.logger.info("PROCESS: Get login length.");
                    currentState = AuthState.REG_LOGIN;
                }
            }

            if (currentState == AuthState.AUTH_LOGIN) {
                if (buf.readableBytes() >= loginLength) {
                    byte[] fileName = new byte[loginLength];
                    buf.readBytes(fileName);
                    login = new String(fileName, StandardCharsets.UTF_8);
                    Server.logger.info("PROCESS: Login received - {} .", login);
                    currentState = AuthState.AUTH_PASS_LENGTH;
                }
            }

            if (currentState == AuthState.REG_LOGIN) {
                if (buf.readableBytes() >= loginLength) {
                    byte[] fileName = new byte[loginLength];
                    buf.readBytes(fileName);
                    login = new String(fileName, StandardCharsets.UTF_8);
                    Server.logger.info("PROCESS: Login received - {} .", login);
                    currentState = AuthState.REG_PASS_LENGTH;
                }
            }

            if (currentState == AuthState.AUTH_PASS_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    passwordLength = buf.readInt();
                    Server.logger.info("PROCESS: Get password length.");
                    currentState = AuthState.AUTH_PASS;
                }
            }

            if (currentState == AuthState.REG_PASS_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    passwordLength = buf.readInt();
                    Server.logger.info("PROCESS: Get password length.");
                    currentState = AuthState.REG_PASS;
                }
            }

            if (currentState == AuthState.AUTH_PASS) {
                if (buf.readableBytes() >= passwordLength) {
                    byte[] hashPasswordBytes = new byte[passwordLength];
                    buf.readBytes(hashPasswordBytes);
                    String hashPassword = new String(hashPasswordBytes, StandardCharsets.UTF_8);
                    Server.logger.info("PROCESS: Password received - {} .", hashPassword);
                    sendBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                    if(dbs.checkPassword(login, hashPassword)){
                        ctx.pipeline().addLast(new InServerHandler(login));
                        ctx.pipeline().get(OutServerHandler.class).setUserPath(login);
                        ctx.pipeline().remove(this);
                        sendBuf.writeByte(Commands.AUTH_SUCCESS);
                        Server.logger.info("PROCESS: Successfully authentication - {}.", login);
                    } else {
                        Server.logger.info("PROCESS: Failed authentication.");
                        sendBuf.writeByte(Commands.AUTH_FAILED);
                    }
                    ctx.writeAndFlush(sendBuf);
                    currentState = AuthState.COMMAND;
                }
            }

            if (currentState == AuthState.REG_PASS) {
                if (buf.readableBytes() >= passwordLength) {
                    byte[] hashPasswordBytes = new byte[passwordLength];
                    buf.readBytes(hashPasswordBytes);
                    String hashPassword = new String(hashPasswordBytes, StandardCharsets.UTF_8);
                    Server.logger.info("PROCESS: Password received - {} .", hashPassword);
                    sendBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                    if(dbs.checkUserExistence(login)){
                        sendBuf.writeByte(Commands.REG_FAILED);
                        Server.logger.info("PROCESS: The user {} is already registered.", login);
                    } else {
                        if(dbs.insertNewUser(login, hashPassword)) {
                            sendBuf.writeByte(Commands.REG_SUCCESS);
                            Server.logger.info("PROCESS: Successfully registration - {}.", login);
                        } else {
                            sendBuf.writeByte(Commands.REG_FAILED);
                        }
                    }
                    ctx.writeAndFlush(sendBuf);
                    currentState = AuthState.COMMAND;
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

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Server.logger.info("PROCESS: Client disconnected.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Server.logger.error("Connection error.");
        ctx.close();
    }
}
