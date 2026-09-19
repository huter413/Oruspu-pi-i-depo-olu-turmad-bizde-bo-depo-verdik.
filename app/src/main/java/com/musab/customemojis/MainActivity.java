package com.musab.customemojis;
import android.app.*;import android.os.*;import android.content.*;import android.view.*;import android.view.inputmethod.InputMethodManager;import android.widget.*;
public class MainActivity extends Activity { public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main); findViewById(R.id.change).setOnClickListener(v->{((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();});}}
