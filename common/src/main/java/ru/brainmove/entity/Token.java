package ru.brainmove.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
public class Token implements Serializable {

    private String id;

    private String accessToken;

    private Timestamp sysdate;

    public Token(String id, String accessToken) {
        this.id = id;
        this.accessToken = accessToken;
    }

    public Token(String id, String accessToken, Timestamp sysdate) {
        this.id = id;
        this.accessToken = accessToken;
        this.sysdate = sysdate;
    }
}
