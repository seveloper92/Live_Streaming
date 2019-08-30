package com.example.dbsdn.mychat.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.example.dbsdn.mychat.LoginActivity;
import com.example.dbsdn.mychat.R;
import com.example.dbsdn.mychat.rtc.ConnectActivity;
import com.google.firebase.messaging.RemoteMessage;
import android.util.Log;
import android.widget.Toast;


public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    Context context;

    @Override
    public void onCreate() {
        super.onCreate ();
        context = getApplicationContext ();
    }

    // [START receive_message]
            //알림 받는거
            @Override
            public void onMessageReceived(RemoteMessage remoteMessage) {

                Toast.makeText (context,"a",Toast.LENGTH_SHORT).show ();
                Intent intent= new Intent (context, LoginActivity.class);
                startActivity(intent);

                //추가한것
                //sendNotification(remoteMessage.getData().get("message"));
                // 여기에 받을 때 이모티콘 변경해보자/
                String title = remoteMessage.getNotification ().getTitle ();
                String body = remoteMessage.getNotification ().getBody ();
                MyNotificationManager.getInstance (getApplicationContext ())
                        .displayNotification (title,body);


                //이부분에서 if문 사용해서 바디가 내 닉네임이랑 같을경우에만 출력
            }
            @Override
            public void onNewToken(String s) {
                super.onNewToken(s);
                Log.e("NEW_TOKEN",s);
            }

            //노티비 보내는거
            private void sendNotification(String messageBody) {
                Intent intent = new Intent(this, ConnectActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* 요청 Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"1")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("fcm수신중")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
            }

        }





