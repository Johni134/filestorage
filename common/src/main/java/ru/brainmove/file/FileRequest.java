package ru.brainmove.file;

import lombok.Getter;
import ru.brainmove.AbstractMessage;

@Getter
public class FileRequest extends AbstractMessage {
    private final String filename;

    public FileRequest(String filename) {
        this.filename = filename;
    }
}
