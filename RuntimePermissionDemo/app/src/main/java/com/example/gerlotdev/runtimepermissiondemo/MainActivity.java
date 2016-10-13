package com.example.gerlotdev.runtimepermissiondemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

	private Button buttonDoYourThing;
	private Button buttonDoWhatever;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.dangerous_permissions)
						.setItems(R.array.dangerous_permissions, null)
						.show();
			}
		});

		buttonDoYourThing = (Button) findViewById(R.id.buttonDoYourThing);
		buttonDoYourThing.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMessage(getString(R.string.do_your_thing));
			}
		});

		buttonDoWhatever = (Button) findViewById(R.id.buttonDoWhatever);
		buttonDoWhatever.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMessage(getString(R.string.do_whatever));
			}
		});

	}

	private void showMessage(String message) {
		Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
		snackbar.getView().setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
		snackbar.show();
	}

}
