package com.example.gerlotdev.runtimepermissiondemo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

	public static final String directoryName = "WHATEVER";
	public static final String KEY_NEVER_ASK_ACCESS_COARSE_LOCATION = "ACCESS_COARSE_LOCATION";
	public static final String KEY_NEVER_ASK_WRITE_EXTERNAL_STORAGE = "WRITE_EXTERNAL_STORAGE";

	private GoogleApiClient googleApiClient;

	private FloatingActionButton fab;
	private Button buttonDoYourThing;
	private Button buttonDoWhatever;

	private boolean showPrimerDialogForYourThing = true;
	private boolean showPrimerDialogForWhatever = true;

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
				if (PermissionUtils.hasSelfPermissions(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
					grantAccessCoarseLocationPermission();
				} else {
					boolean shouldShowRationale = PermissionUtils.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
					boolean neverAsk = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(KEY_NEVER_ASK_ACCESS_COARSE_LOCATION, false);
					if (shouldShowRationale || neverAsk || !showPrimerDialogForYourThing) {
						MainActivityPermissionsDispatcher.grantAccessCoarseLocationPermissionWithCheck(MainActivity.this);
					} else {
						new AlertDialog.Builder(MainActivity.this)
								.setMessage(R.string.need_location_permission)
								.setPositiveButton(R.string.action_request, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										MainActivityPermissionsDispatcher.grantAccessCoarseLocationPermissionWithCheck(MainActivity.this);
									}
								})
								.setNegativeButton(R.string.action_cancel, null)
								.show();
					}
				}
			}
		});

		buttonDoWhatever = (Button) findViewById(R.id.buttonDoWhatever);
		buttonDoWhatever.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (PermissionUtils.hasSelfPermissions(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					grantWriteExternalStoragePermission();
				} else {
					boolean shouldShowRationale = PermissionUtils.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
					boolean neverAsk = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(KEY_NEVER_ASK_WRITE_EXTERNAL_STORAGE, false);
					if (shouldShowRationale || neverAsk || !showPrimerDialogForWhatever) {
						MainActivityPermissionsDispatcher.grantWriteExternalStoragePermissionWithCheck(MainActivity.this);
					} else {
						new AlertDialog.Builder(MainActivity.this)
								.setMessage(R.string.need_files_permission)
								.setPositiveButton(R.string.action_request, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										MainActivityPermissionsDispatcher.grantWriteExternalStoragePermissionWithCheck(MainActivity.this);
									}
								})
								.setNegativeButton(R.string.action_cancel, null)
								.show();
					}
				}
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
				.setPositiveButton(R.string.action_request, positiveListener)
				.setNegativeButton(R.string.action_cancel, negativeListener)
				.show();
	}

	@OnShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
	void showRationaleForAccessCoarseLocation(final PermissionRequest request) {
		new AlertDialog.Builder(MainActivity.this)
				.setMessage(R.string.need_location_permission)
				.setPositiveButton(R.string.action_request, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						request.proceed();
					}
				})
				.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						request.cancel();
					}
				})
				.show();
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
		showMessage(getString(R.string.permission_denied));

		new AlertDialog.Builder(this)
				.setMessage(R.string.need_to_grant_location_permission)
				.setPositiveButton(R.string.action_go_to_app_settings, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startInstalledAppDetailsActivity();
					}
				})
				.setNegativeButton(R.string.action_cancel, null)
				.show();
	}

	@OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION)
	void onAccessCoarseLocationPermissionNeverAsk() {
		showMessage(getString(R.string.permission_never_ask));

		new AlertDialog.Builder(this)
				.setMessage(R.string.need_to_grant_location_permission)
				.setPositiveButton(R.string.action_go_to_app_settings, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startInstalledAppDetailsActivity();
					}
				})
				.setNegativeButton(R.string.action_cancel, null)
				.show();

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
		editor.putBoolean(KEY_NEVER_ASK_ACCESS_COARSE_LOCATION, true);
		editor.apply();
	}

	@OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	void showRationaleForWriteExternalStorage(final PermissionRequest request) {
		new AlertDialog.Builder(MainActivity.this)
				.setMessage(R.string.need_files_permission)
				.setPositiveButton(R.string.action_request, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						request.proceed();
					}
				})
				.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						request.cancel();
					}
				})
				.show();
	}

	@NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	void grantWriteExternalStoragePermission() {
		createWhateverDirectory();
	}

	@OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	void onWriteExternalStoragePermissionDenied() {
		showMessage(getString(R.string.permission_denied));
	}

	@OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	void onWriteExternalStoragePermissionNeverAsk() {
		showMessage(getString(R.string.permission_never_ask));

		buttonDoWhatever.setEnabled(false);

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
		editor.putBoolean(KEY_NEVER_ASK_WRITE_EXTERNAL_STORAGE, true);
		editor.apply();
	}

	private void startInstalledAppDetailsActivity() {
		final Intent intent = new Intent();
		intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setData(Uri.parse("package:" + getPackageName()));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivity(intent);
	}

}
