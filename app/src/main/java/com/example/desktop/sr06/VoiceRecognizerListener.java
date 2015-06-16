package com.example.desktop.sr06;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Desktop on 6/14/2015.
 */
public class VoiceRecognizerListener implements RecognitionListener {

    public static String TAG = "VoiceRecognizerListener";

    MyRecognizerService recognizerService;

    public VoiceRecognizerListener(MyRecognizerService recognizerService) {
        Log.d(TAG, "VoiceRecognizerListener");
        this.recognizerService = recognizerService;
    }
    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {
        Log.d(TAG,"onError " + error);

        Intent i = new Intent();
        i.setAction("RECOGNIZER_SUCCESS");
        i.putExtra("RECOGNIZER_ERROR",error);
        recognizerService.sendBroadcast(i);
    }

    @Override
    public void onResults(Bundle results) {
        Log.d(TAG,"inside onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d(TAG, " You said " + matches.get(0));

        Intent i = new Intent();
        i.setAction("RECOGNIZER_SUCCESS");
        i.putExtra("YOU_SAID",matches.get(0));

        recognizerService.sendBroadcast(i);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

}
