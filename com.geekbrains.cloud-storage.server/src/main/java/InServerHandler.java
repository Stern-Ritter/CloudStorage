import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;

public class InServerHandler extends ChannelInboundHandlerAdapter {
    public enum State {
        COM, NAME_LENGTH, NAME_LENGTH_TO_DELETE, NAME_LENGTH_TO_SEND, NAME, NAME_TO_DELETE, NAME_TO_SEND, FILE_LENGTH, FILE
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

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("PROCESS: Client connected.");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("PROCESS: Client disconnected.");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        ByteBuf sendBuf = null;
        while (buf.readableBytes() > 0) {

            if (currentState == State.COM) {
                byte readed = buf.readByte();
                if(readed == (byte)14){
                    currentState = State.NAME_LENGTH_TO_SEND;
                    System.out.println("PROCESS: Start file sending.");
                }
                if (readed == (byte) 15) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("PROCESS: Start file receiving.");
                } else if (readed == (byte) 16) {
                    sendBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                    sendBuf.writeByte((byte) 16);
                    ctx.writeAndFlush(sendBuf);
                } else if (readed == (byte) 17) {
                    currentState = State.NAME_LENGTH_TO_DELETE;
                    System.out.println("PROCESS: Start file deleting.");
                } else {
                    System.out.println("ERROR: Invalid first byte.");
                }
            }

            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    nameLength = buf.readInt();
                    System.out.println("PROCESS: Get filename length.");
                    currentState = State.NAME;
                }
            }

            if (currentState == State.NAME_LENGTH_TO_DELETE) {
                if (buf.readableBytes() >= 4) {
                    nameLength = buf.readInt();
                    System.out.println("PROCESS: Get filename to delete length.");
                    currentState = State.NAME_TO_DELETE;
                }
            }

            if (currentState == State.NAME_LENGTH_TO_SEND) {
                if (buf.readableBytes() >= 4) {
                    nameLength = buf.readInt();
                    System.out.println("PROCESS: Get filename to send length.");
                    currentState = State.NAME_TO_SEND;
                }
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    System.out.println("PROCESS: Filename received - " + new String(fileName, "UTF-8"));
                    out = new BufferedOutputStream(new FileOutputStream(userPath.toAbsolutePath().toString() + "\\" + new String(fileName)));
                    currentState = State.FILE_LENGTH;
                }
            }

            if (currentState == State.NAME_TO_DELETE) {
                if (buf.readableBytes() >= nameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    System.out.println("PROCESS: Filename to delete received - " + new String(fileName, "UTF-8"));
                    try {
                        FileHandler.deleteFile(Paths.get(userPath.toAbsolutePath().toString() + "\\" + new String(fileName)));
                        System.out.println("PROCESS: Delete operation success.");
                        sendBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                        sendBuf.writeByte((byte) 16);
                        ctx.writeAndFlush(sendBuf);
                    } catch (IOException ex){
                        System.out.println("ERROR: Delete operation failed.");
                    }
                    currentState = State.COM;
                }
            }

            if (currentState == State.NAME_TO_SEND) {
                if (buf.readableBytes() >= nameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    String send = new String(fileName, "UTF-8");
                    System.out.println("PROCESS: Filename to send received - " + send);
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
}