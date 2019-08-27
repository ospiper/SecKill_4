package org.bytecamp19.seckill4.service;

import org.bytecamp19.seckill4.entity.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SessionTest {
    @Autowired
    private SessionService sessionService;

    private final String sessionid = "ab5c53d8e094263412ffdfe524ddba0d";
    private final String sessionid_1 ="abcdefghigklmnopqrstuvwxyz";
    @Test
    public void getSessionTest(){
        // 验证正确sessionid
        Session session = sessionService.getSession(sessionid);
        assertNotNull(session);
        System.out.println(session);
        // 验证错误sessionid_1
        Session session1 = sessionService.getSession(sessionid_1);
        assertNull(session1);
        System.out.println(session1);
    }

}
