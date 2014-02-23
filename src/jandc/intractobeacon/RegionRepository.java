package jandc.intractobeacon;

import java.io.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;


import android.content.Context;



public class RegionRepository {
	private Context context;
	
	public RegionRepository(Context applicationContext) {
		context = applicationContext;
	}
	public String getRemoteJSON(String uri,String cacheFile) {

		String result = "";

		try {
			HttpParams httpparams = new BasicHttpParams();
			HttpClient httpclient = new DefaultHttpClient(httpparams);

			HttpPost httppost = new HttpPost(uri.toString());
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

			OutputStreamWriter os = new OutputStreamWriter(context.openFileOutput(
					cacheFile,0));

			os.write(result);
			os.close();

		} catch (Exception e1) {
			try {
				InputStream instream = context.openFileInput(cacheFile);

				BufferedReader reader;

				reader = new BufferedReader(new InputStreamReader(instream,
						"iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				instream.close();
				result = sb.toString();

			} catch (Exception e2) {
				result = e2.toString();
			}
		}
		return result;
	}
}
