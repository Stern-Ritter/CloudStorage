package ru.stern.client;

import ru.stern.common.Commands;
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
        buf.writeByte(Commands.FILE_TRANSFER);
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
        buf.writeByte(Commands.FILE_DELETE);
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
        buf.writeByte(Commands.FILE_LIST_REQUEST);
        channel.writeAndFlush(buf);
    }

    public static void sendFileRequest(String path, Channel channel){
        ByteBuf buf = null;
        //Записываем в поток сигнальный байт
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(Commands.FILE_REQUEST);
        channel.writeAndFlush(buf);
        //Записываем в поток длинну имени файла
        byte[] filenameBytes = path.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filenameBytes.length);
        channel.writeAndFlush(buf);
        //Записываем в поток имя файла
        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);
    }

    public static void sendLoginPassword(byte command, String login, String hashPassword, Channel channel){
        ByteBuf buf = null;
        //Записываем в поток сигнальный байт
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(command);
        channel.writeAndFlush(buf);
        //Записываем в поток длинну логина
        byte[] loginBytes = login.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(loginBytes.length);
        channel.writeAndFlush(buf);
        //Записываем в поток логин
        buf = ByteBufAllocator.DEFAULT.directBuffer(loginBytes.length);
        buf.writeBytes(loginBytes);
        channel.writeAndFlush(buf);
        //Записываем в поток длинну хэшированного пароля
        byte[] hashPasswordBytes = hashPassword.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(hashPasswordBytes.length);
        channel.writeAndFlush(buf);
        //Записываем в поток пароль
        buf= ByteBufAllocator.DEFAULT.directBuffer(hashPasswordBytes.length);
        buf.writeBytes(hashPasswordBytes);
        channel.writeAndFlush(buf);
    }

    public static void sendDisconnectRequest(Channel channel){
        ByteBuf buf = null;
        //Записываем в поток сигнальный байт
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(Commands.DISCONNECT_REQUEST);
        channel.writeAndFlush(buf);
    }
}
