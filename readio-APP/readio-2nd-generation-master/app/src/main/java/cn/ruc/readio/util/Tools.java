package cn.ruc.readio.util;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import cn.ruc.readio.entity.FileInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Tools {
    public static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


    //自定义Toast，提高复用性
    public static void my_toast(Activity activity, String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /*
    异步方式获取图片
    String fileId : 图片文件Id
    ImageView view : 加载图片的ImageView
    Activity activity : 加载图片的activity
    */
    public static void getImageBitmapAsyn(String fileId, ImageView view, Activity activity) throws IOException, ParseException {
        Bitmap pic = getImageBitmapFromLocal(activity, fileId);
        if (pic == null) {
            //从服务器异步获取
            getImageBitmapFromServerAsyn(fileId, view, activity);
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setImageBitmap(pic);
                }
            });
        }
    }

    /*
   同步方式获取图片
   String fileId : 图片文件Id
   Activity activity : 加载图片的activity
   return Bitmap
   */
    public static Bitmap getImageBitmapSyn(Activity activity, String fileId) throws IOException, JSONException, ParseException {
        Bitmap pic = getImageBitmapFromLocal(activity, fileId);
        if (pic == null) {
            //本地妹有，从云端拿
            Bitmap pic_server = getImageBitmapFromServerSyn(fileId);
            FileInfo serverFileInfo = getFileInfoByFileId(fileId);
            FileReader reader = new FileReader(activity);
            reader.insert(serverFileInfo);
            return pic_server;
        }
        return pic;
    }

    public static boolean localDatabaseHasFile(Activity activity, String fileId) throws IOException, ParseException {
        FileReader fileReader = new FileReader(activity);
        FileInfo fileInfo = fileReader.getFileInfoByFileId(fileId);
        if (fileInfo == null) {
            return false;
        }
        return true;
    }

    public static Bitmap getImageBitmapFromLocal(Activity activity, String fileId) throws IOException, ParseException {
        FileReader fileReader = new FileReader(activity);
        FileInfo fileInfo = fileReader.getFileInfoByFileId(fileId);
        if (fileInfo != null && fileInfo.getContent() != null) {
            Bitmap pic = BitmapFactory.decodeByteArray(fileInfo.getContent(), 0, fileInfo.getContent().length);
            return pic;
        }
        return null;
    }

    public static void getImageBitmapFromServerAsyn(String fileId, ImageView view, Activity activity) {
        ArrayList<Pair<String, String>> queryParam = new ArrayList<>();
        queryParam.add(Pair.create("fileId", fileId));
        Log.d("avatorload", "进入函数");
        HttpUtil.getRequestAsyn("/file/getFileBinaryById", queryParam, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Tools.my_toast(activity, "获取图片失败，请检查网络连接");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.code() == 200) {
                    Log.d("avatorload", "hello");
                    byte[] picBytes = response.body().bytes();
                    Bitmap pic = BitmapFactory.decodeByteArray(picBytes, 0, picBytes.length);
                    try {
                        FileInfo serverFileInfo = getFileInfoByFileId(fileId);
                        FileReader reader = new FileReader(activity);
                        reader.insert(serverFileInfo);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("avatorload", "即将设置");
                            view.setImageBitmap(pic);
                            Log.d("avatorload", "设置完毕");
                        }
                    });

                } else {
                    Log.d("avatorload", "出问题啦");
                    try {
                        Tools.my_toast(activity, new JSONObject(response.body().string()).getString("msg"));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        });
    }

    public static Bitmap getImageBitmapFromServerSyn(String fileId) {
        ArrayList<Pair<String, String>> queryParam = new ArrayList<>();
        queryParam.add(Pair.create("fileId", fileId));
        Bitmap pic = null;
        try {
            Response response = HttpUtil.getRequestSyn("/file/getFileBinaryById", queryParam);
            byte[] picBytes = response.body().bytes();
            pic = BitmapFactory.decodeByteArray(picBytes, 0, picBytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pic;
    }

    public static byte[] getImageByteFromServerSyn(String fileId) {
        ArrayList<Pair<String, String>> queryParam = new ArrayList<>();
        queryParam.add(Pair.create("fileId", fileId));
        byte[] picBytes = null;
        try {
            Response response = HttpUtil.getRequestSyn("/file/getFileBinaryById", queryParam);
            picBytes = response.body().bytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return picBytes;
    }

    public static FileInfo getFileInfoByFileId(String fileId) throws IOException, JSONException, ParseException {
        ArrayList<Pair<String, String>> queryParam = new ArrayList<>();
        queryParam.add(Pair.create("fileId", fileId));
        Response response = HttpUtil.getRequestSyn("/file/getFileInfo", queryParam);
        if (response.code() != 200) {
            return null;
        }
        JSONObject jsonObject = new JSONObject(response.body().string());
        JSONArray dataArray = jsonObject.getJSONArray("data");
        if (dataArray.length() > 0) {
            JSONObject fileInfoObject = (JSONObject) jsonObject.getJSONArray("data").get(0);
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileId(fileInfoObject.getString("fileId"));
            fileInfo.setFileName(fileInfoObject.getString("fileName"));
            fileInfo.setFileType(fileInfoObject.getString("fileType"));
            fileInfo.setFilePath(fileInfoObject.getString("filePath"));
//            Log.d("createTime", fileInfoObject.getString("createTime"));
//            fileInfo.setCreateTime(formatter.parse(fileInfoObject.getString("createTime")));
//            fileInfo.setVisitTime(formatter.parse(fileInfoObject.getString("visitTime")));
            fileInfo.setContent(getImageByteFromServerSyn(fileId));
            return fileInfo;
        }

        return null;
    }

    //异步随机获取图片
    //@param Activity activity  调用该函数的activity
    //@param ImageView view     装随机获取到的图片的view
    public static void randomGetImgAsyn(Activity activity, ImageView view) {
        HttpUtil.getRequestAsyn("/file/randomGetImgFileInfo", new ArrayList<>(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Tools.my_toast(activity, "请检查网络连接");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    try {
                        JSONObject obj = new JSONObject(response.body().string()).getJSONObject("data");
                        String fileId = obj.getString("fileId");
                        getImageBitmapAsyn(fileId, view, activity);
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("Tools.randomGetImgFileInfoSyn", "随机获取fileInfo失败，请检查网络连接");
                }
            }
        });
    }

    //同步随机获取图片
    //@param Activity activity  调用该函数的activity
    //@return Bitmap            随机获取到的图片转化成的Bitmap
    public static Bitmap randomGetImgSyn(Activity activity) throws JSONException, IOException, ParseException {
        FileInfo fileInfo = randomGetImgFileInfoSyn();
        if (fileInfo == null) {
            return null;
        }
        String fileId = fileInfo.getFileId();
        return getImageBitmapSyn(activity, fileId);
    }

    public static FileInfo randomGetImgFileInfoSyn() throws IOException, JSONException {

//        ArrayList<Pair<String, String>> queryParam = new ArrayList<>();
//        queryParam.add(Pair.create("userId", String.valueOf(3)));
//        queryParam.add(Pair.create("bookId", String.valueOf(6)));
//        queryParam.add(Pair.create("progress", String.valueOf(3)));
//        HttpUtil.getRequestSyn("/book/update", queryParam);

        Response response = HttpUtil.getRequestSyn("/file/randomGetImgFileInfo", new ArrayList<>());
        FileInfo fileInfo = new FileInfo();
        if (response.code() == 200) {
            JSONObject obj = new JSONObject(response.body().string()).getJSONObject("data");
            fileInfo.setFileId(obj.getString("fileId"));
            fileInfo.setFileName(obj.getString("fileName"));
            fileInfo.setFileType(obj.getString("fileType"));
            fileInfo.setFilePath(obj.getString("filePath"));
        } else {
            Log.e("Tools.randomGetImgFileInfoSyn", "随机获取fileInfo失败，请检查网络连接");
        }
        return (fileInfo.getFileId() != null && fileInfo.getFileId().length() > 0) ? fileInfo : null;
    }

    public static Bitmap ZoomBitmapImageMatchParent(Activity activity, Bitmap pic) {
        float old_width = pic.getWidth();
        float old_height = pic.getHeight();
        float new_width = activity.getWindowManager().getDefaultDisplay().getWidth() - 20;
        float scale = new_width / old_width;
        float new_height = scale * old_height;

        Log.d("PicScaleTest", "Old width = " + old_width);
        Log.d("PicScaleTest", "Old height = " + old_height);
        Log.d("PicScaleTest", "New width = " + new_width);
        Log.d("PicScaleTest", "New height = " + new_height);

        Matrix matrix = new Matrix();
        matrix.postScale((float) scale, (float) scale);

        Bitmap res = Bitmap.createBitmap(pic, 0, 0, (int) old_width, (int) old_height, matrix, true);
        return res;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 90;
        int length = baos.toByteArray().length / 1024;

        if (length > 5000) {
            //重置baos即清空baos
            baos.reset();
            //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        } else if (length > 4000) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        } else if (length > 3000) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        } else if (length > 2000) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        }
        //循环判断如果压缩后图片是否大于1M,大于继续压缩
        while (baos.toByteArray().length / 1024 > 1024) {
            //重置baos即清空baos
            baos.reset();
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            //每次都减少10
            options -= 10;
        }
        //把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        //把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }


}
