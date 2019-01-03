package com.moon.util;

import com.vdurmont.emoji.EmojiParser;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Author : moon
 * Date  : 2018/12/31 23:05
 * Description : Class for 爬虫工具类
 */
public class CrawUtil {

    /**
     * GET获取网页信息
     *
     * @param url    网页
     * @param params 参数
     * @return
     * @throws Exception
     */
    public static String getResponse(String url, List<NameValuePair> params) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        String response = null;
        if (params != null && !params.isEmpty()) {
            uriBuilder.addParameters(params);
        }
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(uriBuilder.build());
        CloseableHttpResponse httpResponse = client.execute(get);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                response = EntityUtils.toString(entity);
            }
            EntityUtils.consume(entity);
            return response;
        } else {
            return null;
        }
    }

    /**
     * POST 获取网页信息
     *
     * @param url
     * @param params
     * @return
     * @throws Exception
     */
    public static String postResponse(String url, List<NameValuePair> params) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        String response = null;
        if (params != null && !params.isEmpty()) {
            uriBuilder.addParameters(params);
        }
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(uriBuilder.build());
        CloseableHttpResponse httpResponse = client.execute(post);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                response = EntityUtils.toString(entity);
            }
            EntityUtils.consume(entity);
            return response;
        } else {
            return null;
        }
    }


    /**
     * Emoji字符编码
     *
     * @param content
     * @return
     */
    public static String parseEmoji(String content) {
        return EmojiParser.parseToHtmlDecimal(content);
    }

    /**
     * 日期解析
     * @param date
     * @return
     * @throws Exception
     */
    public static Date parseDateTime(String date) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(date);
    }

    /**
     * 日期解析
     * @param date
     * @return
     * @throws Exception
     */
    public static Date parseDateTimeZn(String date) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        return sdf.parse(date);
    }

}
