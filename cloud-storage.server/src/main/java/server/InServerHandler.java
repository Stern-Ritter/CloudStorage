package server;

import common.FileHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class InServerHandler extends ChannelInboundHandlerAdapter {
    private enum State {
        COM, AUTH_LOGIN_LENGTH, AUTH_LOGIN, AUTH_PASS, NAME_LENGTH, NAME_LENGTH_TO_DELETE, NAME_LENGTH_TO_SEND, NAME, NAME_TO_DELETE, NAME_TO_SEND, FILE_LENGTH, FILE
    }
    public enum Command {
        SEND_FILE((byte)15), SEND_FILE_LIST ((byte)16), DELETE_FILE((byte)17);
        byte firstMessageByte;

        Command(byte firstMessageByte) {
            this.firstMessageByte = firstMessageByte;
        }
    }

    private State currentState = State.COM;
    static Path userPath = Paths.get("C:", "CloudStorageServer", "user1");
    private int nameLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    private int loginLength;
    private String login;
    private int hashPass;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Server.logger.info("PROCESS: Client connected.");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Server.logger.info("PROCESS: Client disconnected.");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        ByteBuf sendBuf = null;
        while (buf.readableBytes() > 0) {

            if (currentState == State.COM) {
                byte readed = buf.readByte();
                if(readed == (byte)9){
                    currentState = State.AUTH_LOGIN_LENGTH;
                    Server.logger.info("PROCESS: Start authentication.");
                } else if (readed == (byte)14){
                    currentState = State.NAME_LENGTH_TO_SEND;
                    Server.logger.info("PROCESS: Start file sending.");
                }else if (readed == (byte) 15) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    Server.logger.info("PROCESS: Start file receiving.");
                } else if (readed == (byte) 16) {
                    sendBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                    sendBuf.writeByte((byte) 16);
                    ctx.writeAndFlush(sendBuf);
                } else if (readed == (byte) 17) {
                    currentState = State.NAME_LENGTH_TO_DELETE;
                    Server.logger.info("PROCESS: Start file deleting.");
                } else {
                    Server.logger.error("Invalid first byte.");
                }
            }

            if (currentState == State.AUTH_LOGIN_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    loginLength = buf.readInt();
                    Server.logger.info("PROCESS: Get login length.");
                    currentState = State.AUTH_LOGIN;
                }
            }

            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    nameLength = buf.readInt();
                    Server.logger.info("PROCESS: Get filename length.");
                    currentState = State.NAME;
                }
            }

            if (currentState == State.NAME_LENGTH_TO_DELETE) {
                if (buf.readableBytes() >= 4) {
                    nameLength = buf.readInt();
                    Server.logger.info("PROCESS: Get filename to delete length.");
                    currentState = State.NAME_TO_DELETE;
                }
            }

            if (currentState == State.NAME_LENGTH_TO_SEND) {
                if (buf.readableBytes() >= 4) {
                    nameLength = buf.readInt();
                    Server.logger.info("PROCESS: Get filename to send length.");
                    currentState = State.NAME_TO_SEND;
                }
            }

            if (currentState == State.AUTH_LOGIN) {
                if (buf.readableBytes() >= loginLength) {
                    byte[] fileName = new byte[loginLength];
                    buf.readBytes(fileName);
                    login = new String(fileName, StandardCharsets.UTF_8);
                    Server.logger.info("PROCESS: Login received - {} .", login);
                    currentState = State.AUTH_PASS;
                }
            }

            if (currentState == State.AUTH_PASS) {
                if (buf.readableBytes() >= 4) {
                    hashPass = buf.readInt();
                    Server.logger.info("PROCESS: Password received - {} .", hashPass);
                    sendBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                    if(CloudStorageServer.dbs.checkPassword(login,hashPass)){
                        changeUserDirectory(login);
                        Server.logger.info("PROCESS: Successfully authentication.", hashPass);
                        Server.logger.info("PROCESS: User directory: {}.", userPath);
                        sendBuf.writeByte((byte) 9);
                    } else {
                        Server.logger.info("PROCESS: Failed authentication.", hashPass);
                        sendBuf.writeByte((byte) 10);
                    }
                     ctx.writeAndFlush(sendBuf);
                    currentState = State.COM;
                }
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    Server.logger.info("PROCESS: Filename received - {} .", new String(fileName, StandardCharsets.UTF_8));
                    out = new BufferedOutputStream(new FileOutputStream(userPath.toAbsolutePath().toString() + "\\" + new String(fileName)));
                    currentState = State.FILE_LENGTH;
                }
            }

            if (currentState == State.NAME_TO_DELETE) {
                if (buf.readableBytes() >= nameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    Server.logger.info("PROCESS: Filename to delete received - {} .", new String(fileName, StandardCharsets.UTF_8));
                    try {
                        FileHandler.deleteFile(Paths.get(userPath.toAbsolutePath().toString() + "\\" + new String(fileName)));
                        Server.logger.info("PROCESS: Delete operation success.");
                        sendBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                        sendBuf.writeByte((byte) 16);
                        ctx.writeAndFlush(sendBuf);
                    } catch (IOException ex){
                        Server.logger.error("Delete operation failed.");
                    }
                    currentState = State.COM;
                }
            }

            if (currentState == State.NAME_TO_SEND) {
                if (buf.readableBytes() >= nameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    String send = new String(fileName, StandardCharsets.UTF_8);
                    Server.logger.info("PROCESS: Filename to send received - {}.", send);
                    sendBuf = ByteBufAllocator.DEFAULT.directBuffer(nameLength + 1);
                    sendBuf.writeByte((byte) 14);
                    sendBuf.writeBytes(fileName);
                    ctx.writeAndFlush(sendBuf);
                    currentState = State.COM;
                }
            }

            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    Server.logger.info("PROCESS: File length received - {}.", fileLength);
                    currentState = State.FILE;
                }
            }

            if (currentState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.COM;
                        Server.logger.info("PROCESS: File received.");
                        sendBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                        sendBuf.writeByte((byte) 16);
                        ctx.writeAndFlush(sendBuf);
                        out.close();
                        break;
                    }
                }
            }

            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public void changeUserDirectory(String path){
        userPath = Paths.get("C:", "CloudStorageServer", path);
        if(!Files.exists(userPath)){
            try {
                Files.createDirectories(userPath);
            } catch (IOException e) {
                Server.logger.error("Change user directory for {} failed.", path);
            }
        }
    }

}