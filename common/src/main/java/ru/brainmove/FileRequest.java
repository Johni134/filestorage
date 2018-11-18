package ru.brainmove;

import lombok.Getter;

@Getter
public class FileRequest extends AbstractMessage {
    private final String filename;

    public FileRequest(String filename) {
        this.filename = filename;
    }
}
