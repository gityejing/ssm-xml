package com.soecode.lyf.service.impl;

import com.soecode.lyf.entity.Persion;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

import com.soecode.lyf.dao.PersionDao;

@Service
public class PersionService {

    @Resource
    private PersionDao persionDao;

    public int insert(Persion pojo){
        return persionDao.insert(pojo);
    }

    public int insertList(List< Persion> pojos){
        return persionDao.insertList(pojos);
    }

    public List<Persion> select(Persion pojo){
        return persionDao.select(pojo);
    }

    public int update(Persion pojo){
        return persionDao.update(pojo);
    }

}
