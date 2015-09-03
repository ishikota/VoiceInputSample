package com.ikota.voiceinputsample;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.squareup.otto.Bus;


public class BaseActivity extends AppCompatActivity{

    public static final Bus sBus = new Bus();
    protected Toolbar mActionBarToolbar;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                mActionBarToolbar.setTitle("");
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }


}
