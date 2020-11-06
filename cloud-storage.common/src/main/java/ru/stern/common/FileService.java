package ru.stern.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class FileService {
    public static void deleteFile(Path path) throws IOException {
        Files.delete(path);
    }

    public static List<String> getFileList(Path path) {
        List<String> list = null;
        try {
            list = Files.list(path)
                    .filter(p -> !Files.isDirectory(p))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String fileListToString(List<String> list){
        StringBuilder sb = new StringBuilder();
        for (String s: list) {
            sb.append(s).append(";");
        }
        if (sb.length() > 2){
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static List<String> stringToFileList(String result){
        List<String> list = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(result, ";");
        while(st.hasMoreTokens()){
            list.add(st.nextToken());
        }
        return list;
    }
}