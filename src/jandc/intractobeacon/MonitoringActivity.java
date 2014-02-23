package jandc.intractobeacon;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;

public class MonitoringActivity extends Activity implements IBeaconConsumer {

	private List<Region> regions = new ArrayList<Region>();
	private List<RegionMessage> messages = new ArrayList<RegionMessage>();
	private Thread fetchThread;
	protected static final String TAG = "MonitoringActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Log.d(TAG, "oncreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring);
		verifyBluetooth();
		iBeaconManager.bind(this);

		fetchThread = new Thread(new Runnable() {
			public void run() {
				try {
					messages = fetchMessages();
					regions = fetchRegions();
					try {
						for (Region region_line : regions) {
							iBeaconManager
									.startMonitoringBeaconsInRegion(region_line);
						}
					} catch (RemoteException e) {
					}
				} catch (Exception e) {
					logToEditText(e.toString(), R.id.monitoringText);
					e.printStackTrace();
				}
			}
		});

		fetchThread.start();

	}

	private List<RegionMessage> fetchMessages() {
		List<RegionMessage> fetchedMessages = new ArrayList<RegionMessage>();

		RegionRepository repo = new RegionRepository(
				this.getApplicationContext());
		String result = repo.getRemoteJSON(
				"http://lachesis.eu/index.php/messages", "message_cache");
		try {
			JSONArray jArray = new JSONArray(result);
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject json_data = jArray.getJSONObject(i);
				RegionMessage message = new RegionMessage(
						json_data.getString("uniqueId"),
						json_data.getString("message"));
				fetchedMessages.add(message);
			}

		} catch (Exception e) {
			logToEditText(e.toString(), R.id.monitoringText);
		}

		return fetchedMessages;
	}

	private List<Region> fetchRegions() throws JSONException {
		List<Region> fetchedRegions = new ArrayList<Region>();
		RegionRepository repo = new RegionRepository(
				this.getApplicationContext());
		String result = repo.getRemoteJSON(
				"http://lachesis.eu/index.php/regions", "region_cache");
		try {
			JSONArray jArray = new JSONArray(result);
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject json_data = jArray.getJSONObject(i);
				Region region = new Region(json_data.getString("uniqueId"),
						json_data.getString("UUID"), json_data.getInt("major"),
						json_data.getInt("minor"));
				fetchedRegions.add(region);
			}

		} catch (Exception e) {
			logToEditText(e.toString(), R.id.monitoringText);
		}
		logToTextView(fetchedRegions.size() + " regions loaded",
				R.id.monitoringTitle);

		return fetchedRegions;
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

	private void logToEditText(final String line, final int textid) {
		runOnUiThread(new Runnable() {
			public void run() {
				EditText editText = (EditText) MonitoringActivity.this
						.findViewById(textid);
				editText.setText(line);
			}
		});
	}

	private void logToTextView(final String line, final int viewid) {
		runOnUiThread(new Runnable() {
			public void run() {
				TextView textView = (TextView) MonitoringActivity.this
						.findViewById(viewid);
				textView.setText(line);
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
				for (RegionMessage message : messages) {
					if (message.getUID().equals(region.getUniqueId())) {
						text = message.getMessage();
					}
				}
				logToEditText(text, R.id.monitoringText);
			}

			@Override
			public void didExitRegion(Region region) {
				logToEditText("", R.id.monitoringText);
			}

			@Override
			public void didDetermineStateForRegion(int state, Region region) {

			}
		});

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
