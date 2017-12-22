package live.itrip.client.util;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpUtils {

    /*
    public static StringBuffer httpGet(String url) {
        StringBuffer buffer = new StringBuffer();
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response;
        try {
            response = client.execute(request);
            // Get the response
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = br.readLine()) != null) {
//                System.out.println(line);
                buffer.append(line);
            }
        } catch (IOException | UnsupportedOperationException e) {
            e.printStackTrace();
        }
        return buffer;
    }  */

    /**
     * 发送 get请求
     */
    public static String httpGet(String uri) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(uri);
            Logger.debug("executing request " + httpget.getURI());

            // 执行get请求.
            try (CloseableHttpResponse response = httpclient.execute(httpget)) {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                // 打印响应状态
                System.out.println(response.getStatusLine());
                if (entity != null) {
                    // 打印响应内容长度
                    Logger.debug("Response content length: " + entity.getContentLength());
                    // 打印响应内容
                    String content = EntityUtils.toString(entity);
                    Logger.debug("Response content: " + content);
                    return content;
                }
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
