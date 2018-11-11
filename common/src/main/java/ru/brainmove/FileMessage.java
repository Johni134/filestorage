package ru.brainmove;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
class FileMessage extends AbstractMessage {
    private final String filename;
    private final byte[] data;

    FileMessage(Path path) throws IOException {
        filename = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }
}
