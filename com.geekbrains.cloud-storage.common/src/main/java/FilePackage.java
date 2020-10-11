import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FilePackage implements Serializable {
    private String filename;
    private byte[] data;

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public FilePackage(String path, String filename) throws IOException {
        this.filename = filename;
        this.data = Files.readAllBytes(Paths.get(path));
    }
    public FilePackage(String filename) throws IOException {
        this.filename = filename;
    }
}
