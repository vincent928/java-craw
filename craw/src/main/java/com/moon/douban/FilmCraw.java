package com.moon.douban;

import com.vdurmont.emoji.EmojiParser;
import entity.FilmReview;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author : moon
 * Date  : 2018/12/29 14:49
 * Description : Class for 爬取豆瓣网影评
 */
@Service
public class FilmCraw {


    /**
     * 爬取影评
     *
     * @return
     */
    public List<FilmReview> crawFilmReview() {
        try {
            List<FilmReview> reviewList = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                List<NameValuePair> list = new ArrayList<>();
                BasicNameValuePair param = new BasicNameValuePair("start", String.valueOf(i * 20));
                list.add(param);
                CloseableHttpResponse httpGet = getHttpGet("https://movie.douban.com/review/best", list);
                if (200 == getCode(httpGet)) {
                    String responseStr = getResponseStr(httpGet);
                    if (responseStr != null) {
                        Document document = Jsoup.parse(responseStr);
                        Elements elements = document.getElementsByClass("review-item");
                        for (Element element : elements) {
                            FilmReview filmReview = new FilmReview();
                            String href = element.getElementsByTag("img").first().attr("src");
                            String time = element.getElementsByClass("main-meta").text();
                            String name = element.getElementsByClass("name").text();
                            Element h2 = element.getElementsByTag("h2").first();
                            String ariticleHref = h2.getElementsByTag("a").attr("href");
                            String full = getFull(ariticleHref);
                            String emojiFull = parseEmoji(full);
                            String title = element.getElementsByTag("img").first().attr("title");
                            String rating = element.getElementsByClass("main-title-rating").attr("title");
                            filmReview.setMovieName(title);
                            filmReview.setUsername(name);
                            filmReview.setTime(parseDateTime(time));
                            filmReview.setMoviePic(href);
                            filmReview.setText(emojiFull);
                            filmReview.setStar(parseRating(rating));
                            reviewList.add(filmReview);
                        }
                    }
                }
            }
            return reviewList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Date parseDateTime(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(date);
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

    private static Integer parseRating(String rating) {
        switch (rating) {
            case "很差":
                return 1;
            case "较差":
                return 2;
            case "还行":
                return 3;
            case "推荐":
                return 4;
            case "力荐":
                return 5;
            default:
                return 0;
        }
    }

    /**
     * 创建Get请求
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    private static CloseableHttpResponse getHttpGet(String url, List<NameValuePair> params) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        uriBuilder.addParameters(params);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(uriBuilder.build());
        return client.execute(get);
    }

    /**
     * 创建Get请求
     *
     * @param url
     * @return
     * @throws IOException
     */
    private static CloseableHttpResponse getHttpGet(String url) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        return client.execute(get);
    }

    /**
     * 创建Post请求
     *
     * @param url
     * @param entity
     * @return
     * @throws IOException
     */
    private static CloseableHttpResponse getHttpPost(String url, HttpEntity entity) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setEntity(entity);
        return client.execute(post);
    }

    /**
     * 获取状态码
     *
     * @param response
     * @return
     */
    private static int getCode(CloseableHttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    /**
     * 获取返回值
     *
     * @param response
     * @return
     * @throws IOException
     */
    private static String getResponseStr(CloseableHttpResponse response) throws IOException {
        String respon = null;
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            respon = EntityUtils.toString(entity);
        }
        EntityUtils.consume(entity);
        return respon;
    }

    /**
     * 获取全部评论
     *
     * @return
     */
    private static String getFull(String url) throws Exception {
        CloseableHttpResponse httpGet = getHttpGet(url);
        String str = null;
        if (200 == getCode(httpGet)) {
            str = getResponseStr(httpGet);
        }
        Document parse = Jsoup.parse(str);
        Element content = parse.getElementsByClass("review-content").first();
        return content.text();
    }


}
