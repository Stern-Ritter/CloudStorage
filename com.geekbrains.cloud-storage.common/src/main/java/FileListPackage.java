import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileListPackage implements Serializable {
    private ArrayList<String> fileList;

    public List<String> getFileList() {
        return fileList;
    }

    public FileListPackage(ArrayList<String> fileList) {
        this.fileList = fileList;
    }
}
