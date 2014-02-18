package jandc.intractobeacon;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;

import com.radiusnetworks.ibeacon.*;

import android.R.string;
import android.os.*;
import android.app.*;
import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import java.sql.*;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

		new Thread(new Runnable() {
			public void run() {
				regions = fetchRegions();
			}
		}).start();

		// regions.add(new
		// Region("intracto.entrance","e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0",
		// null, null));
		// regions.add(new Region("intracto.thebox",
		// "e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e1", null, null));

	}

	private List<Region> fetchRegions() {
		List<Region> fetchedRegions = new ArrayList<Region>();

		String result = "";

		// http post
		try {
			HttpParams httpparams = new BasicHttpParams();
			HttpClient httpclient = new DefaultHttpClient(httpparams);

			HttpPost httppost = new HttpPost("http://192.168.1.100/index.php");
			HttpResponse response = httpclient.execute(httppost);

			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();

			// convert response to string
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();
			OutputStreamWriter os = new OutputStreamWriter(openFileOutput(
					"cached_json", 0));

			os.write(result);
			os.close();

		} catch (Exception e) {
			try {
				logToDisplay(e.toString());
				InputStream instream = openFileInput("cached_json");

				BufferedReader reader;

				reader = new BufferedReader(new InputStreamReader(instream,
						"iso-8859-1"), 8);
				result = reader.readLine();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.toString();
			}
		}
		try {
			// parse json data
			JSONArray jArray = new JSONArray(result);
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject json_data = jArray.getJSONObject(i);

				fetchedRegions.add(new Region(json_data.getString("uniqueId"),
						json_data.getString("UUID"), json_data.getInt("major"),
						json_data.getInt("minor")));
			}
		} catch (Exception e) {

		}

		// Toast.makeText(getApplicationContext(),"There are " +
		// fetchedRegions.size() + " of them",Toast.LENGTH_LONG).show();
		return fetchedRegions;
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
