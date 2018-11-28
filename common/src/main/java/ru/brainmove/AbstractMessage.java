package ru.brainmove;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public abstract class AbstractMessage implements Serializable {
    private String accessToken;
    private String id;
}
