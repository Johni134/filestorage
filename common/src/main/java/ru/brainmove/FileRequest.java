package ru.brainmove;

import lombok.Getter;

@Getter
class FileRequest extends AbstractMessage {
    private final String filename;

    FileRequest(String filename) {
        this.filename = filename;
    }
}
