package ru.brainmove.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileUtils {

    public static final int MAX_BYTE_SIZE = 1024 * 1024;

    public static void createFileAndDirectories(String path, FileMessage fm) throws IOException {
        final Path filePath = Paths.get(path + fm.getFilename());
        final Path dirPath = Paths.get(path);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        Files.createDirectories(dirPath);
        Files.write(filePath, fm.getData(), StandardOpenOption.CREATE);
    }

    public static boolean createFileFromTemp(String path, String finalPath, FileMessage fm) throws IOException {
        boolean allFilesDone = false;
        long fileCounts = Files.list(Paths.get(path)).map(p -> p.getFileName().toString()).filter(p -> p.startsWith(fm.getRealFilename())).count();
        if (fm.getFileCounts().equals(fileCounts)) {
            final Path newFilePath = Paths.get(finalPath + fm.getRealFilename());
            final Path newDirPath = Paths.get(finalPath);
            if (Files.exists(newFilePath))
                Files.delete(newFilePath);
            Files.createDirectories(newDirPath);
            Files.list(Paths.get(path)).filter(p -> p.getFileName().toString().startsWith(fm.getRealFilename())).forEachOrdered(p -> {
                try {
                    Files.write(newFilePath, Files.readAllBytes(p), (!Files.exists(newFilePath) ? StandardOpenOption.CREATE : StandardOpenOption.APPEND));
                    Files.delete(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            allFilesDone = true;
        }
        return allFilesDone;
    }
}
