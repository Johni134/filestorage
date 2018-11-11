package ru.brainmove;

import lombok.Getter;

@Getter
class AuthRequest extends AbstractMessage {

    private final String login;
    private final String password;
    private final AuthType authType;

    AuthRequest(String login, String password, AuthType authType) {
        this.login = login;
        this.password = password;
        this.authType = authType;
    }
}
