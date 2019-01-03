package com.moon.acfun;

import lombok.Data;

import java.io.Serializable;

/**
 * Author : moon
 * Date  : 2019/1/2 13:17
 * Description : Class for
 */
@Data
public class AcfunModel implements Serializable {

    private String url;
    private String size;
    private String pageNo;
    private String realmIds;

}
