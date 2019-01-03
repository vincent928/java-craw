package com.moon.douban;

import entity.FilmReview;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

/**
 * Author : moon
 * Date  : 2018/12/29 14:48
 * Description : Interface for
 */
@Repository
public interface FilmReviewMapper {


    @Insert("insert into douban_film_review(movie_name,movie_pic,username,star,time,content)" +
            "values(#{movieName,jdbcType=VARCHAR},#{moviePic,jdbcType=VARCHAR},#{username,jdbcType=VARCHAR}," +
            "#{star,jdbcType=INTEGER},#{time,jdbcType=TIMESTAMP},#{text,jdbcType=VARCHAR})")
    int insertFilmReview(FilmReview filmReview);


}
