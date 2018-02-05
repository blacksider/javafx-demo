package demo.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

/**
 * Created by Snart Lu on 2018/2/5
 */
public class HttpsClient {
    private static Logger logger = LoggerFactory.getLogger(HttpsClient.class.getName());
    private static PoolingHttpClientConnectionManager connMgr;
    private static RequestConfig requestConfig;
    private static final int MAX_TIMEOUT = 15000;

    static {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
            .<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", createSSLConnSocketFactory())
            .build();

        connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connMgr.setMaxTotal(100);
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());

        RequestConfig.Builder configBuilder = RequestConfig.custom();
        configBuilder.setConnectTimeout(MAX_TIMEOUT);
        configBuilder.setSocketTimeout(MAX_TIMEOUT);
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
        requestConfig = configBuilder.build();
    }

    public static HttpResponse doGetSSL(String apiUrl) {
        HttpResponse httpResponse = new HttpResponse();
        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connMgr)
            .setDefaultRequestConfig(requestConfig)
            .build();
        HttpGet httpGet = new HttpGet(apiUrl);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            httpResponse.setStatus(statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return null;
                }
                String httpStr = EntityUtils.toString(entity, "utf-8");
                httpResponse.setData(httpStr);
            }
        } catch (Exception e) {
            logger.error("doGetSSL error", e);
            httpResponse.setException(e);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    logger.error("doGetSSL consume error" + e.getMessage());
                    httpResponse.setException(e);
                }
            }
        }
        return httpResponse;
    }

    public static HttpResponse doPostSSL(String apiUrl, String json) {
        HttpResponse httpResp = new HttpResponse();
        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connMgr)
            .setDefaultRequestConfig(requestConfig)
            .build();
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;
        String httpStr = null;

        try {
            StringEntity stringEntity = new StringEntity(json, "UTF-8");
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            httpResp.setStatus(statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return null;
                }
                httpStr = EntityUtils.toString(entity, "utf-8");
                httpResp.setData(httpStr);
            }
        } catch (Exception e) {
            logger.error("doPostSSL error", e);
            httpResp.setException(e);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    logger.error("doPostSSL consume error", e);
                    httpResp.setException(e);
                }
            }
        }
        return httpResp;
    }

    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory socketFactory = null;
        try {
            TrustStrategy trustAllStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, trustAllStrategy)
                .build();

            socketFactory = new SSLConnectionSocketFactory(sslContext);
        } catch (GeneralSecurityException e) {
            logger.error("createSSLConnSocketFactory error", e);
        }
        return socketFactory;
    }
}
