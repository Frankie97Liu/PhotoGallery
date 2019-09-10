package com.example.photogallery;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.photogallery.JSON.FlickrFetchr;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownload";

    //message what参数
    private static final int MESSAGE_DOWNLOAD = 0;

    private Boolean mHasQuit = false;

    //消息请求的handler
    private Handler mRequestHandler; //负责在ThumbnailDownload后台线程上管理下载请求消息队列,取出并下载请求消息

    private ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();

    //消息处理handler
    private Handler mResponseHandler;//存放来在主线程的handler

    //新增监听器
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;//主线程发送请求，响应结果是下载图片

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target,Bitmap thumbnail);
    }

    public void setThumbnailDownloaderListener(ThumbnailDownloadListener<T> listener){
        mThumbnailDownloadListener = listener;
    }

    //构造函数
    public ThumbnailDownloader(Handler responseHandler){
        super(TAG);
        mResponseHandler = responseHandler;
    }

    //looper开始
    @Override
    protected void onLooperPrepared() {
        //初始化handler
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD){
                    T target = (T) msg.obj;
                    Log.i(TAG, "request for URL:"+ mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };

    }



    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }


    public void queueThumbnail(T target, String url){
        Log.i(TAG, "Go to a URL:"+url);

        //发送消息
        if (url == null){
            mRequestMap.remove(target);
        }else {
            mRequestMap.put(target,url);
            //发送要下载图片的消息
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();
        }
    }

    //清理消息队列
    public void clearQueue(){
        mResponseHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }


    /**
     * 下载图片执行方法
     * @param target
     */
    private void handleRequest(final T target){
        try{
            final String url = mRequestMap.get(target);

            if (url == null){
                return;
            }

            //确认url有效后，将他传递给FlickrFetchr新实例
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            //把getUrlBytes()返回的字节数组转换为位图
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);

            Log.i(TAG, "Bitmap created");

            //图片下载与显示
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url || mHasQuit){
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target,bitmap);
                }
            });
        }catch (IOException e){
            Log.e(TAG, "Error downloading image",e );
        }
    }


}