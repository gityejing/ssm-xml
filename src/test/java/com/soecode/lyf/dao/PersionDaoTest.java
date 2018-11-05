package com.soecode.lyf.dao;

import com.soecode.lyf.BaseTest;
import com.soecode.lyf.entity.Persion;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class PersionDaoTest extends BaseTest {

    @Autowired
    PersionDao persionDao;

    @Test
    public void test1(){
        Persion persion = new Persion();
        persion.setName("叶静");
        persion.setAge(30);
        persion.setBirthday(new Date());
        persionDao.insert(persion);
    }

    @Test
    public void testQuery(){
        Persion params = new Persion();
        List<Persion> persionList = persionDao.select(params);
        System.out.println(persionList);
    }
}
