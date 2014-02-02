package jandc.intractobeacon;

import java.lang.reflect.Constructor;
import java.util.*;

import com.radiusnetworks.ibeacon.*;

import android.R.string;
import android.os.*;
import android.app.*;
import android.content.*;
import android.util.Log;
import android.view.View;
import android.widget.*;

public class MonitoringActivity extends Activity implements IBeaconConsumer {

	private List<Region> regions = new ArrayList<Region>();

	protected static final String TAG = "MonitoringActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Log.d(TAG, "oncreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring);
		verifyBluetooth();
		iBeaconManager.bind(this);

		regions.add(new Region("intracto.entrance",
				"e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0", null, null));
		regions.add(new Region("intracto.thebox",
				"e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e1", null, null));

	}

	public void onRangingClicked(View view) {
		Intent myIntent = new Intent(this, RangingActivity.class);
		this.startActivity(myIntent);
	}

	private void verifyBluetooth() {

		try {
			if (!IBeaconManager.getInstanceForApplication(this)
					.checkAvailability()) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(
						this);
				builder.setTitle("Bluetooth not enabled");
				builder.setMessage("Please enable bluetooth in settings and restart this application.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						finish();
						System.exit(0);
					}
				});
				builder.show();
			}
		} catch (RuntimeException e) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE not available");
			builder.setMessage("Sorry, this device does not support Bluetooth LE.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
					System.exit(0);
				}

			});
			builder.show();

		}

	}

	private IBeaconManager iBeaconManager = IBeaconManager
			.getInstanceForApplication(this);

	@Override
	protected void onDestroy() {
		super.onDestroy();
		iBeaconManager.unBind(this);
	}

	public void onBackgroundClicked(View view) {
		Intent myIntent = new Intent(this, BackgroundActivity.class);
		this.startActivity(myIntent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (iBeaconManager.isBound(this))
			iBeaconManager.setBackgroundMode(this, true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (iBeaconManager.isBound(this))
			iBeaconManager.setBackgroundMode(this, false);
	}

	private void logToDisplay(final String line) {
		runOnUiThread(new Runnable() {
			public void run() {
				EditText editText = (EditText) MonitoringActivity.this
						.findViewById(R.id.monitoringText);
				editText.setText(line);
			}
		});
	}

	@Override
	public void onIBeaconServiceConnect() {

		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {

			
			@Override
			public void didEnterRegion(Region region) {
				String text = "Just entered :";
				text += region.getUniqueId();

				if (region.getUniqueId()
						.equals(new String("intracto.entrance"))) {
					text = " Welcome to intracto digital agency.";
				} else if (region.getUniqueId().equals(
						new String("intracto.thebox"))) {
					text = " Welcome to team 'The Box'.";
				}

				logToDisplay(text);
			}

			@Override
			public void didExitRegion(Region region) {
				logToDisplay("");
			}

			@Override
			public void didDetermineStateForRegion(int state, Region region) {

			}
		});

		/*
		 * iBeaconManager.setRangeNotifier(new RangeNotifier() {
		 * 
		 * @Override public void didRangeBeaconsInRegion(Collection<IBeacon>
		 * iBeacons, Region region) { ArrayList<Accuracies> accuracies = new
		 * ArrayList<MonitoringActivity.Accuracies>(); for (IBeacon iBeacon :
		 * iBeacons) { Accuracies acc = new Accuracies(); acc.accuracy =
		 * iBeacon.getAccuracy(); acc.uuid = iBeacon.getProximityUuid();
		 * accuracies.add(acc); } Collections.sort(accuracies); String text= "";
		 * for (Region region_line : regions) { if
		 * (region_line.getProximityUuid().equals(accuracies.get(0).uuid)) { if
		 * (region_line.getUniqueId().equals( new String("intracto.entrance")))
		 * { text =
		 * " Welcome to intracto digital agency. There is free wifi! Login with essid 'ITR-Guest' and password 'intractointernet'"
		 * ; } else if (region_line.getUniqueId().equals( new
		 * String("intracto.thebox"))) { text =
		 * " Welcome to team 'The Box'. This is where I was born"; } } }
		 * logToDisplay(text);
		 * 
		 * } });
		 */
		try {
			for (Region region_line : regions) {
				iBeaconManager.startMonitoringBeaconsInRegion(region_line);
			}
		} catch (RemoteException e) {
		}
	}

	class Accuracies implements Comparable<Accuracies> {
		double accuracy;
		String uuid;

		// sort, lower accuracy = closer beacon
		@Override
		public int compareTo(Accuracies another) {
			if (another.accuracy > this.accuracy) {
				return 1;
			}
			return -1;

		}
	}
}
