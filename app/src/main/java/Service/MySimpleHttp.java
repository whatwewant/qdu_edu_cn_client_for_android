package Service;

import android.annotation.TargetApi;
import android.os.Build;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by potter on 14-8-1.
 */

/**
 * @author potter
 * @method get
 *  @params url
 *
 * @method post
 *  #params url postParams
 *
 * @principle / theory
 *  原理:
 *      浏览器对服务器: 每次发生请求时,服务器会返回给浏览器一个Cookie，用以下方法获得,cookies是一个迭代对象，
 *                      cookies的没一项都是一个键值对
 *                  CookieStore cookieStore = httpClient.getCookieStore();
 *                   List<Cookie> cookies = cookieStore.getCookies();
 *
 *      保持登入(保持Session)的原理是: 下一次对服务器发生请求请，将之前访问获得的cookies设置在请求头中，
 *          发送请求后，服务器会先检查请求头，根据请求头判断是不是刚才请求过的用户
 *
 *      注意，这里的cookies值有时间限制，过期时间由服务器定
 * */


/**
 * 使用方法:
 *      // 1、先生成MyHttpClient对象
 *      MyHttpClient myHttpClient = new MyHttpClient();
 *      // 2、对要访问的网址先执行get,获取cookies值
 *      myHttpClient.get(url);
 *      // 3、对该该网站其他或当前进行post
 *      myHttpClient.post(newurl, params);
 *      // 注意这里的params是ArrayList<NameValuePair>
 *      //    产生方法:  ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 *      //              params.add(new BasicNameValuePair("username", "ssssssss"));
 *      //              params.add(new BasicNameValuePair("password", "s22222222"));
 * */
public class MySimpleHttp {
    private DefaultHttpClient httpClient;
    private HttpPost httpPost;
    private HttpGet httpGet;
    private HttpEntity httpEntity;
    private HttpResponse httpResponse;

    private String returnResult;

    //private String JSESSIONID = null;
    // private static ArrayList<NameValuePair> myCookies = null;
    private  ArrayList<NameValuePair> myCookies = null;

    private String httpCookies = "";

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public String get(String urlString) throws IOException {
        httpGet = new HttpGet(urlString);

        //if(null != JSESSIONID) {
        //    httpGet.setHeader("Cookie", "cVote=" + JSESSIONID);
        //}

        if(! httpCookies.isEmpty()) {
            httpGet.setHeader("Cookie", httpCookies);

        }
        httpGet.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        //httpGet.addHeader("Accept-Encoding", "gzip,deflate,sdch");
        //httpGet.addHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
        httpGet.addHeader("Connection", "keep-alive");
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
        httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);

        httpResponse = httpClient.execute(httpGet);

        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            returnResult = EntityUtils.toString(httpResponse.getEntity());
            CookieStore cookieStore = httpClient.getCookieStore();
            List<Cookie> cookies = cookieStore.getCookies();

            if(null == myCookies) {
                ArrayList<NameValuePair> tmp = new ArrayList<NameValuePair>();
                for(Cookie each : cookies) {
                    tmp.add(new BasicNameValuePair(each.getName(), each.getValue()));
                    httpCookies = "";
                    httpCookies += each.getName() + "=" + each.getValue() + "; ";
                }
                myCookies = tmp;
            }

            /*
            for(int i=0; i<cookies.size(); i++) {
                if( "cVote".equals(cookies.get(i).getName()) ) {
                    JSESSIONID = cookies.get(i).getValue();
                    break;
                }

            //    System.out.println(cookies.get(i).getName() +" = " + cookies.get(i).getValue());

            }*/
            // System.out.println("In MyHttpClient Get: Cookies: " + cookies.toString());
            return returnResult;
        }

        return "网络请求异常，请检查网络";
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public String post(String url, ArrayList<NameValuePair> postParams) throws IOException {
        httpPost = new HttpPost(url);


        if(! httpCookies.isEmpty()) {
            httpPost.setHeader("Cookie", httpCookies);
            //System.out.println("HttpCookies: " + httpCookies);
        }

        //if(null != JSESSIONID) {
        //    httpPost.setHeader("Cookie", "cVote=" + JSESSIONID);
        //}
        httpPost.addHeader("Host", "10.0.109.2");
        // httpPost.addHeader("Content-Length", "98");
        httpPost.addHeader("Origin","http://10.0.109.2");
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
        httpPost.addHeader("Referer", "http://10.0.109.2");

        httpPost.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));

        httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
        httpResponse = httpClient.execute(httpPost);

        if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            returnResult = EntityUtils.toString(httpResponse.getEntity());
            System.out.println("In Http Post: " + returnResult);
            return returnResult;
        }

        return "网络请求异常，请检查网络";
    }
}