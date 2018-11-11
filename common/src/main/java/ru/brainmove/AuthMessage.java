package ru.brainmove;

import lombok.Getter;

@Getter
class AuthMessage extends AbstractMessage {

    private final boolean success;
    private final String errorMsg;
    private final AuthType authType;

    AuthMessage(boolean success, String errorMsg, AuthType authType) {
        this.success = success;
        this.errorMsg = errorMsg;
        this.authType = authType;
    }
}
