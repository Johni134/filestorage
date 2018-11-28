package ru.brainmove.api;

import org.jetbrains.annotations.Nullable;
import ru.brainmove.entity.Token;
import ru.brainmove.entity.User;

public interface UserService {

    @Nullable
    User findByUser(@Nullable String login);

    boolean check(
            @Nullable String login,
            @Nullable String password
    );

    boolean registry(
            @Nullable String login,
            @Nullable String password
    );

    boolean exists(@Nullable String login);

    boolean setLogin(
            @Nullable String login,
            @Nullable String newLogin
    );

    boolean setPassword(
            @Nullable String login,
            @Nullable String passwordOld,
            @Nullable String passwordNew
    );

    Token createToken(@Nullable User user);

    boolean checkToken(@Nullable String id, @Nullable String accessToken);
}
