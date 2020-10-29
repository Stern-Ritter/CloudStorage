package ru.stern.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CommandSender {

    public static void sendFile(Path path, Channel channel) throws IOException {
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        ByteBuf buf = null;
        //Записываем в поток сигнальный байт
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 15);
        channel.writeAndFlush(buf);
        //Записываем в поток длинну имени файла
        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filenameBytes.length);
        channel.writeAndFlush(buf);
        //Записываем в поток имя файла
        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);
        //Записываем в поток длинну файла
        buf = ByteBufAllocator.DEFAULT.directBuffer(8);
        buf.writeLong(Files.size(path));
        channel.writeAndFlush(buf);
        //Записываем в поток файл zero-copy file transfer
        channel.writeAndFlush(region);
    }

    public static void sendDeleteCommand(String filename, Channel channel){
        ByteBuf buf = null;
        //Записываем в поток сигнальный байт
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte)17);
        channel.writeAndFlush(buf);
        //Записываем в поток длинну имени файла
        byte[] fileNameBytes = filename.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(fileNameBytes.length);
        channel.writeAndFlush(buf);
        //Записываем в поток имя файла
        buf = ByteBufAllocator.DEFAULT.directBuffer(fileNameBytes.length);
        buf.writeBytes(fileNameBytes);
        channel.writeAndFlush(buf);
    }

    public static void sendFileListRequest(Channel channel){
        ByteBuf buf = null;
        //Записываем в поток сигнальный байт
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte)16);
        channel.writeAndFlush(buf);
    }

    public static void sendFileRequest(String path, Channel channel){
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 14);
        channel.writeAndFlush(buf);
        byte[] filenameBytes = path.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filenameBytes.length);
        channel.writeAndFlush(buf);
        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);
    }

    public static void sendLoginPassword(String login, int hashPassword, Channel channel){
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte)9);
        channel.writeAndFlush(buf);
        byte[] loginBytes = login.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(loginBytes.length);
        channel.writeAndFlush(buf);
        buf = ByteBufAllocator.DEFAULT.directBuffer(loginBytes.length);
        buf.writeBytes(loginBytes);
        channel.writeAndFlush(buf);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(hashPassword);
        channel.writeAndFlush(buf);
    }
}
