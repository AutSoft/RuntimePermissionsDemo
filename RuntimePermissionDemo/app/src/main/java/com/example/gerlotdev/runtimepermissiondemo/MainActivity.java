package com.example.gerlotdev.runtimepermissiondemo;

import android.Manifest;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

	public static final String directoryName = "WHATEVER";

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
				MainActivityPermissionsDispatcher.grantAccessCoarseLocationPermissionWithCheck(MainActivity.this);
			}
		});

		buttonDoWhatever = (Button) findViewById(R.id.buttonDoWhatever);
		buttonDoWhatever.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivityPermissionsDispatcher.grantWriteExternalStoragePermissionWithCheck(MainActivity.this);
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
		try {
			Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
			if (lastLocation != null) {
				showMessage(String.format(getResources().getString(R.string.lat_long), lastLocation.getLatitude(), lastLocation.getLongitude()));
				googleApiClient.disconnect();
			} else {
				LocationRequest locationRequest = createLocationRequest();
				LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
				//showMessage(getString(R.string.could_not_get_last_location));
			}
		} catch (SecurityException e) {
			showMessage(getString(R.string.could_not_get_last_location));
		}
	}

	protected LocationRequest createLocationRequest() {
		LocationRequest mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(120000);
		mLocationRequest.setFastestInterval(30000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		return mLocationRequest;
	}


	private void createWhateverDirectory() {
		File file = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS), directoryName);
		if (file.exists()) {
			file.delete();
		}
		if (file.mkdir()) {
			showMessage(String.format(getResources().getString(R.string.directory_created), directoryName));
		} else {
			showMessage(getString(R.string.directory_not_created));
		}
	}

	private void showMessage(String message) {
		Snackbar snackbar = Snackbar.make(fab, message, Snackbar.LENGTH_LONG);
		snackbar.getView().setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
		snackbar.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		showMessage(String.format(getResources().getString(R.string.lat_long), location.getLatitude(), location.getLongitude()));
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}

	private void showRationaleDialog(String message, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
		new AlertDialog.Builder(this)
				.setMessage(message)
				.setPositiveButton("Request", positiveListener)
				.setNegativeButton("Cancel", negativeListener)
				.show();
	}

	@OnShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
	void showRationaleForAccessCoarseLocation(final PermissionRequest request) {
		showRationaleDialog("rationaleACCESS_COARSE_LOCATION message", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				request.proceed();
			}
		}, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				request.cancel();
			}
		});
	}

	@NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
	void grantAccessCoarseLocationPermission() {
		if (googleApiClient.isConnected()) {
			getLastLocation();
		} else {
			googleApiClient.connect();
		}
	}

	@OnPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION)
	void onAccessCoarseLocationPermissionDenied() {
		Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
	}

	@OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION)
	void onAccessCoarseLocationPermissionNeverAsk() {
		Toast.makeText(this, "permission never ask", Toast.LENGTH_SHORT).show();
	}

	@OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	void showRationaleForWriteExternalStorage(final PermissionRequest request) {
		showRationaleDialog("rationaleWRITE_EXTERNAL_STORAGE message", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				request.proceed();
			}
		}, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				request.cancel();
			}
		});
	}

	@NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	void grantWriteExternalStoragePermission() {
		createWhateverDirectory();
	}

	@OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	void onWriteExternalStoragePermissionDenied() {
		Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
	}

	@OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	void onWriteExternalStoragePermissionNeverAsk() {
		Toast.makeText(this, "permission never ask", Toast.LENGTH_SHORT).show();
	}
}
