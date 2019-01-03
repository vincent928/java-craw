package entity;

import lombok.Data;

import java.io.Serializable;

/**
 * Author : moon
 * Date  : 2018/12/29 11:22
 * Description : Class for
 */
@Data
public class PageHelpPojo implements Serializable {

    private Integer pageNum;
    private Integer pageSize;



}
