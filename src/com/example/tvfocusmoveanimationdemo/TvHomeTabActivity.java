package com.example.tvfocusmoveanimationdemo;

import com.example.tvfocusmoveanimationdemo.widget.Constant;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class TvHomeTabActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tv_home_tab);
		Constant.initConstant(this);
	}
}
