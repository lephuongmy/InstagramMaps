package com.example.lephuongmy.mymap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class DetailsActivity extends Activity {
    InstagramPost mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        if (mPost == null) {
            mPost = getIntent().getExtras().getParcelable("post");
        }

        if (mPost != null) {
            TextView tvTitle = ((TextView)findViewById(R.id.username));
            tvTitle.setText(mPost.mFullName);

            TextView tvSnippet = ((TextView)findViewById(R.id.caption));
            tvSnippet.setText(mPost.mCaptionText);


            final ImageView profileImage = ((ImageView)findViewById(R.id.profilePicture));

            AsyncTask downImageTask = new AsyncTask<Object, Void, Bitmap>() {
                protected Bitmap doInBackground(Object[] arg0) {
                    try {
                        /*
                        URL urlConnection = new URL(mPost.mProfilePictureURLString);
                        HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                        //connection.setDoInput(true);
                        //connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);*/

                        HttpGet httpRequest = new HttpGet(URI.create(mPost.mProfilePictureURLString) );
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
                        HttpEntity entity = response.getEntity();
                        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
                        Bitmap myBitmap = BitmapFactory.decodeStream(bufHttpEntity.getContent());
                        httpRequest.abort();

                        return myBitmap;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
                @Override
                protected void onPostExecute(Bitmap bm) {
                    if(bm != null)
                        profileImage.setImageBitmap(bm);
                }
            }.execute();

            final ImageView postImage = ((ImageView)findViewById(R.id.postPicture));

            AsyncTask downImageTask2 = new AsyncTask<Object, Void, Bitmap>() {
                protected Bitmap doInBackground(Object[] arg0) {
                    try {
                        URL urlConnection = new URL(mPost.mImageURLStringStandard);
                        HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);

                        return myBitmap;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
                @Override
                protected void onPostExecute(Bitmap bm) {
                    if(bm != null)
                        postImage.setImageBitmap(bm);
                }
            }.execute();
        }
    }
}
