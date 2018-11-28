package ru.brainmove.service;

import ru.brainmove.entity.Token;
import ru.brainmove.entity.User;
import ru.brainmove.repository.UserRepository;

import java.io.IOException;
import java.util.List;

class UserEntityService extends AbstractService {

    private final UserRepository userRepository;

    UserEntityService() throws IOException {
        userRepository = sqlSession.getMapper(UserRepository.class);
    }

    List<User> findAll() {
        return userRepository.findAll();
    }

    User findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    void updateLogin(User user) {
        userRepository.updateLogin(user);
    }

    void updatePassword(User user) {
        userRepository.updatePassword(user);
    }

    void insert(User user) {
        userRepository.insert(user);
    }

    Long countAll() {
        return userRepository.countAll();
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    Token getTokenByIdAndAccess(String id, String access) {
        return userRepository.getTokenByIdAndAccess(new Token(id, access));
    }

    void insertToken(Token token) {
        userRepository.insertToken(token);
    }

    void clearOldTokens() {
        userRepository.clearOldTokens();
    }

}
