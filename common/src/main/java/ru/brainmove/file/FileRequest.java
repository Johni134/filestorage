package ru.brainmove.file;

import lombok.Getter;
import ru.brainmove.AbstractMessage;

@Getter
public class FileRequest extends AbstractMessage {
    private final String filename;
    private final boolean forDeleting;

    public FileRequest(String filename) {
        this.filename = filename;
        this.forDeleting = false;
    }

    public FileRequest(String filename, boolean forDeleting) {
        this.filename = filename;
        this.forDeleting = forDeleting;
    }
}
