package com.moon.douban;

import entity.FilmReview;
import enums.ResultEnum;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import vo.ResultData;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author : moon
 * Date  : 2018/12/29 20:20
 * Description : Class for
 */
@RestController
@RequestMapping(value = "/douban")
public class FileReviewController {

    @Resource
    private FilmCraw craw;
    @Resource
    private FilmDao dao;

    /**
     * 爬取豆瓣影评信息
     *
     * @return
     */
    @RequestMapping(value = "review", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultData crawDouBanReview() {
        List<FilmReview> reviewList = craw.crawFilmReview();
        if (reviewList != null && !reviewList.isEmpty()) {
            return dao.insertFilm(reviewList);
        }
        return ResultData.result(ResultEnum.FAIL);
    }


}
