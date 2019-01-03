package com.moon.douban;

import entity.FilmReview;
import enums.ResultEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vo.ResultData;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author : moon
 * Date  : 2018/12/29 20:21
 * Description : Class for
 */
@Service
public class FilmDao {

    @Resource
    private FilmReviewMapper mapper;

    private static final Logger LOG = LoggerFactory.getLogger(FilmDao.class);

    /**
     * 爬取数据写入数据库
     *
     * @param list
     * @return
     */
    public ResultData insertFilm(List<FilmReview> list) {
        try {
            for (FilmReview filmReview : list) {
                LOG.info(filmReview.toString());
                mapper.insertFilmReview(filmReview);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResultData.result(ResultEnum.SUCCESS, list.size());
    }


}
