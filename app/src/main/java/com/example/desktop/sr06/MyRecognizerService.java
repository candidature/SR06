package com.example.desktop.sr06;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.speech.SpeechRecognizer;
import android.util.Log;

/**
 * Created by Desktop on 6/14/2015.
 */
public class MyRecognizerService extends Service {
    SpeechRecognizer recognizer;
    final static String TAG = "MyRecognizerService";

    private LocalBinder localBinder = new LocalBinder();




    public class LocalBinder extends Binder {
        public MyRecognizerService getService() {
            Log.d(TAG,"Inside getService of LocalBinder ");
            return MyRecognizerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "inside onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Inside onBind");

        return localBinder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG,"On unbind");
        return super.onUnbind(intent);
    }

    /*
    receiving any broadcast which is invoked after TTS initilized

     */



    public void startRecognizer() {
        if(recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(getBaseContext());
            recognizer.setRecognitionListener(new VoiceRecognizerListener(this));
            Log.d(TAG, "Inside startRecognizer");
        } else {
            Log.d(TAG,"recognizer was not null so not created again");
        }



    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Inside onDestroy");

    }

}
