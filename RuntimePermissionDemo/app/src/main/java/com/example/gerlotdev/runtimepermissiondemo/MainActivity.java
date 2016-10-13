package com.example.gerlotdev.runtimepermissiondemo;

import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	public static final String directoryName = "HATEVER";

	private GoogleApiClient googleApiClient;

	private FloatingActionButton fab;
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

		fab = (FloatingActionButton) findViewById(R.id.fab);
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
				if (googleApiClient.isConnected()) {
					getLastLocation();
				} else {
					googleApiClient.connect();
				}
			}
		});

		buttonDoWhatever = (Button) findViewById(R.id.buttonDoWhatever);
		buttonDoWhatever.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMessage(getString(R.string.do_whatever));
				createWhateverDirectory();
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
		getLastLocation();
	}

	@Override
	public void onConnectionSuspended(int i) {
		showMessage(getString(R.string.google_api_connection));
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		showMessage(getString(R.string.google_api_connection_failed));
	}

	private void getLastLocation() {
		Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
				googleApiClient);
		if (lastLocation != null) {
			showMessage(String.format(getResources().getString(R.string.lat_long), lastLocation.getLatitude(), lastLocation.getLongitude()));
			googleApiClient.disconnect();
		} else {
			showMessage(getString(R.string.could_not_get_last_location));
		}
	}

	private void createWhateverDirectory() {
		File file = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS), directoryName);
		if (!file.mkdirs()) {
			showMessage(getString(R.string.directory_not_created));
		} else {
			showMessage(String.format(getResources().getString(R.string.directory_created), directoryName));
		}
	}

	private void showMessage(String message) {
		Snackbar snackbar = Snackbar.make(fab, message, Snackbar.LENGTH_LONG);
		snackbar.getView().setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
		snackbar.show();
	}

}
