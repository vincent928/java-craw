package entity;

import lombok.Data;

import java.io.Serializable;

/**
 * Author : moon
 * Date  : 2018/12/28 11:02
 * Description : Class for 文章类
 */
@Data
public class Ariticle extends BasePojo implements Serializable {


    private String title;
    private String desc;
    private String content;
    private Integer status;


}
