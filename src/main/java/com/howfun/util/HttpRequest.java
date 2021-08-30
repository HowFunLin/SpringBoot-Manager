package com.howfun.util;

import groovy.util.logging.Slf4j;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
public class HttpRequest {

    static HostnameVerifier hv = (urlHostName, session) -> true;

    public static String readData(String url, String type) {
        // 变量json，用于存放拼装好的json数据
        StringBuffer json = new StringBuffer();

        //url中不可以出现空格，空格全部用%20替换

        try {
            URL urls = new URL(url);

            //服务器不信任我们自己创建的证书，所以在代码中必须要忽略证书信任问题。只要在创建connection之前调用两个方法：
            trustAllHttpsCertificates();
            HttpsURLConnection.setDefaultHostnameVerifier(hv);

            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urls.openConnection();

            conn.setRequestMethod(type);
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            InputStream inputStream = conn.getInputStream();
            //从输入流中获取数据（一定要设置编码格式，不然在服务器端接收到的数据可能乱码）
            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;

            //一行一行的读
            while ((line = bf.readLine()) != null) {
                json = json.append(line);
            }

            if (inputStream != null)
                inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //得到输入流
        String[] strs = json.toString().split("\\\\");
        String str = "";
        StringBuffer jsons = new StringBuffer("");

        for (int i = 0; i < strs.length; i++) {
            str = strs[i];
            jsons = jsons.append(str);
        }

        return jsons.toString();
    }

    // 信任所有HTTP证书
    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;

        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    // 创建信任管理器
    static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            return;
        }

        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            return;
        }
    }
}