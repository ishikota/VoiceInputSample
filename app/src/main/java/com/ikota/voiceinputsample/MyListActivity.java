package com.ikota.voiceinputsample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyListActivity extends BaseActivity {

    private static final String TAG = "SR lifecycle";  // SpeechRecognizer lifecycle

    // Voice constant
    private static final String HELLO = "Hello";
    private static final String BYE = "Bye";

    private CoordinatorLayout mCoordinatorLayout;
    private FloatingActionButton mFAB;

    private SpeechRecognizer mSpeechRecognizer;
    private TextToSpeech mTTS;
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

        // setup TTS
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (TextToSpeech.SUCCESS == status) {
                    Locale locale = Locale.ENGLISH;
                    if (mTTS.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                        Log.i("TTS", "Success onInit()");
                        mTTS.setLanguage(locale);
                    } else {
                        Log.d("TTS", "Error SetLocale");
                    }
                } else {
                    Log.d("TTS", "Error Init");
                }
            }
        });

        mCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.parent);
        mFAB = (FloatingActionButton)findViewById(R.id.fab);
        mFAB.setTag(false);  // if listening now
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean is_listening = (boolean)view.getTag();
                if(!is_listening) {
                    speechText(new SpeechEvent(HELLO));
                    startSpeechRecognizer();
                } else {
                    speechText(new SpeechEvent(BYE));
                    killRecognizer();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        BaseActivity.sBus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        killRecognizer();
        BaseActivity.sBus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mTTS!=null) mTTS.shutdown();
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
            speechText(new SpeechEvent(HELLO));
            startSpeechRecognizer();
        } else if(id == R.id.action_cancel) {
            killRecognizer();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("deprecation")
    @Subscribe
    public void speechText(SpeechEvent ev) {
        String message = ev.message;
        if (0 < message.length()) {
            if (mTTS.isSpeaking()) {
                mTTS.stop();
                Log.i("TTS", "speaking is interrupted.");
            }
            Log.i("TTS", "message : "+message);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null, message);
            } else {
                mTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    public static class SpeechEvent {
        public final String message;
        public SpeechEvent(String message) {
            this.message = message;
        }
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

    private void startSpeechRecognizerWithDelay(long mills) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startSpeechRecognizer();
            }
        }, mills);
    }

    /** start speech recognition without dialog */
    private void startSpeechRecognizer() {

        if(mTTS.isSpeaking()) {
            startSpeechRecognizerWithDelay(1000);
            changeFABState(false);
            mFAB.setClickable(false);
            return;
        }

        mFAB.setClickable(true);
        changeFABState(true);

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
        changeFABState(false);
        if(mSpeechRecognizer != null) {
            Log.i(TAG, "destroy old recognizer");
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
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
    private final class MyRecognitionListener implements RecognitionListener {

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
                speechText(new SpeechEvent(BYE));
                killRecognizer();
            } else {
                BaseActivity.sBus.post(new MyListFragment.VoiceEvent(voice_in));
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
            boundAnim(mFAB);
            mFAB.setImageResource(R.drawable.ic_sms_white_24dp);
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.i(TAG, "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            // Called when user has finished to speak
            boundAnim(mFAB);
            mFAB.setImageResource(R.drawable.ic_mic_white_24dp);
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


    @Subscribe
    public void ifCommandAccepted(CAEvent ev) {
        if(ev.accepted) {
            startSpeechRecognizerWithDelay(1000);
        } else {
            Snackbar.make(mCoordinatorLayout, "Try Again", Snackbar.LENGTH_SHORT).show();
            noMatchAnim(mFAB);
        }
    }

    public static class CAEvent {
        public final boolean accepted;
        public CAEvent(boolean accepted) {
            this.accepted = accepted;
        }
    }

    private void changeFABState(boolean to_listening) {
        boolean is_listening = (boolean)mFAB.getTag();  // get FAB current state
        if(is_listening == to_listening) return;  // already FAB is desired state
        if(to_listening) {
            mFAB.setImageResource(R.drawable.ic_mic_white_24dp);
        } else {
            mFAB.setImageResource(R.drawable.ic_mic_off_white_24dp);
        }
        mFAB.setTag(!is_listening);
        boundAnim(mFAB);
    }

    private void boundAnim(View target) {
        List<Animator> list = new ArrayList<>();
        list.add(createScaleAnim(40, target, 1.0f, 0.7f));
        list.add(createScaleAnim(40, target, 0.7f, 1.2f));
        list.add(createScaleAnim(40, target, 1.2f, 1.0f));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(list);
        animatorSet.start();
    }

    private void noMatchAnim(final ImageView target) {
        List<Animator> list = new ArrayList<>();
        list.add(createScaleAnim(50, target, 1.0f, 0.7f));
        list.add(createScaleAnim(50, target, 0.7f, 1.0f));
        list.add(createScaleAnim(50, target, 1.0f, 0.7f));
        list.add(createScaleAnim(50, target, 0.7f, 1.0f));
        list.add(createScaleAnim(50, target, 1.0f, 0.7f));
        list.add(createScaleAnim(50, target, 0.7f, 1.0f));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(list);
        animatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                target.setImageResource(R.drawable.ic_warning_white_24dp);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                target.setImageResource(R.drawable.ic_mic_white_24dp);
                startSpeechRecognizerWithDelay(100);
            }
        });
        animatorSet.start();
    }

    private Animator createScaleAnim(int duration, View target, float from, float to) {
        PropertyValuesHolder pvhsx = PropertyValuesHolder.ofFloat(View.SCALE_X, from, to);
        PropertyValuesHolder pvhsy = PropertyValuesHolder.ofFloat(View.SCALE_Y, from, to);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(target, pvhsx, pvhsy);
        anim.setDuration(duration);
        return anim;
    }


}
