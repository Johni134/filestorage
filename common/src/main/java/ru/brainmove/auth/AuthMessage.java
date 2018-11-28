package ru.brainmove.auth;

import lombok.Getter;
import ru.brainmove.AbstractMessage;
import ru.brainmove.entity.Token;
import ru.brainmove.entity.User;

@Getter
public class AuthMessage extends AbstractMessage {

    private final boolean success;
    private final String errorMsg;
    private final AuthType authType;
    private final User user;
    private final Token token;

    public AuthMessage(boolean success, String errorMsg, AuthType authType, User user, Token token) {
        this.success = success;
        this.errorMsg = errorMsg;
        this.authType = authType;
        this.user = user;
        this.token = token;
    }
}
