package com.ikota.voiceinputsample;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Sample of starting Recognizer Activity which receive voice input with dialog.
 * This Activity is not used in this project.
 */
public class SampleActivity extends BaseActivity{

    public static final int REQUEST_CODE = 0;

    private String str(int id) {
        return getResources().getString(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.fab).setVisibility(View.GONE);
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


}
