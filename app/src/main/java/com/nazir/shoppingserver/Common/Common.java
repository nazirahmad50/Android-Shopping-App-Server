package com.nazir.shoppingserver.Common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.nazir.shoppingserver.Model.Request;
import com.nazir.shoppingserver.Model.User;
import com.nazir.shoppingserver.Remote.APIService;
import com.nazir.shoppingserver.Remote.FCMRetrofitClient;
import com.nazir.shoppingserver.Remote.IGeoCoordinates;
import com.nazir.shoppingserver.Remote.RetrofitClient;

public class Common {

    //variable to save current user
    public static User cuurentUser;
    public static Request cuurentRequest;
    public static final String PHONE_TEXT = "userPhone";


    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";

    public static final int PICK_IMAGE_REQUEST = 71;

    public static String convertCodeToString(String code){

        if (code.equals("0")){
            return "Placed";
        }else if (code.equals("1")){
            return "On way";
        }else{
            return "Shipped";
        }
    }


    //************************************Map functions*************************************

    public static final String baseUrl = "https://maps.googleapis.com";

    public static IGeoCoordinates getGeoCoordinatesService(){

        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);

    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight){

        Bitmap scaleBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float)bitmap.getWidth();
        float scaleY = newHeight / (float)bitmap.getHeight();
        float pivotX = 0, pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);

        Canvas canvas = new Canvas(scaleBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaleBitmap;

    }


    //************************************Notification functions*************************************


    public static final String FCM_URL = "https://fcm.googleapis.com/";

    public static APIService getFCMService(){

        return FCMRetrofitClient.getClient(FCM_URL).create(APIService.class);
    }


}
