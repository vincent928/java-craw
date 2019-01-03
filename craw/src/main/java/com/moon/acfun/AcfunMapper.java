package com.moon.acfun;

import entity.AcFunArticleEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author : moon
 * Date  : 2019/1/2 12:02
 * Description : Class for
 */
@Repository
public interface AcfunMapper {


    int addArticles(@Param("entities") List<AcFunArticleEntity> entities);


}
