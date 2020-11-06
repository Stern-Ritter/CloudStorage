package ru.stern.server;

import ru.stern.common.Сommands;
import ru.stern.common.FileHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OutServerHandler extends ChannelOutboundHandlerAdapter {
    private Path userPath;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf rec = ((ByteBuf) msg);
        byte readed = rec.readByte();
        if(readed == Сommands.FILE_REQUEST){
            byte[] arr = new byte[rec.readableBytes()];
            rec.readBytes(arr);
            String fileName = new String(arr);
            Path filePath = Paths.get(userPath.toString() + "\\"+ fileName);
            FileRegion region = new DefaultFileRegion(filePath.toFile(), 0, Files.size(filePath));
            ByteBuf buf = null;
            //Записываем в поток сигнальный байт
            buf = ByteBufAllocator.DEFAULT.directBuffer(1);
            buf.writeByte(Сommands.FILE_REQUEST);
            ctx.writeAndFlush(buf);
            //Записываем в поток длинну имени файла
            byte[] filenameBytes = fileName.getBytes(StandardCharsets.UTF_8);
            buf = ByteBufAllocator.DEFAULT.directBuffer(4);
            buf.writeInt(filenameBytes.length);
            ctx.writeAndFlush(buf);
            //Записываем в поток имя файла
            buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
            buf.writeBytes(filenameBytes);
            ctx.writeAndFlush(buf);
            //Записываем в поток длинну файла
            buf = ByteBufAllocator.DEFAULT.directBuffer(8);
            buf.writeLong(Files.size(filePath));
            ctx.writeAndFlush(buf);
            //Записываем в поток файл zero-copy file transfer
            ctx.writeAndFlush(region);
        }
        if(readed == Сommands.FILE_LIST_REQUEST) {
            Server.logger.info("PROCESS: Start file list sending.");
            String result = FileHandler.fileListToString(FileHandler.getFileList(userPath));
            byte[] send = result.getBytes();
            ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(send.length + 5);
            buf.writeByte(Сommands.FILE_LIST_REQUEST);
            buf.writeInt(send.length);
            buf.writeBytes(send);
            ctx.writeAndFlush(buf);
            Server.logger.info("PROCESS: File list sending success.");
        }
        if(readed == Сommands.AUTH_SUCCESS) {
            ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1);
            buf.writeByte(Сommands.AUTH_SUCCESS);
            ctx.writeAndFlush(buf);
            Server.logger.info("PROCESS: Send client: successfully authentication.");
        }
        if(readed == Сommands.AUTH_FAILED) {
            ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1);
            buf.writeByte(Сommands.AUTH_FAILED);
            ctx.writeAndFlush(buf);
            Server.logger.info("PROCESS: Send client: failed authentication.");
        }
        if(readed == Сommands.REG_SUCCESS) {
            ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1);
            buf.writeByte(Сommands.REG_SUCCESS);
            ctx.writeAndFlush(buf);
            Server.logger.info("PROCESS: Send client: successfully registration.");
        }
        if(readed == Сommands.REG_FAILED) {
            ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1);
            buf.writeByte(Сommands.REG_FAILED);
            ctx.writeAndFlush(buf);
            Server.logger.info("PROCESS: Send client: failed registration.");
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public void setUserPath(String login) {
        userPath = Paths.get("C:", "CloudStorageServer", login);
    }

}
