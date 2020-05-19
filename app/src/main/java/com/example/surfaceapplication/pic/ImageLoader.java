package com.example.surfaceapplication.pic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description: image 磁盘加载器
 * @Author: forJrking
 * @Version: 1.0.0
 */
public class ImageLoader {

    private static final String TAG = "ImageLoader";
    private static final int MESSAGE_POST_RESULT = 102;
    /**
     * DES: 内存缓存
     */
    private LruCache<String, Bitmap> bitmapCache;
    private volatile static ImageLoader sImageLoader;

    private final ExecutorService threadPool;
    /**
     * DES: 主线程调度
     */
    private final InternalHandler handler;


    private ImageLoader() {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                if (value == null) {
                    return super.sizeOf(key, value);
                }
                return value.getRowBytes() * value.getHeight();
            }

        };

        threadPool = Executors.newCachedThreadPool();
        handler = new InternalHandler(Looper.getMainLooper());
    }

    public static ImageLoader getInstance() {
        if (sImageLoader == null) {
            synchronized (ImageLoader.class) {
                if (sImageLoader == null) {
                    sImageLoader = new ImageLoader();
                }
            }
        }
        return sImageLoader;
    }


    public void loadImage(final String path, final ImageView view) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                int width = view.getWidth();
                int height = view.getHeight();
                Bitmap bitmap = loadImage(path, width, height, Bitmap.Config.ARGB_8888, true);
                view.setTag(bitmap);
                Message obtain = Message.obtain();
                obtain.what = MESSAGE_POST_RESULT;
                obtain.obj = new WeakReference<ImageView>(view);
                handler.sendMessage(obtain);
            }
        });
    }

    /**
     * 加载图片
     */
    public Bitmap loadImage(String path, int dstWidth, int dstHeight) {
        return loadImage(path, dstWidth, dstHeight, Bitmap.Config.ARGB_8888, true);
    }

    /**
     * 加载图片
     */
    public Bitmap loadImage(String path, int dstWidth, int dstHeight, boolean isUseCache) {
        return loadImage(path, dstWidth, dstHeight, Bitmap.Config.ARGB_8888, isUseCache);
    }

    /**
     * 加载图片
     */
    public Bitmap loadImage(String path, int dstWidth, int dstHeight, Bitmap.Config config, boolean isUseCache) {
        Bitmap bitmap;
        Log.d(TAG, "loadImage:" + Runtime.getRuntime().freeMemory());
        if (isUseCache) {
            Bitmap bitmapSoftReference = bitmapCache.get(key(path, dstWidth, dstHeight));
            if (bitmapSoftReference != null && !bitmapSoftReference.isRecycled() && dstWidth <= bitmapSoftReference.getWidth() && dstHeight <= bitmapSoftReference.getHeight()) {
                Log.d(TAG, "bitmap is cache,the path:" + path);
                return bitmapSoftReference;
            } else {
                bitmap = getBitmap(path, dstWidth, dstHeight, config);
                if (bitmap != null) {
                    if (bitmapCache != null) {
                        bitmapCache.put(key(path, dstWidth, dstHeight), bitmap);
                        Log.d(TAG, "bitmap put cache,the path:" + path);
                    }
                }
                return bitmap;
            }
        } else {
            return getBitmap(path, dstWidth, dstHeight, config);
        }
    }

    /**
     * key 组合
     **/
    private String key(String path, int dstWidth, int dstHeight) {
        return path + "_" + dstWidth + "_" + dstHeight;
    }

    private Bitmap getBitmap(String path, int dstWidth, int dstHeight, Bitmap.Config config) {
        Bitmap bitmap = null;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opt);

        float picWidth = opt.outWidth;
        float picHeight = opt.outHeight;
        // DES: 解码失败不是图片文件
        if (picWidth != -1 && picHeight != -1) {
            if (dstWidth <= 0) {
                dstWidth = (int) picWidth;
            }
            if (dstHeight <= 0) {
                dstHeight = (int) picHeight;
            }
            int heightRatio = (int) (picHeight / dstHeight);
            int widthRatio = (int) (picWidth / dstWidth);

            int minRatio = Math.min(heightRatio, widthRatio);

            if (minRatio < 1) {
                minRatio = 1;
            }
            // DES: 采样系数
            opt.inSampleSize = minRatio;
            // DES: 颜色模式
            opt.inPreferredConfig = config;
            // DES: 进入内存
            opt.inJustDecodeBounds = false;

            bitmap = BitmapFactory.decodeFile(path, opt);
        }
        if (bitmap == null) {
            Log.d(TAG, "getBitmap is null");
        }
        return bitmap;
    }

    /**
     * 清除所有图片缓存
     */
    public void remove(String key) {
        if (bitmapCache != null) {
            bitmapCache.remove(key);
        }
    }

    private static class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
//            Log.d(TAG,"handleMessage");
            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    WeakReference<ImageView> viewWeak = (WeakReference<ImageView>) msg.obj;
                    ImageView imageView = viewWeak.get();
                    if (imageView != null && imageView.getTag() instanceof Bitmap) {
                        Bitmap tag = (Bitmap) imageView.getTag();
                        imageView.setImageBitmap(tag);
                        imageView.setTag(null);
                    } else {
                        Log.d(TAG, "ImageView is null3");
                    }
                    break;
                default:
            }
        }
    }

}
