package com.moon.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Author : moon
 * Date  : 2019/1/2 13:48
 * Description : Class for httpclient4.5.x
 */
public class HttpUtil {


    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static final String DEFAULT_CHARSET = "UTF-8"; //默认请求编码
    private static final int DEFAULT_SOCKET_TIMEOUT = 5000; //默认等待响应时间
    private static final int DEFAULT_RETRY_TIMES = 0; //默认执行重试次数
    private static final int DEFAULT_REQUEST_TIMEOUT = 1000; //从connect manager获取connection超时时间

    /************************************************************************************************************
     *                                   httpclient                                                            *
     **********************************************************************************************************/

    /**
     * 创建一个可关闭的httpclient
     *
     * @return
     */
    public static CloseableHttpClient createHttpClient() {
        return createHttpClient(DEFAULT_RETRY_TIMES, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * 创建一个可关闭的httpclient
     *
     * @param socketTimeout 等待响应时间
     * @return
     */
    public static CloseableHttpClient createHttpClient(int socketTimeout) {
        return createHttpClient(DEFAULT_RETRY_TIMES, socketTimeout);
    }

    /**
     * 创建一个可关闭的httpclient
     *
     * @param retryTimes    请求重试次数,<= 0 表示不重试
     * @param socketTimeout 请求数据超时时间
     * @return
     */
    public static CloseableHttpClient createHttpClient(int retryTimes, int socketTimeout) {
        Builder builder = RequestConfig.custom();
        builder.setConnectTimeout(DEFAULT_SOCKET_TIMEOUT); //设置超时时间,单位毫秒
        builder.setConnectionRequestTimeout(DEFAULT_REQUEST_TIMEOUT); //设置从connect manager获取connection超时时间。单位毫秒
        if (socketTimeout >= 0)
            builder.setSocketTimeout(socketTimeout);
        RequestConfig requestConfig = builder.setCookieSpec(CookieSpecs.STANDARD_STRICT)
                .setExpectContinueEnabled(true)
                .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
                .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
                .build();
        //开启https支持
        enableSSL();
        //创建可用Scheme
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", socketFactory).build();
        //创建connectionManager，添加connection配置信息
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        HttpClientBuilder clientBuilder = HttpClients.custom();
        if (retryTimes > 0)
            setRetyHandler(clientBuilder, retryTimes);
        CloseableHttpClient httpClient = clientBuilder.setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
        return httpClient;
    }


    /***********************************************************************************************************************
     *                                            Get / Post 请求                                                         *
     *********************************************************************************************************************/

    /**
     * 执行简单Get请求
     *
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public static HttpResult get(String url) throws Exception {
        return get(url, null);
    }

    /**
     * 执行简单Get请求
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return
     * @throws Exception
     */
    public static HttpResult get(String url, Map<String, String> params) throws Exception {
        CloseableHttpClient httpClient = createHttpClient();
        return executeGet(httpClient, url, params, null, null, DEFAULT_CHARSET, true);
    }

    /**
     * 执行Get请求
     *
     * @param httpClient      httpClient客户端实例,传入null则自动创建一个实例
     * @param url             请求的地址
     * @param referer         referer信息,可null
     * @param cookie          cookie信息,可null
     * @param charSet         请求编码,默认为utf8
     * @param closeHttpClient 请求结束后,是否关闭实例
     * @return
     * @throws Exception
     */
    public static HttpResult executeGet(CloseableHttpClient httpClient, String url, Map<String, String> params,
                                        String referer, String cookie, String charSet, boolean closeHttpClient) throws Exception {
        CloseableHttpResponse response = null;
        try {
            charSet = getCharset(charSet);
            response = executeGetResponse(httpClient, url, referer, cookie, params);
            //请求状态码
            int statusCode = response.getStatusLine().getStatusCode();
            String result = getResult(response, charSet);
            return new HttpResult(statusCode, result);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (closeHttpClient && httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }


    /**
     * 执行Get请求
     *
     * @param httpClient      httpclient实例
     * @param url             请求地址
     * @param referer         referer信息
     * @param cookie          cookie信息
     * @param charSet         编码格式
     * @param closeHttpClient 是否请求完成后关闭实例
     * @return
     * @throws Exception
     */
    public static String executeGetString(CloseableHttpClient httpClient, String url, Map<String, String> params, String referer,
                                          String cookie, String charSet, boolean closeHttpClient) throws Exception {
        CloseableHttpResponse response = null;
        try {
            charSet = getCharset(charSet);
            response = executeGetResponse(httpClient, url, referer, cookie, params);
            return getResult(response, charSet);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (closeHttpClient && httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 获取GET响应
     *
     * @param httpClient httpclient实例
     * @param url        请求地址
     * @param referer    referer信息,可null
     * @param cookie     cookie信息,可null
     * @return
     * @throws Exception
     */
    public static CloseableHttpResponse executeGetResponse(CloseableHttpClient httpClient, String url, String
            referer, String cookie, Map<String, String> params) throws Exception {
        if (httpClient == null)
            httpClient = createHttpClient();
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> list = mapToParams(params);
            uriBuilder.addParameters(list);
        }
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        if (cookie != null && !"".equals(cookie))
            httpGet.setHeader("Cookie", cookie);
        if (referer != null && !"".equals(referer))
            httpGet.setHeader("referer", referer);
        return httpClient.execute(httpGet);
    }


    /**
     * 简单Post请求
     *
     * @param url
     * @param params
     * @return
     */
    public static HttpResult post(String url, Object params) throws Exception {
        return executePost(url, params, DEFAULT_CHARSET, DEFAULT_SOCKET_TIMEOUT);
    }


    /**
     * 简单Post请求
     *
     * @param url           请求地址
     * @param params        post请求参数,支持map<String,String>, JSON,XML格式
     * @param charSet       请求编码格式,默认utf-8
     * @param socketTimeout 响应超时时间,默认5s
     * @return
     * @throws Exception
     */
    public static HttpResult executePost(String url, Object params, String charSet, int socketTimeout) throws Exception {
        return executePost(createHttpClient(socketTimeout), url, params, null, null, charSet, true);
    }


    /**
     * 执行Post请求
     *
     * @param httpClient      httpclient实例
     * @param url             请求地址
     * @param params          post请求参数,支持map<String,String>, JSON,XML格式
     * @param referer         referer信息
     * @param cookie          cookie信息
     * @param charSet         编码
     * @param closeHttpClient 请求完成之后,是否关闭httpclient实例
     * @return
     * @throws Exception
     */
    public static HttpResult executePost(CloseableHttpClient httpClient, String url, Object params, String referer,
                                         String cookie, String charSet, boolean closeHttpClient) throws Exception {
        CloseableHttpResponse response;
        try {
            charSet = getCharset(charSet);
            response = executePostResponse(httpClient, url, params, referer, cookie, charSet);
            int statusCode = response.getStatusLine().getStatusCode();
            String result = getResult(response, charSet);
            return new HttpResult(statusCode, result);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (closeHttpClient && httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 执行Post请求
     *
     * @param httpClient      httpclient实例
     * @param url             请求地址
     * @param params          post请求参数,支持map<String,String>, JSON,XML格式
     * @param referer         referer信息
     * @param cookie          cookie信息
     * @param charSet         编码
     * @param closeHttpClient 请求完成之后,是否关闭httpclient实例
     * @return
     * @throws Exception
     */
    public static String executePostString(CloseableHttpClient httpClient, String url, Object params, String referer,
                                           String cookie, String charSet, boolean closeHttpClient) throws Exception {
        CloseableHttpResponse response;
        try {
            charSet = getCharset(charSet);
            response = executePostResponse(httpClient, url, params, referer, cookie, charSet);
            return getResult(response, charSet);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (closeHttpClient && httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 获取Post响应
     *
     * @param httpClient httpclient实例
     * @param url        请求地址
     * @param params     post请求参数,支持map<String,String>, JSON,XML格式
     * @param referer    referer信息
     * @param cookie     cookie信息
     * @param charSet    编码
     * @return
     * @throws Exception
     */
    public static CloseableHttpResponse executePostResponse(CloseableHttpClient httpClient, String url, Object params,
                                                            String referer, String cookie, String charSet) throws Exception {
        if (httpClient == null)
            httpClient = createHttpClient();
        HttpPost httpPost = new HttpPost(url);
        if (cookie != null && !"".equals(cookie))
            httpPost.setHeader("Cookie", cookie);
        if (referer != null && !"".equals(referer))
            httpPost.setHeader("referer", referer);
        HttpEntity entity = getEntity(params, charSet);
        if (entity != null)
            httpPost.setEntity(entity);
        return httpClient.execute(httpPost);
    }


    /***********************************************************************************************************************
     *                                              文件上传/下载                                                         *
     *********************************************************************************************************************/

    /**
     * 文件上传
     *
     * @param remoteFileUrl
     * @param localFilePath
     * @return
     * @throws Exception
     */
    public static HttpResult uploadFile(String remoteFileUrl, String localFilePath) throws Exception {
        return uploadFile(createHttpClient(), remoteFileUrl, localFilePath, DEFAULT_CHARSET, true);
    }

    /**
     * 文件上传
     *
     * @param httpClient      httpclient实例
     * @param remoteFileUrl   远程接收文件地址
     * @param localFilePath   本地文件地址
     * @param charSet         编码
     * @param closeHttpClient 请求完成是否关闭实例
     * @return
     * @throws Exception
     */
    public static HttpResult uploadFile(CloseableHttpClient httpClient, String remoteFileUrl, String localFilePath,
                                        String charSet, boolean closeHttpClient) throws Exception {
        CloseableHttpResponse response;
        try {
            if (httpClient == null)
                createHttpClient();
            charSet = getCharset(charSet);
            File localFile = new File(localFilePath);
            FileBody fileBody = new FileBody(localFile);
            HttpEntity httpEntity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)  //以浏览器兼容模式运行,防止文件名乱码
                    .addPart("uploadFile", fileBody) //"uploadFile"对应服务端类的同名属性<File类型> .addPart("uploadFileName",fileName) "uploadFileName"对应服务端类的同名属性<String类型>
                    .setCharset(StandardCharsets.UTF_8)
                    .build();
            HttpPost httpPost = new HttpPost(remoteFileUrl);
            httpPost.setEntity(httpEntity);
            response = httpClient.execute(httpPost);
            return new HttpResult(response.getStatusLine().getStatusCode(), getResult(response, charSet));
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (closeHttpClient && httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 文件上传(二进制流形式)
     *
     * @param httpClient      httpclient实例
     * @param remoteFileUrl   上传地址
     * @param localFilePath   本地文件地址
     * @param charSet         编码
     * @param closeHttpClient 请求完成是否关闭实例
     * @return
     * @throws Exception
     */
//    public static HttpResult uploadFileByStream(CloseableHttpClient httpClient, String remoteFileUrl, InputStream stream, String localFilePath,
//                                                String charSet, boolean closeHttpClient) throws Exception {
//        CloseableHttpResponse response;
//        try{
//            if (httpClient ==null)
//                httpClient = createHttpClient();
//
//        }finally {
//
//        }
//    }

    /**
     * 下载文件
     *
     * @param url       文件地址
     * @param localPath 本地保存路径
     * @param fileName  文件名(不带后缀)
     * @return
     */
    public static boolean downloadFile(String url, String localPath, String fileName) throws Exception {
        CloseableHttpClient httpClient = createHttpClient();
        String suffix = getSuffix(url);
        String filePath;
        if (localPath.endsWith("/")) {
            filePath = localPath + fileName + suffix;
        } else {
            filePath = localPath + "/" + fileName + suffix;
        }

        return downloadFile(httpClient, url, filePath, true);
    }


    /**
     * 获取文件后缀
     *
     * @param remoteFileUrl https://avatar.csdn.net/1/E/C/3_fhaohaizi.jpg
     * @return .jpg .tar
     */
    private static String getSuffix(String remoteFileUrl) {
        String s = remoteFileUrl.substring(remoteFileUrl.lastIndexOf("/")); //  /3_fhaohaizi.jpg
        return s.substring(s.indexOf("."));
    }

    private static void mkdirs(File file) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
    }

    /**
     * 下载文件
     *
     * @param httpClient      httpclient实例
     * @param remoteFileUrl   服务端资源文件地址
     * @param localFilePath   本地存储文件地址
     * @param closeHttpClient 请求结束是否关闭实例
     * @return
     */
    public static boolean downloadFile(CloseableHttpClient httpClient, String remoteFileUrl, String localFilePath,
                                       boolean closeHttpClient) throws Exception {
        CloseableHttpResponse response = null;
        InputStream in;
        FileOutputStream fos = null;
        try {
            if (httpClient == null)
                httpClient = createHttpClient();
            HttpGet httpGet = new HttpGet(remoteFileUrl);
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity == null)
                return false;
            in = entity.getContent();
            File file = new File(localFilePath);
            //创建目录
            mkdirs(file);
            fos = new FileOutputStream(file);
            int i;
            byte[] tmp = new byte[1024];
            while ((i = in.read(tmp)) != -1) {
                fos.write(tmp, 0, i);   //此处使用OutPutStream.write(byte)的话,图片会失真
            }
            fos.flush();
            EntityUtils.consume(entity);
            return true;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (closeHttpClient && httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /***********************************************************************************************************************
     *                                                开启HTTPS验证                                                       *
     *********************************************************************************************************************/

    private static SSLConnectionSocketFactory socketFactory;
    /**
     * HTTPS网站一般情况下使用了安全系数较低的SHA-1签名，因此首先我们在调用SSL之前需要重写验证方法。取消检测SSL
     */
    private static TrustManager manager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };

    /**
     * 开启SSL支持
     */
    private static void enableSSL() {
        try {
            SSLContext tls = SSLContext.getInstance("TLS");
            tls.init(null, new TrustManager[]{manager}, null);
            socketFactory = new SSLConnectionSocketFactory(tls, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*****************************************************************************************************************
     *                                          设置httpclient                                                      *
     ***************************************************************************************************************/
    /**
     * 为httpClients设置重试信息
     *
     * @param httpClientBuilder
     * @param retryTimes
     */
    private static void setRetyHandler(HttpClientBuilder httpClientBuilder, final int retryTimes) {
        HttpRequestRetryHandler myHandler = (exception, executionCount, context) -> {
            if (executionCount >= retryTimes)
                return false;// Do not retry if over max retry count
            if (exception instanceof InterruptedIOException)
                return false; //TimeOut
            if (exception instanceof UnknownHostException)
                return false; //Unknown host
            if (exception instanceof ConnectTimeoutException)
                return false; //Connection refused
            if (exception instanceof SSLException)
                return false; //SSL handshake exception
            HttpClientContext httpClientContext = HttpClientContext.adapt(context);
            HttpRequest request = httpClientContext.getRequest();
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            if (idempotent)
                return true;//如果请求是认为是幂等的,那么就重试 Retry if the request is considered idempotent
            return false;
        };
        httpClientBuilder.setRetryHandler(myHandler);
    }


    /*******************************************************************************************************************
     *                                             工具方法                                                           *
     *****************************************************************************************************************/

    /**
     * 根据参数获取请求的Entity
     *
     * @param param
     * @param charSet
     * @return
     * @throws Exception
     */
    private static HttpEntity getEntity(Object param, String charSet) throws Exception {
        if (param == null) {
            logger.info("当前未传入参数信息,无法生成org.apache.http.HttpEntity");
            return null;
        }
        if (Map.class.isInstance(param)) { //当前是map数据
            @SuppressWarnings("unchecked")
            Map<String, String> paramsMap = (Map<String, String>) param;
            List<NameValuePair> pairList = mapToParams(paramsMap);
            UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(pairList, charSet);
            httpEntity.setContentType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
            return httpEntity;
        } else if (String.class.isInstance(param)) {
            String str = (String) param;
            StringEntity httpEntity = new StringEntity(str, charSet);
            if (str.startsWith("{"))
                httpEntity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            else if (str.startsWith("<"))
                httpEntity.setContentType(ContentType.APPLICATION_XML.getMimeType());
            else
                httpEntity.setContentType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
            return httpEntity;
        } else
            logger.info("当前传入参数属于不能识别信息,无法生成org.apache.http.HttpEntity");
        return null;
    }

    /**
     * 请求编码
     *
     * @param charSet
     * @return
     */
    private static String getCharset(String charSet) {
        return charSet == null ? DEFAULT_CHARSET : charSet;
    }

    /**
     * Map --> List<NameValuePair>
     *
     * @param paramsMap
     * @return
     */
    private static List<NameValuePair> mapToParams(Map<String, String> paramsMap) {
        List<NameValuePair> list = new ArrayList<>();
        if (paramsMap == null || paramsMap.isEmpty())
            return list;
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    /**
     * 从结果中获取出String数据
     *
     * @param response http结果对象
     * @param charSet  编码
     * @return
     * @throws Exception
     */
    private static String getResult(CloseableHttpResponse response, String charSet) throws Exception {
        String result;
        if (response == null)
            return null;
        HttpEntity entity = response.getEntity();
        if (entity == null)
            return null;
        result = EntityUtils.toString(entity, charSet);
        EntityUtils.consume(entity);    //关闭资源
        return result;
    }

    /****************************************************************************************************************
     *                                           HttpResult                                                       *
     **************************************************************************************************************/
    static class HttpResult implements Serializable {
        private Integer code;
        private String data;

        public HttpResult(Integer code, String data) {
            this.code = code;
            this.data = data;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

}
