package cn.ruc.readio.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
    public static String BASE_URL = "killuayz.top"; //http://killuayz.top:5000  http://127.0.0.1:5000
    public static String BASE_SCHEME = "http";
    public static int BASE_PORT = 5000;

    public static int getServer(Activity activity) {
//        BASE_URL = readHttpHost(activity);
        if (BASE_URL == "killuayz.top") {
            return 0;
        } else {
            return 1;
        }
    }

    public static void setBaseUrl_Tencent(Activity activity) {
        BASE_URL = "killuayz.top";
        changeHttpHost(activity, BASE_URL);
    }

    public static void setBaseUrl_550w(Activity activity) {
        BASE_URL = "server.killuayz.top";
        changeHttpHost(activity, BASE_URL);
    }

    public static void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
    }


    public static void changeHttpHost(Activity activity, String host) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("SERVER_HOST", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("SERVER_HOST", host);
        editor.commit();
    }

    public static String readHttpHost(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("SERVER_HOST", Context.MODE_PRIVATE);
        return sharedPreferences.getString("SERVER_HOST", "");
    }

    /*
    不带Token的异步get请求
    @param String address 对应的api
    @param ArrayList<Pair<String,String>> queryParameter 查询参数
    @param Callback callback 回调函数，即向服务器请求成功或失败后调用的函数
    * */
    public static void getRequestAsyn(String address, ArrayList<Pair<String, String>> queryParameter, Callback callback) {
        Log.d("HttpUtil", "正在访问 " + BASE_URL + address);
        HttpUrl.Builder url = new HttpUrl.Builder()
                .host(BASE_URL)
                .port(BASE_PORT)
                .scheme(BASE_SCHEME)
                .addPathSegment(address);

        queryParameter.forEach((item) -> {
            url.addQueryParameter(item.first, item.second);
        });

        Request request = new Request.Builder()
                .url(url.build())
                .get()
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(callback);
    }

    /*
    不带Token的同步get请求
    @param String address 对应的api
    @param ArrayList<Pair<String,String>> queryParameter 查询参数
    @return Response okhttp3的Response类型，可以获取到服务器返回的状态码，数据等内容
    * */
    public static Response getRequestSyn(String address, ArrayList<Pair<String, String>> queryParameter) throws IOException {
        Log.d("HttpUtil", "正在访问 " + BASE_URL + address);
        HttpUrl.Builder url = new HttpUrl.Builder()
                .host(BASE_URL)
                .port(BASE_PORT)
                .scheme(BASE_SCHEME)
                .addPathSegment(address);

        queryParameter.forEach((item) -> {
            url.addQueryParameter(item.first, item.second);
        });

        Request request = new Request.Builder()
                .url(url.build())
                .get()
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        return okHttpClient.newCall(request).execute();
    }

    /*
    带Token的异步get请求
    @param Activity activity 调用该函数的activity(如果是activity调用就传this，如果是fragment调用就传getActivity())
    @param String address 对应的api
    @param ArrayList<Pair<String,String>> queryParameter 查询参数
    @param Callback callback 回调函数，即向服务器请求成功或失败后调用的函数
    * */
    public static void getRequestWithTokenAsyn(Activity activity, String address, ArrayList<Pair<String, String>> queryParameter, Callback callback) {
        Log.d("HttpUtil", "正在访问 " + BASE_URL + address);
        HttpUrl.Builder url = new HttpUrl.Builder()
                .host(BASE_URL)
                .port(BASE_PORT)
                .scheme(BASE_SCHEME)
                .addPathSegment(address);

        queryParameter.forEach((item) -> {
            url.addQueryParameter(item.first, item.second);
        });

        Auth.Token token = new Auth.Token(activity);

        Request request = new Request.Builder()
                .url(url.build())
                .get()
                .addHeader("Authorization", token.getToken())
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public static void getRequestWithTokenAsyn(Activity activity, String address, ArrayList<Pair<String, String>> queryParameter, Callback callback, int needJump) {
        Log.d("HttpUtil", "正在访问 " + BASE_URL + address);
        HttpUrl.Builder url = new HttpUrl.Builder()
                .host(BASE_URL)
                .port(BASE_PORT)
                .scheme(BASE_SCHEME)
                .addPathSegment(address);

        queryParameter.forEach((item) -> {
            url.addQueryParameter(item.first, item.second);
        });

        Auth.Token token = new Auth.Token(activity, needJump);

        Request request = new Request.Builder()
                .url(url.build())
                .get()
                .addHeader("Authorization", token.getToken())
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(callback);
    }

    /*
    带Token的同步get请求
    @param Activity activity 调用该函数的activity(如果是activity调用就传this，如果是fragment调用就传getActivity())
    @param String address 对应的api
    @param ArrayList<Pair<String,String>> queryParameter 查询参数
    @return Response okhttp3的Response类型，可以获取到服务器返回的状态码，数据等内容
    * */
    public static Response getRequestWithTokenSyn(Activity activity, String address, ArrayList<Pair<String, String>> queryParameter) throws IOException {
        Log.d("HttpUtil", "正在访问 " + BASE_URL + address);
        HttpUrl.Builder url = new HttpUrl.Builder()
                .host(BASE_URL)
                .port(BASE_PORT)
                .scheme(BASE_SCHEME)
                .addPathSegment(address);

        queryParameter.forEach((item) -> {
            url.addQueryParameter(item.first, item.second);
        });

        Auth.Token token = new Auth.Token(activity);

        Request request = new Request.Builder()
                .url(url.build())
                .get()
                .addHeader("Authorization", token.getToken())
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        return okHttpClient.newCall(request).execute();
    }

    /*
    不带Token的异步post请求
    @param String address 对应的api
    @param RequestBody requestBody 报文体内容，类型是okhttp3的RequestBody类型
    @param Callback callback 回调函数，即向服务器请求成功或失败后调用的函数
    * */
    public static void postRequestAsyn(String address, RequestBody requestBody, Callback callback) {

        HttpUrl.Builder url = new HttpUrl.Builder()
                .host(BASE_URL)
                .port(BASE_PORT)
                .scheme(BASE_SCHEME)
                .addPathSegment(address);

        Log.d("HttpUtil", "postRequest正在访问 " + url.build().toString());
        Request request = new Request.Builder()
                .url(url.build())
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(callback);
    }

    /*
    不带Token的同步post请求
    @param String address 对应的api
    @param RequestBody requestBody 报文体内容，类型是okhttp3的RequestBody类型
    @return Response okhttp3的Response类型，可以获取到服务器返回的状态码，数据等内容
    * */
    public static Response postRequestSyn(String address, RequestBody requestBody) throws IOException {

        HttpUrl.Builder url = new HttpUrl.Builder()
                .host(BASE_URL)
                .port(BASE_PORT)
                .scheme(BASE_SCHEME)
                .addPathSegment(address);

        Log.d("HttpUtil", "postRequest正在访问 " + url.build().toString());
        Request request = new Request.Builder()
                .url(url.build())
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        return okHttpClient.newCall(request).execute();
    }

    /*
    带Token的异步post请求
    @param Activity activity 调用该函数的activity(如果是activity调用就传this，如果是fragment调用就传getActivity())
    @param String address 对应的api
    @param RequestBody requestBody 报文体内容，类型是okhttp3的RequestBody类型
    @param Callback callback 回调函数，即向服务器请求成功或失败后调用的函数
    * */
    public static void postRequestWithTokenAsyn(Activity activity, String address, RequestBody requestBody, Callback callback) {
        Log.d("HttpUtil", "正在访问 " + BASE_URL + address);
        HttpUrl.Builder url = new HttpUrl.Builder()
                .host(BASE_URL)
                .port(BASE_PORT)
                .scheme(BASE_SCHEME)
                .addPathSegment(address);

        Auth.Token token = new Auth.Token(activity);

        Request request = new Request.Builder()
                .url(url.build())
                .post(requestBody)
                .addHeader("Authorization", token.getToken())
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(callback);
    }

    /*
    带Token的同步post请求
    @param Activity activity 调用该函数的activity(如果是activity调用就传this，如果是fragment调用就传getActivity())
    @param String address 对应的api
    @param RequestBody requestBody 报文体内容，类型是okhttp3的RequestBody类型
    @return Response okhttp3的Response类型，可以获取到服务器返回的状态码，数据等内容
    * */
    public static Response postRequestWithTokenSyn(Activity activity, String address, RequestBody requestBody) throws IOException {
        Log.d("HttpUtil", "正在访问 " + BASE_URL + address);
        HttpUrl.Builder url = new HttpUrl.Builder()
                .host(BASE_URL)
                .port(BASE_PORT)
                .scheme(BASE_SCHEME)
                .addPathSegment(address);

        Auth.Token token = new Auth.Token(activity);

        Request request = new Request.Builder()
                .url(url.build())
                .post(requestBody)
                .addHeader("Authorization", token.getToken())
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        return okHttpClient.newCall(request).execute();
    }

    /*
    不带Token的异步post请求
    @param String address 对应的api
    @param String jsonString Json字符串，作为报文体内容
    @param Callback callback 回调函数，即向服务器请求成功或失败后调用的函数
    * */
    public static void postRequestJsonAsyn(String address, String jsonString, Callback callback) {
//        Log.d("HttpUtil","正在访问 "+BASE_URL + address + "\n body = "+jsonString);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonString);

        HttpUrl.Builder url = new HttpUrl.Builder()
                .host(BASE_URL)
                .port(BASE_PORT)
                .scheme(BASE_SCHEME)
                .addPathSegment(address);

        Log.d("HttpUtil", "postRequestJson正在访问 " + url.build().toString() + "\n body = " + jsonString);
        Request request = new Request.Builder()
                .url(url.build())
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(callback);
    }

    /*
    不带Token的同步post请求
    @param String address 对应的api
    @param String jsonString Json字符串，作为报文体内容
    @return Response okhttp3的Response类型，可以获取到服务器返回的状态码，数据等内容
    * */
    public static Response postRequestJsonSyn(String address, String jsonString) throws IOException {
//        Log.d("HttpUtil","正在访问 "+BASE_URL + address + "\n body = "+jsonString);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonString);

        HttpUrl.Builder url = new HttpUrl.Builder()
                .host(BASE_URL)
                .port(BASE_PORT)
                .scheme(BASE_SCHEME)
                .addPathSegment(address);

        Log.d("HttpUtil", "postRequestJson正在访问 " + url.build().toString() + "\n body = " + jsonString);
        Request request = new Request.Builder()
                .url(url.build())
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        return okHttpClient.newCall(request).execute();
    }

    /*
    带Token的异步post请求
    @param Activity activity 调用该函数的activity(如果是activity调用就传this，如果是fragment调用就传getActivity())
    @param String address 对应的api
    @param String jsonString Json字符串，作为报文体内容
    @param Callback callback 回调函数，即向服务器请求成功或失败后调用的函数
    * */
    public static void postRequestWithTokenJsonAsyn(Activity activity, String address, String jsonString, Callback callback) {
        Log.d("HttpUtil", "正在访问 " + BASE_URL + address);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonString);

        HttpUrl.Builder url = new HttpUrl.Builder()
                .host(BASE_URL)
                .port(BASE_PORT)
                .scheme(BASE_SCHEME)
                .addPathSegment(address);

        Auth.Token token = new Auth.Token(activity);

        Request request = new Request.Builder()
                .url(url.build())
                .post(requestBody)
                .addHeader("Authorization", token.getToken())
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(callback);
    }

    /*
    带Token的同步post请求
    @param Activity activity 调用该函数的activity(如果是activity调用就传this，如果是fragment调用就传getActivity())
    @param String address 对应的api
    @param String jsonString Json字符串，作为报文体内容
    @return Response okhttp3的Response类型，可以获取到服务器返回的状态码，数据等内容
    * */
    public static Response postRequestWithTokenJsonSyn(Activity activity, String address, String jsonString, Callback callback) throws IOException {
        Log.d("HttpUtil", "正在访问 " + BASE_URL + address);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonString);

        HttpUrl.Builder url = new HttpUrl.Builder()
                .host(BASE_URL)
                .port(BASE_PORT)
                .scheme(BASE_SCHEME)
                .addPathSegment(address);

        Auth.Token token = new Auth.Token(activity);

        Request request = new Request.Builder()
                .url(url.build())
                .post(requestBody)
                .addHeader("Authorization", token.getToken())
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        return okHttpClient.newCall(request).execute();
    }


    public static void getAvaAsyn(String avaId, ImageView view, FragmentActivity avtivity) {
        ArrayList<Pair<String, String>> queryParam = new ArrayList<>();
        queryParam.add(Pair.create("fileId", avaId));
        Log.d("avatorload", "进入函数");
        HttpUtil.getRequestAsyn("/file/getFileBinaryById", queryParam, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(view.getContext(), "加载失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.code() == 200) {
                    Log.d("avatorload", "hello");
                    byte[] picBytes = response.body().bytes();
                    Bitmap pic = BitmapFactory.decodeByteArray(picBytes, 0, picBytes.length);
                    avtivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("avatorload", "即将设置");
                            view.setImageBitmap(pic);
                            Log.d("avatorload", "设置完毕");
                        }
                    });

                } else {
                    Log.d("avatorload", "出问题啦");
                }

            }
        });
    }


    public static Bitmap getAvaSyn(String avaId) {
        ArrayList<Pair<String, String>> queryParam = new ArrayList<>();
        queryParam.add(Pair.create("fileId", avaId));
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


//    public static String getIPAddress(Context context, Activity activity) {
//        NetworkInfo info = ((ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
//        if (info != null && info.isConnected()) {
//            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
//                try {
//                    Log.d("当前使用网络", "正在使用移动网络");
//                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
//                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
//                        NetworkInterface intf = en.nextElement();
//                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//                            InetAddress inetAddress = enumIpAddr.nextElement();
//                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
//                                return inetAddress.getHostAddress();
//                            }
//                        }
//                    }
//                } catch (SocketException e) {
//                    e.printStackTrace();
//                }
//
//            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
//                Log.d("当前使用网络", "正在使用WIFI");
//                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
//                return ipAddress;
//            }
//        } else {
//            //当前无网络连接,请在设置中打开网络
//            Tools.my_toast(activity, "当前无网络连接，请在设置中打开网络");
//        }
//        return null;
//    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }


    //基于规则选择ip地址
    public static void updateHttpHostByRule(Context context, Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        if (nc == null) {
            Tools.my_toast(activity, "请打开网络连接");
        } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            //当前正在使用WIFI
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
            if (ipAddress.startsWith("10")) {
                //如果以10开头，说明大概率连接了校园网，此时就用10.47.40.87
                setBaseUrl("10.47.40.87");
            } else {
                setBaseUrl("killuayz.top");
            }
        } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            //当前正在使用移动网络
            //如果正在使用移动网络，则一律使用killuaz.top域名访问
            setBaseUrl("killuayz.top");
        }
    }

    public static void updateHttpHostByPing(Activity activity) {
        HttpUtil.getRequestAsyn("/hello", new ArrayList<>(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("Ping Service", "访问失败，需要更改ip，当前ip：" + BASE_URL);
                if (BASE_URL.equals("killuayz.top")) {
                    setBaseUrl("10.47.40.87");
                    Log.d("Ping Service", "更改ip为：" + BASE_URL);
                } else {
                    setBaseUrl("killuayz.top");
                    Log.d("Ping Service", "更改ip为：" + BASE_URL);
                }

                HttpUtil.getRequestAsyn("/hello", new ArrayList<>(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("Ping Service", "更改ip后无法正常访问，当前ip：" + BASE_URL);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        //能够ping通，说明不需要改变
                        Log.d("Ping Service", "更改ip后可以正常访问，当前ip：" + BASE_URL);
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //能够正确访问，无需改变
                Log.d("Ping Service", "可以正常访问，无需更改ip，当前ip：" + BASE_URL);
            }
        });
    }

}
