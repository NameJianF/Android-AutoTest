package live.itrip.client.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HttpUtils {

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
    }
}
