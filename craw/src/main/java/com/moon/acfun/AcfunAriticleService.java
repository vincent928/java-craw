package com.moon.acfun;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.moon.constants.RedisContants;
import com.moon.util.CrawUtil;
import com.moon.util.RedisUtil;
import entity.AcFunArticleEntity;
import enums.ResultEnum;
import org.apache.http.NameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import util.CodeUtil;
import vo.ResultData;

import javax.annotation.Resource;
import java.util.*;

/**
 * Author : moon
 * Date  : 2018/12/31 23:22
 * Description : Class for Acfun文章爬取
 */
@Service
public class AcfunAriticleService {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private AcfunMapper acfunMapper;
    private static final Logger LOG = LoggerFactory.getLogger(AcfunAriticleService.class);

    /**
     * 爬取acfun文章列表
     * 杂谈
     *
     * @return
     */
    public ResultData getArticle(String url, List<NameValuePair> params) {
        try {
            //1.获取文章列表
            Set<Integer> idList = new HashSet<>();
            String response = CrawUtil.getResponse(url, params);
            JSONObject jsonObject = JSONObject.parseObject(response);
            JSONObject data = (JSONObject) jsonObject.get("data");
            JSONArray articleList = data.getJSONArray("articleList");
            for (Object o : articleList) {
                JSONObject object = JSONObject.parseObject(o.toString());
                Integer id = object.getInteger("id");
                idList.add(id);
            }
            //2.筛选已经爬取过的页面
            String uuid = CodeUtil.getUUID();
            redisUtil.Sset(RedisContants.REDIS_ACFUN_SET_SDIFF + ":" + uuid, idList, 75 << 2);
            Set<Object> sdiff = redisUtil.Sdiff(RedisContants.REDIS_ACFUN_SET_SDIFF + ":" + uuid, RedisContants.REDIS_ACFUN_SET);
            LOG.info("待爬取文章：" + sdiff);
            if (sdiff == null || sdiff.isEmpty()) {
                return ResultData.result(ResultEnum.FAIL.getCode(), "以上文章已被爬取过", sdiff);
            }
            //2.爬取文章页面
            List<AcFunArticleEntity> articleEntities = parseArticle(sdiff);
            LOG.info("爬取文章数:" + articleEntities.size());
            //3.存取数据库
            acfunMapper.addArticles(articleEntities);
            //4.将存取过的页面存入redis
            redisUtil.Sset(RedisContants.REDIS_ACFUN_SET, sdiff);
            return ResultData.result(ResultEnum.SUCCESS, sdiff.size());
        } catch (Exception e) {
            return ResultData.result(ResultEnum.FAIL);
        }
    }

    /**
     * 文章爬取
     *
     * @param set
     * @return
     */
    private List<AcFunArticleEntity> parseArticle(Set set) throws Exception {
        List<AcFunArticleEntity> list = new ArrayList<>();
        for (Object o : set) {
            AcFunArticleEntity entity = new AcFunArticleEntity();
            int id = Integer.parseInt(o.toString());
            String url = RedisContants.ACFUN_ARTICLE_URL + id;
            String response = CrawUtil.getResponse(url, null);
            Document document = Jsoup.parse(response);
            Elements tags = document.getElementsByClass("article-parent");
            StringBuffer sb = new StringBuffer();
            for (Element tag : tags) {
                String ss = tag.text();
                sb.append(ss).append(">");
            }
            String tag = sb.toString().substring(0, sb.toString().length() - 1);
            entity.setType(tag);
            String title = document.getElementsByClass("caption").first().text();
            entity.setTitle(title);
            String upname = document.getElementsByClass("upname").first().text();
            entity.setUpName(upname);
            String time = document.getElementsByClass("up-time").first().text();
            Date date = CrawUtil.parseDateTimeZn(time);
            entity.setCreateTime(date);
            String content = document.getElementsByClass("article-content").first().text();
            entity.setContent(content);
            entity.setLink(url);
            list.add(entity);
        }
        return list;
    }


}
