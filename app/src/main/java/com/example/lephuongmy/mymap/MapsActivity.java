package com.example.lephuongmy.mymap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity {
    private GoogleMap mMap;
    InstagramLogin instagramController;
    WebView wbview;
    Boolean mShouldZoomToUserLocation = true;
    Boolean ignoreMapChange = false;
    HashMap<Marker, InstagramPost> mMarkerPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        if (mMarkerPost == null) {
            mMarkerPost = new HashMap<>();
        }
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);

                instagramController = new InstagramLogin(this);

                wbview = instagramController.RunLogin();
                addContentView(wbview, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
            }
        }
    }

    public void removeWebView(){
        //ViewGroup vg = (ViewGroup)findViewById(R.layout.activity_maps).getRootView();
        FrameLayout v = (FrameLayout)wbview.getParent();
        v.removeView(wbview);
        setUpMap();
    }

    public void setUpMap() {
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (mShouldZoomToUserLocation) {
                    mShouldZoomToUserLocation = false;
                    instagramController.fetchPostsWithLocation( new LatLng(location.getLatitude(), location.getLongitude()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), 13));
                }
            }
        });
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (!ignoreMapChange) {
                    instagramController.fetchPostsWithLocation(cameraPosition.target);
                }
                ignoreMapChange = false;
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ignoreMapChange = true;
                InstagramPost post = mMarkerPost.get(marker);
                if (post != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("post", post);
                    Intent intent = new Intent(MapsActivity.this, DetailsActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                return false;
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(final Marker marker) {
                View myContentView = getLayoutInflater().inflate(R.layout.info_window, null);

                final InstagramPost post = mMarkerPost.get(marker);

                TextView tvTitle = ((TextView) myContentView.findViewById(R.id.title));
                tvTitle.setText(post.mFullName);

                TextView tvSnippet = ((TextView) myContentView.findViewById(R.id.snippet));
                tvSnippet.setText(post.mCaptionText);

                return myContentView;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                InstagramPost post = mMarkerPost.get(marker);
                if (post != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("post", post);
                    Intent intent = new Intent(MapsActivity.this, DetailsActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
    }

    public void didFetchPosts(JSONArray posts) {
        try {
            ArrayList<InstagramPost> newPosts = new ArrayList<>();
            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.getJSONObject(i);
                InstagramPost igPost = new InstagramPost(post);
                newPosts.add(igPost);

            }
            mMarkerPost.clear();
            mMap.clear();

            for (int i = 0; i < newPosts.size(); i++) {
                InstagramPost igPost = newPosts.get(i);
                Marker marker = mMap.addMarker(igPost.marker());
                mMarkerPost.put(marker, igPost);
            }

        } catch (JSONException e) {

        }
    }
}