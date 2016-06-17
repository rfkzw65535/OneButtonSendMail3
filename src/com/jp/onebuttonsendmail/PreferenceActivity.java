package com.jp.onebuttonsendmail;

import android.os.Bundle;


public class PreferenceActivity extends android.preference.PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new PreferenceFragmentUnit())
                            .commit();
    }

}
