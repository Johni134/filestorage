package ru.brainmove;

import lombok.Getter;

import java.util.List;

@Getter
public class FileListMessage extends AbstractMessage {

    List<String> fileList;

    FileListMessage(List<String> fileList) {
        this.fileList = fileList;
    }
}
