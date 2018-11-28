package ru.brainmove.auth;

import lombok.Getter;
import ru.brainmove.AbstractMessage;

@Getter
public class AuthRequest extends AbstractMessage {

    private final String login;
    private final String password;
    private final AuthType authType;

    public AuthRequest(String login, String password, AuthType authType) {
        this.login = login;
        this.password = password;
        this.authType = authType;
    }
}
