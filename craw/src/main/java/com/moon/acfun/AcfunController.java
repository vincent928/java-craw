package com.moon.acfun;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import vo.ResultData;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Author : moon
 * Date  : 2018/12/31 23:34
 * Description : Class for
 */
@RestController
@RequestMapping(value = "acfun")
public class AcfunController {

    @Resource
    private AcfunAriticleService acfunAriticleService;

    /**
     * 获取Acfun文章
     *
     * @return
     */
    @RequestMapping(value = "article", method = RequestMethod.POST)
    public ResultData getArticle(@RequestBody AcfunModel model) {
        List<NameValuePair> params = new ArrayList<>();
        BasicNameValuePair param = new BasicNameValuePair("pageNo", model.getPageNo());
        BasicNameValuePair param2 = new BasicNameValuePair("size", model.getSize());
        BasicNameValuePair param3 = new BasicNameValuePair("realmIds", model.getRealmIds());
        BasicNameValuePair param4 = new BasicNameValuePair("originalOnly", "false");
        BasicNameValuePair param5 = new BasicNameValuePair("orderType", "1");
        BasicNameValuePair param6 = new BasicNameValuePair("periodType", "-1");
        BasicNameValuePair param7 = new BasicNameValuePair("filterTitleImage", "true");
        params.add(param);
        params.add(param2);
        params.add(param3);
        params.add(param4);
        params.add(param5);
        params.add(param6);
        params.add(param7);
        return acfunAriticleService.getArticle(model.getUrl(), params);
    }

}
