package com.example.photogallery.http;

import android.net.Uri;
import android.util.Log;

import com.example.photogallery.Model.GalleryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 基本的网络连接代码
 */
public class FlickrFetchr {

    private static final String TAG = "FlickrFetchr";

    private static final String API_KEY = "13554564-338f4f40826510d26b1eab4d1";

    /**
     * 从指定URL获取原始数据并返回一个字节流数组
     * @param urlSpec
     * @return
     * @throws IOException
     */
    public byte[] getUrlBytes(String urlSpec) throws IOException{

        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()+ ": with"+urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer))>0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    /**
     * 将getUrlBytes(String)方法返回的结果转换为String
     * @param urlSpec
     * @return
     * @throws IOException
     */
    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    //JSON数据地址
    public List<GalleryItem> fetchItem(){

        List<GalleryItem> items = new ArrayList<>();

        try{
            String url = Uri.parse("https://pixabay.com/api/")
                    .buildUpon()
                    .appendQueryParameter("key",API_KEY)
                    .appendQueryParameter("image_type","photo")
                    .build().toString();

            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: "+jsonString);

            //解析Json数据
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items,jsonBody);

        }catch (IOException e){
            Log.e(TAG, "failed to fetch items", e);
        }catch (JSONException je){
            Log.e(TAG, "failed to parse JSON", je);
        }

        return items;
    }

    //解析JSON图片
    private void parseItems(List<GalleryItem>items,JSONObject jsonObject) throws IOException,JSONException{

        //JSONObject photosJsonObject = jsonObject.getJSONObject(" ");
        JSONArray photoJsonArray = jsonObject.getJSONArray("hits");

        for (int i = 0; i<photoJsonArray.length();i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("user"));
            item.setUrl(photoJsonObject.getString("previewURL"));

            items.add(item);
        }

    }
}
