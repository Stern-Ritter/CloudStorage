package ru.stern.server;

import ru.stern.common.Commands;
import ru.stern.common.FileService;
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

    private TransferState currentState = TransferState.COMMAND;
    private Path userPath;
    private int nameLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    public InServerHandler(String login){
        userPath = Paths.get("C:", "CloudStorageServer", login);
        if(!Files.exists(userPath)){
            try {
                Files.createDirectories(userPath);
                Server.logger.info("PROCESS: User directory: {}.", userPath);
            } catch (IOException e) {
                Server.logger.error("Find user directory for {} failed.", login);
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        ByteBuf sendBuf = null;
        while (buf.readableBytes() > 0) {

            if (currentState == TransferState.COMMAND) {
                byte readed = buf.readByte();
                if(readed == Commands.FILE_REQUEST){
                    currentState = TransferState.NAME_LENGTH_TO_SEND;
                    Server.logger.info("PROCESS: Start file sending.");
                }else if (readed == Commands.FILE_TRANSFER) {
                    currentState = TransferState.NAME_LENGTH;
                    receivedFileLength = 0L;
                    Server.logger.info("PROCESS: Start file receiving.");
                } else if (readed == Commands.FILE_LIST_REQUEST) {
                    sendBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                    sendBuf.writeByte(Commands.FILE_LIST_REQUEST);
                    ctx.writeAndFlush(sendBuf);
                } else if (readed == Commands.FILE_DELETE) {
                    currentState = TransferState.NAME_LENGTH_TO_DELETE;
                    Server.logger.info("PROCESS: Start file deleting.");
                } else if(readed == Commands.DISCONNECT_REQUEST){
                    Server.logger.info("PROCESS: Start disconnect.");
                    ctx.close();
                } else {
                    Server.logger.error("Invalid first byte.");
                }
            }

            if (currentState == TransferState.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    nameLength = buf.readInt();
                    Server.logger.info("PROCESS: Get filename length.");
                    currentState = TransferState.NAME;
                }
            }

            if (currentState == TransferState.NAME_LENGTH_TO_DELETE) {
                if (buf.readableBytes() >= 4) {
                    nameLength = buf.readInt();
                    Server.logger.info("PROCESS: Get filename to delete length.");
                    currentState = TransferState.NAME_TO_DELETE;
                }
            }

            if (currentState == TransferState.NAME_LENGTH_TO_SEND) {
                if (buf.readableBytes() >= 4) {
                    nameLength = buf.readInt();
                    Server.logger.info("PROCESS: Get filename to send length.");
                    currentState = TransferState.NAME_TO_SEND;
                }
            }

            if (currentState == TransferState.NAME) {
                if (buf.readableBytes() >= nameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    Server.logger.info("PROCESS: Filename received - {} .", new String(fileName, StandardCharsets.UTF_8));
                    out = new BufferedOutputStream(new FileOutputStream(userPath.toAbsolutePath().toString() + "\\" + new String(fileName)));
                    currentState = TransferState.FILE_LENGTH;
                }
            }

            if (currentState == TransferState.NAME_TO_DELETE) {
                if (buf.readableBytes() >= nameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    Server.logger.info("PROCESS: Filename to delete received - {} .", new String(fileName, StandardCharsets.UTF_8));
                    try {
                        FileService.deleteFile(Paths.get(userPath.toAbsolutePath().toString() + "\\" + new String(fileName)));
                        Server.logger.info("PROCESS: Delete operation success.");
                        sendBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                        sendBuf.writeByte((byte) 16);
                        ctx.writeAndFlush(sendBuf);
                    } catch (IOException ex){
                        Server.logger.error("Delete operation failed.");
                    }
                    currentState = TransferState.COMMAND;
                }
            }

            if (currentState == TransferState.NAME_TO_SEND) {
                if (buf.readableBytes() >= nameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    String send = new String(fileName, StandardCharsets.UTF_8);
                    Server.logger.info("PROCESS: Filename to send received - {}.", send);
                    sendBuf = ByteBufAllocator.DEFAULT.directBuffer(nameLength + 1);
                    sendBuf.writeByte((byte) 14);
                    sendBuf.writeBytes(fileName);
                    ctx.writeAndFlush(sendBuf);
                    currentState = TransferState.COMMAND;
                }
            }

            if (currentState == TransferState.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    Server.logger.info("PROCESS: File length received - {}.", fileLength);
                    currentState = TransferState.FILE;
                }
            }

            if (currentState == TransferState.FILE) {
                if(receivedFileLength == 0){
                    currentState = TransferState.COMMAND;
                    Server.logger.info("PROCESS: File received.");
                    sendBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                    sendBuf.writeByte((byte) 16);
                    ctx.writeAndFlush(sendBuf);
                    out.close();
                    break;
                }
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = TransferState.COMMAND;
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