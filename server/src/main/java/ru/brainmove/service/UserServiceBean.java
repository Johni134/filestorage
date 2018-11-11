package ru.brainmove.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mindrot.jbcrypt.BCrypt;
import ru.brainmove.api.UserService;
import ru.brainmove.entity.User;

import java.io.IOException;
import java.util.UUID;

public class UserServiceBean implements UserService {

    public UserServiceBean() {
        init();
    }

    private void init() {

        Long usersCount = 0L;
        UserEntityService userEntityService = null;
        try {
            userEntityService = new UserEntityService();
            usersCount = userEntityService.countAll();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (userEntityService != null)
                userEntityService.sqlSession.close();
        }

        if (usersCount == 0) {
            registry("admin", "admin");
            registry("test", "test");
        }
    }

    @Override
    public User findByUser(String login) {
        if (login == null || login.isEmpty()) return null;
        UserEntityService userEntityService = null;
        User foundUser = null;
        try {
            userEntityService = new UserEntityService();
            foundUser = userEntityService.findByLogin(login);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (userEntityService != null)
                userEntityService.sqlSession.close();
        }
        return foundUser;
    }

    @Override
    public boolean check(String login, String password) {
        if (login == null || login.isEmpty()) return false;
        if (password == null || password.isEmpty()) return false;
        @Nullable final User user = findByUser(login);
        if (user == null || user.getPassword() == null) return false;
        return BCrypt.checkpw(password, user.getPassword());
    }

    @Override
    public boolean registry(@Nullable final String login, @Nullable final String password) {
        if (login == null || login.isEmpty()) return false;
        if (password == null || password.isEmpty()) return false;
        if (exists(login)) return false;
        @NotNull final User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setLogin(login);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        UserEntityService userEntityService = null;
        try {
            userEntityService = new UserEntityService();
            userEntityService.insert(user);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (userEntityService != null)
                userEntityService.sqlSession.close();
        }
        return true;
    }

    @Override
    public boolean exists(String login) {
        if (login == null || login.isEmpty()) return false;
        UserEntityService userEntityService = null;
        boolean isExist = false;
        try {
            userEntityService = new UserEntityService();
            isExist = (userEntityService.findByLogin(login) != null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (userEntityService != null)
                userEntityService.sqlSession.close();
        }
        return isExist;
    }

    @Override
    public boolean setLogin(String login, String newLogin) {
        if (login == null || login.isEmpty()) return false;
        @Nullable final User user = findByUser(login);
        if (user == null) return false;
        if (exists(newLogin)) return false;
        user.setLogin(newLogin);
        UserEntityService userEntityService = null;
        try {
            userEntityService = new UserEntityService();
            userEntityService.updateLogin(user);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (userEntityService != null)
                userEntityService.sqlSession.close();
        }
        return true;
    }

    @Override
    public boolean setPassword(String login, String passwordOld, String passwordNew) {
        if (!check(login, passwordOld)) return false;
        if (passwordNew == null || passwordNew.isEmpty()) return false;
        @Nullable final User user = findByUser(login);
        if (user == null) return false;
        user.setPassword(BCrypt.hashpw(passwordNew, BCrypt.gensalt()));
        UserEntityService userEntityService = null;
        try {
            userEntityService = new UserEntityService();
            userEntityService.updatePassword(user);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (userEntityService != null)
                userEntityService.sqlSession.close();
        }
        return true;
    }
}
