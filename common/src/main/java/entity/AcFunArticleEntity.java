package entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Author : moon
 * Date  : 2018/12/31 23:29
 * Description : Class for
 */
@Data
public class AcFunArticleEntity implements Serializable {

    private Integer id;
    private String title;
    private String upName;
    private Date createTime;
    private String content;
    private String link;
    private String type;

}
