package ru.brainmove.repository;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import ru.brainmove.entity.User;

import java.util.List;

public interface UserRepository {

    @Select("SELECT * FROM users")
    List<User> findAll();

    @Select("SELECT COUNT(*) AS userCount FROM users")
    Long countAll();

    @Select("SELECT * FROM users WHERE login = #{login}")
    User findByLogin(String login);

    @Update("UPDATE users SET login = #{login} WHERE id = #{id}")
    void updateLogin(User user);

    @Update("UPDATE users SET password = #{password} WHERE id = #{id}")
    void updatePassword(User user);

    @Insert("INSERT INTO users (id, login, password) VALUES (#{id}, #{login}, #{password})")
    void insert(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void delete(User user);
}
