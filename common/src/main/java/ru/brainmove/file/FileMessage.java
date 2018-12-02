package ru.brainmove.file;

import lombok.Getter;
import ru.brainmove.AbstractMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class FileMessage extends AbstractMessage {
    private final String filename;
    private final byte[] data;
    private final Long fileCounts;
    private String realFilename;

    public FileMessage(Path path) throws IOException {
        this(path, 1L);
    }

    public FileMessage(Path path, Long fileCounts) throws IOException {
        filename = path.getFileName().toString();
        data = Files.readAllBytes(path);
        this.fileCounts = fileCounts;
    }

    public FileMessage(String filename, byte[] data, Long fileCounts, String realFilename) {
        this.filename = filename;
        this.data = data;
        this.fileCounts = fileCounts;
        this.realFilename = realFilename;
    }

    public void setRealFilename(String realFilename) {
        this.realFilename = realFilename;
    }
}
