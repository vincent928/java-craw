package entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Author : moon
 * Date  : 2018/12/29 14:57
 * Description : Class for
 */
@Data
public class FilmReview implements Serializable {

    private Integer id;
    private String movieName;
    private String moviePic;
    private String username;
    private Integer star;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC+8")
    private Date time;
    private String text;

}
