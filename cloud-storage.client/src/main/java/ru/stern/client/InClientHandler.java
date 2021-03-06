package ru.stern.client;

import ru.stern.common.Commands;
import ru.stern.common.FileService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class InClientHandler extends ChannelInboundHandlerAdapter {

    private TransferState currentState = TransferState.COMMAND;
    static CloudStorageController cloudStorageController;
    static  AuthController authController;
    private static Path clientPath = Paths.get("C:","CloudStorage");
    private int nameLength;
    private long fileLength;
    private int fileListLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {

            if (currentState == TransferState.COMMAND) {
                byte readed = buf.readByte();
                if(readed == Commands.REG_SUCCESS){
                    authController.failedAction("Registration", "Registration was successful.");
                    System.out.println("PROCESS: Received from server successfully registration.");
                } else if(readed == Commands.REG_FAILED){
                    authController.failedAction("Registration", "Registration failed. Try a different username.");
                    System.out.println("PROCESS: Received from server failed registration.");
                } else if (readed == Commands.AUTH_SUCCESS) {
                    authController.showCloudStorageWindow();
                    System.out.println("PROCESS: Received from server successfully authentication.");
                } else  if (readed == Commands.AUTH_FAILED) {
                    authController.failedAction("Authentication", "Invalid username or password.");
                    System.out.println("PROCESS: Received from server failed authentication.");
                } else if (readed == Commands.FILE_REQUEST) {
                    currentState = TransferState.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("PROCESS: Start file receiving.");
                } else if (readed == Commands.FILE_LIST_REQUEST) {
                    currentState = TransferState.FILE_LIST_LENGTH;
                    System.out.println("PROCESS: Start file list receiving.");
                } else {
                    System.out.println("ERROR: Invalid first byte.");
                }
            }

            if (currentState == TransferState.FILE_LIST_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    fileListLength = buf.readInt();
                    System.out.println("PROCESS: File list length received - " + fileLength);
                    currentState = TransferState.FILE_LIST;
                }
            }

            if (currentState == TransferState.FILE_LIST) {
                if (buf.readableBytes() >= fileListLength) {
                    byte[] arr = new byte[fileListLength];
                    buf.readBytes(arr);
                    String result = new String(arr);
                    List<String> list = FileService.stringToFileList(result);
                    cloudStorageController.updateServerFileList(list);
                    currentState = TransferState.COMMAND;
                    System.out.println("PROCESS: File list received.");
                }
            }

            if (currentState == TransferState.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    nameLength = buf.readInt();
                    System.out.println("PROCESS: Get filename length.");
                    currentState = TransferState.NAME;
                }
            }

            if (currentState == TransferState.NAME) {
                if (buf.readableBytes() >= nameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    System.out.println("PROCESS: Filename received - " + new String(fileName, StandardCharsets.UTF_8));
                    out = new BufferedOutputStream(new FileOutputStream(clientPath.toAbsolutePath().toString() + "\\" + new String(fileName)));
                    currentState = TransferState.FILE_LENGTH;
                }
            }

            if (currentState == TransferState.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("PROCESS: File length received - " + fileLength);
                    currentState = TransferState.FILE;
                }
            }

            if (currentState == TransferState.FILE) {
                if(receivedFileLength == 0) {
                    currentState = TransferState.COMMAND;
                    System.out.println("PROCESS: File received.");
                    cloudStorageController.updateFileList();
                    out.close();
                    break;
                }
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = TransferState.COMMAND;
                        System.out.println("PROCESS: File received.");
                        cloudStorageController.updateFileList();
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
}