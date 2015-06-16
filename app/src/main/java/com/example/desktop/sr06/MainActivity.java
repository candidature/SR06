package com.example.desktop.sr06;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class MainActivity extends Activity implements MyCallBacks{
    ToggleButton toggleSpeakButton;
    Button btnPause;
    Button btnExit;
    ToggleButton toggleHearButton;
    public static String TAG = "MainActivity";
    TTSService ttsService;
    MyRecognizerService myRecognizerService;
    public String lastInstructionBeforePause;
    boolean appStopped = false;

    ServiceConnection recogConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Inside recogConn");
            myRecognizerService = ((MyRecognizerService.LocalBinder) service).getService();
            myRecognizerService.startRecognizer();
            //ttsService.createInstance(getApplicationContext());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            unregisterReceiver(recognizerSuccessReceiver);
            unregisterReceiver(TTSCompletionReceiver);
            unregisterReceiver(MainTTSInitReceiver);
            myRecognizerService = null;
        }
    };
    ServiceConnection ttsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ttsService = ((TTSService.LocalBinder) service).getService();
            //ttsService.createInstance(getApplicationContext());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unregisterReceiver(recognizerSuccessReceiver);
            unregisterReceiver(TTSCompletionReceiver);
            unregisterReceiver(MainTTSInitReceiver);
            ttsService = null;
        }
    };


    BroadcastReceiver MainTTSInitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received the TTS_SUCCESSFUL_INITILIZATION broadcast");

            HashMap<String, String> params = new HashMap<String, String>();
            //KEY_PARAM_UTTERANCE_ID is important it helps in setTtsListener callbacks
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "LAST");
            ttsService.textToSpeech.speak("Welcome. You can speak after the beep", TextToSpeech.QUEUE_FLUSH, params);
            return;
        }
    };

    BroadcastReceiver TTSCompletionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000);


            //intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication().getPackageName());

            i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            Log.d(TAG, "Start Recognizing");
            if (myRecognizerService.recognizer == null) {
                Log.d(TAG, "myRecognizerService.recognizer is null");
                return;
            }
            Handler handler = new Handler();
            //hearing start - you can speak now. speaker sign ON


            toggleSpeakButton.setBackgroundResource(R.drawable.spechdisabled);

            handler.postDelayed(new Runnable() {
                public void run() {
                    toggleHearButton.setBackgroundResource(R.drawable.listen);

                    myRecognizerService.recognizer.startListening(i);

                    return;

                }
            }, 1000);

        }
    };

    int recognizerInterval = 3000;
    boolean errorToggle = true;


    public void SearchResult(String result) {
        myTTSHandler(result, recognizerInterval);
    }

    BroadcastReceiver recognizerSuccessReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {


            final String youSpoke = intent.getStringExtra("YOU_SAID");
            int error = intent.getIntExtra("RECOGNIZER_ERROR", 20);
            if (error == 8 || error == 5) {
                //recognizerInterval = recognizerInterval + recognizerInterval;
                Log.d(TAG, "Ignoring error code " + error);
                return;
            }

            if (error == 7 && errorToggle) {
                errorToggle = false;
                Log.d(TAG, "Ignoring error code " + error);

                return;
            }
            errorToggle = true;
            lastInstructionBeforePause = youSpoke;

            toggleHearButton.setBackgroundResource(R.drawable.hearingstop);
            if (youSpoke == null) {
                myTTSHandler(youSpoke, 3000);
                return;
            }
            if (youSpoke.equals("pause")) {
                //Click on Pause button
                btnPause.setEnabled(true);
                return;
            }

            if (youSpoke.equals("stop")) {
                appStopped = true;
                myStop();
                return;
            }
            //toggleSpeakButton.setBackgroundResource(R.drawable.hearing);
            //hearing stop - you can NOT speak now. speaker sign OFF.

            Log.d(TAG, "Calling myTTSHandler - outsource speaking to a method");

            new GoogleTask().execute(youSpoke);


            //hearing end and speaking start
            /*
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    toggleSpeakButton.setBackgroundResource(R.drawable.speakingnow);
                    ttsService.textToSpeech.speak(youSpoke + ", You can speak after the beep", TextToSpeech.QUEUE_FLUSH, params);
                    //speaking now...
                }
            }, 5000);
            */
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter ttsIntentFilter = new IntentFilter();
        ttsIntentFilter.addAction("TTS_SUCCESSFUL_INITILIZATION");
        registerReceiver(MainTTSInitReceiver, ttsIntentFilter);

        IntentFilter ttsSuccessIntentFilter = new IntentFilter();
        ttsSuccessIntentFilter.addAction("TTS_DONE");
        registerReceiver(TTSCompletionReceiver, ttsSuccessIntentFilter);


        IntentFilter recognizerSuccessIntentFilter = new IntentFilter();
        recognizerSuccessIntentFilter.addAction("RECOGNIZER_SUCCESS");
        registerReceiver(recognizerSuccessReceiver, recognizerSuccessIntentFilter);

        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, TTSService.class);
        bindService(intent, ttsServiceConnection, BIND_AUTO_CREATE);

        Intent recogIntent = new Intent(this, MyRecognizerService.class);
        bindService(recogIntent, recogConn, BIND_AUTO_CREATE);
        Log.d(TAG, "Inside onCreate");


        toggleSpeakButton = (ToggleButton) findViewById(R.id.toggleSpeaking);
        toggleHearButton = (ToggleButton) findViewById(R.id.toggleHearing);
        btnPause = (Button) findViewById(R.id.btnPause);
        btnExit = (Button) findViewById(R.id.btnExit);
        btnExit.setEnabled(true);
    }

    boolean toggleSpeak = true;

    public void toggleSpeakOnClick(View view) {
        toggleSpeak = ((ToggleButton) view).isChecked();
        if (toggleSpeak) {
            //can speak
        } else {
            //can not speak
        }
    }


    public void onClickbtnResume(View view) {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "LAST");
        toggleSpeakButton.setBackgroundResource(R.drawable.speakingnow);
        ttsService.textToSpeech.speak(lastInstructionBeforePause + ", You can speak after the beep", TextToSpeech.QUEUE_FLUSH, params);
        btnPause.setEnabled(false);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ttsService != null) {
            //unbindService(ttsServiceConnection);
        }

        if (myRecognizerService != null) {
            // unbindService(recogConn);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "System onStop is called");
        appStopped = true;
    }

    protected void myStop() {
        onStop();
        btnExit.setEnabled(false);

        Log.d(TAG, "Custom onStop was called");

        //myRecognizerService.recognizer.cancel();
        //myRecognizerService.recognizer.stopListening();
        //ttsService.textToSpeech.stop();

        unbindService(ttsServiceConnection);
        unbindService(recogConn);

    }

    protected void myTTSHandler(final String youSpoke, final int duration) {
        Handler handler = new Handler();
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "LAST");

        handler.postDelayed(new Runnable() {
            public void run() {
                toggleSpeakButton.setBackgroundResource(R.drawable.speakingnow);
                ttsService.textToSpeech.speak(youSpoke + ", You can speak after the beep", TextToSpeech.QUEUE_FLUSH, params);
                //speaking now...
            }
        }, duration);


    }


    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private class GoogleTask extends AsyncTask<String,Integer,String> {

        public String TAG = "GoogleTask";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            SearchResult(s);
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }


        protected String googleSearch(String query) {
            String address = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
            String charset = "UTF-8";
            String title = null;
            String searchedUrl = null;
            String totalResult = null;

            URL url = null;
            try {
                url = new URL(address + URLEncoder.encode(query, charset));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            /*
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(
                        url.openStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String str;

            try {
                while ((str = in.readLine()) != null) {
                    //System.out.println(str);
                    Log.d(TAG, str);
                    totalResult = totalResult + str;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
            try {
                Reader reader = new InputStreamReader(url.openStream(), charset);
                GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);
                searchedUrl = results.getResponseData().getResults().get(0).getUrl();
                title = results.getResponseData().getResults().get(0).getTitle();
                URL searchedUrlObj = new URL(searchedUrl);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                BufferedReader resultReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String oneLine;
                while ((oneLine = resultReader.readLine()) != null) {
                    totalResult = totalResult + oneLine;
                }



            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG,totalResult);
            return totalResult;
        }

        @Override
        protected String doInBackground(String... params) {

            return googleSearch(params[0]);

        }
    }
}



