package com.ikota.voiceinputsample;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MyListActivity extends BaseActivity {

    private static final String TAG = "SR lifecycle";  // SpeechRecognizer lifecycle

    SpeechRecognizer mSpeechRecognizer;
    public static final int REQUEST_CODE = 0;

    private String str(int id) {
        return getResources().getString(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if retained fragment exists
        FragmentManager fm = getSupportFragmentManager();
        MyListFragment fragment = (MyListFragment) fm.findFragmentByTag(MyListFragment.class.getSimpleName());
        if (fragment == null) {
            fragment = new MyListFragment();
            String tag = MyListFragment.class.getSimpleName();
            fm.beginTransaction().add(R.id.container, fragment, tag).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        killRecognizer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_with_dialog) {
            startRecognizerActivity();
        } else if(id == R.id.action_without_dialog) {
            startSpeechRecognizer();
        } else if(id == R.id.action_cancel) {
            killRecognizer();
        }

        return super.onOptionsItemSelected(item);
    }


    /** Display speech dialog and starts to listening. */
    private void startRecognizerActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // set text which is displayed on dialog
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, str(R.string.listening));
        // set language to recognize
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // the case when the target device doesn't supported speech recognition
            Toast.makeText(this, str(R.string.not_available), Toast.LENGTH_LONG).show();
        }
    }

    /** start speech recognition without dialog */
    private void startSpeechRecognizer() {

        // force old recognizer instance to finish
        if(mSpeechRecognizer!=null) {
            Log.i(TAG, "destroy old recognizer");
            mSpeechRecognizer.destroy();  // this line prevents error when TIMEOUT occurred
        }

        // setup recognizer with custom listener
        Log.i(TAG, "create new recognizer");
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new MyRecognitionListener());

        // create intent to start speech recognition
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());

        // set language
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString());
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toString());

        // pass intent to recognizer and start recognition
        Log.i(TAG, "starts listening");
        mSpeechRecognizer.startListening(intent);
    }

    private void killRecognizer() {
        if(mSpeechRecognizer != null) {
            Log.i(TAG, "destroy old recognizer");
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
            Toast.makeText(this, str(R.string.finish_listen), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String s = results.get(0);
            Toast.makeText(this, String.format("You said '%s'", s), Toast.LENGTH_LONG).show();
            Log.i("MyListActivity", "Recognition result is : " + s);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    /** This listener re-start SpeechRecognizer When TIMEOUT or NO_MATCH occurred. */
    class MyRecognitionListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.i(TAG, "onReadyForSpeech called");
            Toast.makeText(getApplicationContext(), str(R.string.start_listen), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> candidates = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            assert candidates != null;
            String voice_in = candidates.get(0);
            Log.i(TAG, "onResults called wit result : " + voice_in);
            if(voice_in.equals("finish")) {
                killRecognizer();
            } else {
                BaseActivity.sBus.post(new MyListFragment.VoiceEvent(voice_in));
                // start next recognition again
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("speech", "start recognizer again");
                        startSpeechRecognizer();
                    }
                }, 1000);
            }

        }

        @Override
        public void onError(int error) {
            Log.i(TAG, "onError called with ： " + error);
            if(error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                Log.i(TAG, "TimeOut, starts recognizer again.");
                startSpeechRecognizer();
            } else if(error == SpeechRecognizer.ERROR_NO_MATCH) {
                Log.i(TAG, "Ambiguous input, starts recognizer again.");
                Toast.makeText(MyListActivity.this, "Sorry, Try speaking again.", Toast.LENGTH_SHORT).show();
                startSpeechRecognizer();
            }
        }

        @Override
        public void onBeginningOfSpeech() {
            // Called when user starts to speak
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.i(TAG, "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            // Called when user has finished to speak
        }

        @Override public void onEvent(int eventType, Bundle params) {
            Log.i(TAG, "onEvent called with eventType " + eventType);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.i(TAG, "onPartialResults called");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // Log.i(TAG, "onRmsChanged called with rmsdB "+rmsdB);  // called frequently
        }

    }


}
