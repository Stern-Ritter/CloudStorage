import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    public enum State{
        COM, NAME_LENGTH, NAME_LENGTH_TO_DELETE, NAME, NAME_TO_DELETE, FILE_LENGTH, FILE
    }
    private State currentState = State.COM;
    Path userPath = Paths.get("C:","CloudStorageServer", "user1");
    private int nameLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        ByteBuf buf = ((ByteBuf)msg);
        while (buf.readableBytes() > 0){

            if (currentState == State.COM) {
                byte readed = buf.readByte();
                if(readed == (byte)15){
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("Start file receiving.");
                } else if(readed == (byte)16) {

                }
                else if(readed == (byte)17) {
                    currentState = State.NAME_LENGTH_TO_DELETE;
                } else {
                    System.out.println("Invalid first byte.");
                }
            }

            if(currentState == State.NAME_LENGTH){
                if(buf.readableBytes() >= 4) {
                    nameLength = buf.readInt();
                    System.out.println("Get filename length " + nameLength);
                    currentState = State.NAME;
                }
            }

            if(currentState == State.NAME_LENGTH_TO_DELETE){
                if(buf.readableBytes() >= 4) {
                    nameLength = buf.readInt();
                    System.out.println("Get filename length " + nameLength);
                    currentState = State.NAME_TO_DELETE;
                }
            }

            if(currentState == State.NAME_TO_DELETE) {
                if(buf.readableBytes() >= nameLength){
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    System.out.println("Filename received - "+ new String(fileName,"UTF-8"));
                    deleteFile(Paths.get(userPath.toAbsolutePath().toString() + "\\" + new String(fileName)));
                    currentState = State.COM;
                }
            }

            if(currentState == State.NAME){
                if(buf.readableBytes() >= nameLength){
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    System.out.println("Filename received - "+ new String(fileName,"UTF-8"));
                    out = new BufferedOutputStream(new FileOutputStream(userPath.toAbsolutePath().toString() + "\\" + new String(fileName)));
                    System.out.println(userPath.toAbsolutePath().toString() + new String(fileName));
                    currentState = State.FILE_LENGTH;
                }
            }

            if(currentState == State.FILE_LENGTH){
                if(buf.readableBytes() >=8) {
                    fileLength = buf.readLong();
                    System.out.println("File length received - " + fileLength);
                    currentState = State.FILE;
                }
            }

            if(currentState == State.FILE){
                while(buf.readableBytes() > 0){
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if(fileLength == receivedFileLength){
                        currentState = State.COM;
                        System.out.println("File received");
                        out.close();
                        break;
                    }
                }
            }

            if(buf.readableBytes() == 0) {
                buf.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        cause.printStackTrace();
        ctx.close();
    }

    public void deleteFile(Path path){
        try{
            Files.delete(path);
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public ArrayList<String> getFileList() {
        ArrayList<String> list = new ArrayList<>();
        if (Files.exists(userPath)){
            try {
                Files.walkFileTree(userPath, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        list.add(file.getFileName().toString());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return list;
    }
}
