package com.soecode.lyf.dao;

import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.soecode.lyf.entity.Persion;

public interface PersionDao {

    int insert(@Param("pojo") Persion pojo);

    int insertList(@Param("pojos") List< Persion> pojo);

    List<Persion> select(@Param("pojo") Persion pojo);

    int update(@Param("pojo") Persion pojo);

}
