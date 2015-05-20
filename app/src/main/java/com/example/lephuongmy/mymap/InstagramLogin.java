package com.example.lephuongmy.mymap;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by LePhuongMy on 19/05/2015.
 */

public class InstagramLogin {
    private static final String AUTHURL = "https://api.instagram.com/oauth/authorize/";
    private static final String TOKENURL = "https://api.instagram.com/oauth/access_token";
    public static final String APIURL = "https://api.instagram.com/v1";
    public static String CALLBACKURL = "http://simplemap";

    private String client_id = "49aa375ef20a4cc3a457dafe9d6fbbc3";
    private String client_secret = "41cd1460033145e1adab2305fe3d80d3";

    private String authURLString = AUTHURL + "?client_id=" + client_id + "&redirect_uri=" + CALLBACKURL + "&response_type=code&display=touch&scope=likes+comments+relationships";
    private String tokenURLString = TOKENURL + "?client_id=" + client_id + "&client_secret=" + client_secret + "&redirect_uri=" + CALLBACKURL + "&grant_type=authorization_code";

    public static String userID;
    public static String userName;
    public static String accessTokenString;
    public static String request_token;

    public static MapsActivity mainActivity;

    public InstagramLogin(MapsActivity act)
    {
        mainActivity = act;
    }

    InstagramFetchTask mFetchTasks;

    Boolean ready = false;

    public WebView RunLogin()
    {
        WebView webView = new WebView(mainActivity.getApplicationContext());
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new AuthWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(authURLString);
        return webView;
    }

    public void fetchPostsWithLocation(LatLng location) {
        if (mFetchTasks != null) {
            mFetchTasks.cancel(true);
            mFetchTasks = null;
        }

        mFetchTasks = new InstagramFetchTask();
        mFetchTasks.execute(location);
    }

    public class AuthWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(InstagramLogin.CALLBACKURL)) {
                System.out.println(url);
                String parts[] = url.split("=");

                InstagramLogin.request_token = parts[1];

                new InstagramAccessTokenTask().execute();
                return true;
            }
            return false;
        }
    }

    public class InstagramAccessTokenTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void...arg0) {
            try {
                URL url = new URL(tokenURLString);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("POST");
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpsURLConnection.getOutputStream());
                outputStreamWriter.write("client_id=" + client_id +
                        "&client_secret=" + client_secret +
                        "&grant_type=authorization_code" +
                        "&redirect_uri=" + CALLBACKURL +
                        "&code=" + request_token);
                outputStreamWriter.flush();

                String response = streamToString(httpsURLConnection.getInputStream());
                JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();

                accessTokenString = jsonObject.getString("access_token");

                return accessTokenString;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String accessToken) {
            ready = true;
            mainActivity.removeWebView();
        }
    }

    public class InstagramFetchTask extends AsyncTask<LatLng, Void, JSONArray> {
        protected JSONArray doInBackground(LatLng...locations) {
            LatLng location = locations[0];
            String urlString = APIURL + "/media/search?lat=" + location.latitude + "&lng=" + location.longitude + "&access_token=" + accessTokenString;
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                String response = streamToString(urlConnection.getInputStream());


                JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                return jsonArray;
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray results) {
            mainActivity.didFetchPosts(results);
            mFetchTasks = null;
        }
    }

    public static String streamToString(InputStream p_is)
    {
        try
        {
            BufferedReader m_br;
            StringBuffer m_outString = new StringBuffer();
            m_br = new BufferedReader(new InputStreamReader(p_is));
            String m_read = m_br.readLine();
            while(m_read != null)
            {
                m_outString.append(m_read);
                m_read =m_br.readLine();
            }
            return m_outString.toString();
        }
        catch (Exception p_ex)
        {
            p_ex.printStackTrace();
            return "";
        }
    }

    public Boolean isReady() {
        return ready;
    }
}
