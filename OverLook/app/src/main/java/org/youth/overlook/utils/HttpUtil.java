package org.youth.overlook.utils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/4/28 0028.
 */
public class HttpUtil {

    public HttpUtil() {
    }

    /**
     * 将输入流转换为指定编码的字符串
     *
     * @param inputStream
     * @param encode
     * @return
     */
    public static String changeInputStream(InputStream inputStream,
                                           String encode) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;

        String result = "";

        if (inputStream != null) {
            try {
                while ((len = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, len);
                }
                result = new String(outputStream.toByteArray(), encode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 使用apache的httpPost工具
     *
     * @param path
     * @param params
     * @param encode
     * @return
     */
    public static String sendHttpClientPost(String path,
                                            Map<String, String> params, String encode) {
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                list.add(new BasicNameValuePair(entry.getKey(), entry
                        .getValue()));
            }
        }
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, encode);// 将请求的参数封装到请求体中

            // 定义post请求对象
            HttpPost httpPost = new HttpPost(path);
            httpPost.setEntity(entity);
            // 执行post请求
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                // 将输入流转换成字符串
                return changeInputStream(httpResponse.getEntity().getContent(),
                        encode);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
