package cn.ruc.readio.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.Date;

import cn.ruc.readio.MainActivity;
import cn.ruc.readio.util.HttpUtil;

public class PingService extends Service {

    public PingService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Ping Service", "执行时间" + new Date());
                HttpUtil.updateHttpHostByPing(MainActivity.mainAct);
            }
        }).start();

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anMinute = 5 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anMinute;
        Intent intent1 = new Intent(this, AlarmReceiver.class);
        int flag = 0;
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent1, flag | PendingIntent.FLAG_IMMUTABLE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }
}