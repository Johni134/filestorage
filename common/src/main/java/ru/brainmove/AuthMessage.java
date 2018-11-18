package ru.brainmove;

import lombok.Getter;
import ru.brainmove.entity.User;

@Getter
public class AuthMessage extends AbstractMessage {

    private final boolean success;
    private final String errorMsg;
    private final AuthType authType;
    private final User user;

    AuthMessage(boolean success, String errorMsg, AuthType authType, User user) {
        this.success = success;
        this.errorMsg = errorMsg;
        this.authType = authType;
        this.user = user;
    }
}
