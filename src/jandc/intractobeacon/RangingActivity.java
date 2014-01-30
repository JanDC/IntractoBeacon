package jandc.intractobeacon;

import java.util.Collection;

import android.app.*;
import android.os.*;
import android.util.*;
import android.widget.Toast;

import com.radiusnetworks.ibeacon.*;


public class RangingActivity extends Activity implements IBeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
               iBeaconManager.bind(this);
   
    }
    @Override 
    protected void onDestroy() {
        super.onDestroy();
        iBeaconManager.unBind(this);
    }
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setRangeNotifier(new RangeNotifier() {
			       	
        @Override 
        public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
        	Toast.makeText(getApplicationContext(), "Woei?2",Toast.LENGTH_LONG).show();
        	if (iBeacons.size() > 0) {            	
                Log.i(TAG, "The first iBeacon I see is about "+iBeacons.iterator().next().getAccuracy()+" meters away.");       
            }
        }
        });

        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }
}