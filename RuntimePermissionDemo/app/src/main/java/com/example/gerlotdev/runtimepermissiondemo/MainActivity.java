package com.example.gerlotdev.runtimepermissiondemo;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private GoogleApiClient googleApiClient;

	private Button buttonDoYourThing;
	private Button buttonDoWhatever;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		if (googleApiClient == null) {
			googleApiClient = new GoogleApiClient.Builder(this)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
		}

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
				googleApiClient.connect();
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

	@Override
	protected void onDestroy() {
		googleApiClient.disconnect();
		super.onDestroy();
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
				googleApiClient);
		if (lastLocation != null) {
			showMessage("Lat: " + lastLocation.getLatitude() + ", Lon: " + lastLocation.getLongitude());
			googleApiClient.disconnect();
		}

	}

	@Override
	public void onConnectionSuspended(int i) {
		showMessage("Google API Connection Suspended");
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		showMessage("Google API Connection Failed");
	}


	private void showMessage(String message) {
		Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
		snackbar.getView().setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
		snackbar.show();
	}

}
