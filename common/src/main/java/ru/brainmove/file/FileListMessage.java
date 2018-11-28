package ru.brainmove.file;

import lombok.Getter;
import ru.brainmove.AbstractMessage;

import java.util.List;

@Getter
public class FileListMessage extends AbstractMessage {

    private List<String> fileList;

    public FileListMessage(List<String> fileList) {
        this.fileList = fileList;
    }
}
