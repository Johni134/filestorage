package ru.brainmove.service;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

class AbstractService {

    private static final String RESOURCE = "mybatis-config.xml";
    final SqlSession sqlSession;
    private final SqlSessionFactory sqlSessionFactory;

    AbstractService() throws IOException {
        final InputStream inputStream = Resources.getResourceAsStream(RESOURCE);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        sqlSession = sqlSessionFactory.openSession(true);
    }
}
