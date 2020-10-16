public class ClientHandler {


                        if(num == 16){
                            out.writeByte(16);
                            FileListPackage outputPackage = getFileList();
                            out.writeObject(outputPackage);
                        }

}
