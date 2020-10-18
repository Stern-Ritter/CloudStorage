import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class InClientHandler extends ChannelInboundHandlerAdapter {
    public enum State {
        COM, NAME_LENGTH, NAME, FILE_LIST_LENGTH, FILE_LENGTH, FILE, FILE_LIST
    }

    private State currentState = State.COM;
    static CloudStorageController cloudStorageController;
    static Path clientPath = Paths.get("C:","CloudStorage");
    private int nameLength;
    private long fileLength;
    private int fileListLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {

            if (currentState == State.COM) {
                byte readed = buf.readByte();
                if (readed == (byte) 14) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("PROCESS: Start file receiving.");
                } else if (readed == (byte) 16) {
                    currentState = State.FILE_LIST_LENGTH;
                    System.out.println("PROCESS: Start file list receiving.");
                } else {
                    System.out.println("ERROR: Invalid first byte.");
                }
            }

            if (currentState == State.FILE_LIST_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    fileListLength = buf.readInt();
                    System.out.println("PROCESS: File list length received - " + fileLength);
                    currentState = State.FILE_LIST;
                }
            }

            if (currentState == State.FILE_LIST) {
                if (buf.readableBytes() >= fileListLength) {
                    byte[] arr = new byte[fileListLength];
                    buf.readBytes(arr);
                    String result = new String(arr);
                    List<String> list = FileHandler.stringToFileList(result);
                    Platform.runLater(() -> {
                        cloudStorageController.updateServerFileList(list);
                    });
                    currentState = State.COM;
                    System.out.println("PROCESS: File list received.");
                }
            }

            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    nameLength = buf.readInt();
                    System.out.println("PROCESS: Get filename length.");
                    currentState = State.NAME;
                }
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    System.out.println("PROCESS: Filename received - " + new String(fileName, "UTF-8"));
                    out = new BufferedOutputStream(new FileOutputStream(clientPath.toAbsolutePath().toString() + "\\" + new String(fileName)));
                    currentState = State.FILE_LENGTH;
                }
            }

            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("PROCESS: File length received - " + fileLength);
                    currentState = State.FILE;
                }
            }

            if (currentState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.COM;
                        System.out.println("PROCESS: File received.");
                        Platform.runLater(() -> {
                            cloudStorageController.updateFileList();
                        });
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