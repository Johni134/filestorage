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

    public void setRealFilename(String realFilename) {
        this.realFilename = realFilename;
    }
}
