package entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Author : moon
 * Date  : 2018/12/28 11:01
 * Description : Class for
 */
@Data
public class BasePojo extends PageHelpPojo implements Serializable {

    Integer id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC+8")
    Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC+8")
    Date updateTime;


}
