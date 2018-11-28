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

    public FileMessage(Path path) throws IOException {
        filename = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }
}
