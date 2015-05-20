package com.example.lephuongmy.mymap;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class InstagramPost implements Parcelable{
    LatLng mLocation;
    String mCaptionText;
    String mImageURLStringLow;
    String mImageURLStringStandard;
    String mProfilePictureURLString;
    String mFullName;
    int    mCommentCount;
    int    mLikeCount;

    public InstagramPost(JSONObject post) {
        try {
            if (post.has("location") && !post.isNull("location")) {
                JSONObject location = post.getJSONObject("location");
                mLocation = new LatLng(location.getDouble("latitude"), location.getDouble("longitude"));
            }
            mCaptionText = "";
            if (post.has("caption") && !post.isNull("caption")) {
                JSONObject caption = post.getJSONObject("caption");
                mCaptionText = caption.optString("text", "");
            }

            JSONObject images = post.getJSONObject("images");
            JSONObject low = images.getJSONObject("low_resolution");
            JSONObject standard = images.getJSONObject("standard_resolution");
            mImageURLStringLow = low.getString("url");
            mImageURLStringStandard = low.getString("url");
            JSONObject user = post.getJSONObject("user");
            mFullName = user.getString("full_name");
            mProfilePictureURLString = user.getString("profile_picture");
            JSONObject comments = post.getJSONObject("comments");
            mCommentCount = comments.getInt("count");
            JSONObject likes = post.getJSONObject("likes");
            mLikeCount = likes.getInt("count");
        } catch (JSONException e) {

        }
    }

    public MarkerOptions marker() {
        MarkerOptions marker = new MarkerOptions()
                .position(mLocation)
                .title(mFullName)
                .snippet(mCaptionText)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        return marker;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(mLocation.latitude);
        dest.writeDouble(mLocation.longitude);
        dest.writeString(mCaptionText);
        dest.writeString(mImageURLStringLow);
        dest.writeString(mImageURLStringStandard);
        dest.writeString(mProfilePictureURLString);
        dest.writeString(mFullName);
        dest.writeInt(mCommentCount);
        dest.writeInt(mLikeCount);
    }

    private InstagramPost(Parcel in) {
        mLocation = new LatLng(in.readDouble(), in.readDouble());
        mCaptionText = in.readString();
        mImageURLStringLow = in.readString();
        mImageURLStringStandard = in.readString();
        mProfilePictureURLString = in.readString();
        mFullName= in.readString();
        mCommentCount = in.readInt();
        mLikeCount = in.readInt();
    }

    public static final Parcelable.Creator<InstagramPost> CREATOR = new Parcelable.Creator<InstagramPost>() {
        public InstagramPost createFromParcel(Parcel source) {
            return new InstagramPost(source);
        }

        public InstagramPost[] newArray(int size) {
            return new InstagramPost[size];
        }
    };
}
