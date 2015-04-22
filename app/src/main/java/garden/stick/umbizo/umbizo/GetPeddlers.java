package garden.stick.umbizo.umbizo;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
/**
 * Created by IMukunya on 4/20/2015.
 */

public class GetPeddlers extends AsyncTask<String,Void,String>{

    private String serverURL="http://10.0.2.2/Umbizo/fetchPeds";
    HttpClient httpclient;
    InputStream inputStream = null;
    String result;

    // make GET request to the given URL
    HttpResponse httpResponse;

    @Override
    protected String doInBackground(String... coords) {
        String jsonResult=null;
        try {
            jsonResult= fetchPeds(coords[0]);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonResult;
    }

    protected void onPostExecute(String jsonResult){
        Log.i("JSON output:",jsonResult);
    }


    public String fetchPeds(String coords) throws IOException, JSONException {
        JSONObject jsonResult = new JSONObject();
        httpclient = new DefaultHttpClient();
        try {
            // make GET request to the given URL
            httpResponse = httpclient.execute(new HttpGet(serverURL));
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);

        } catch (IOException IOe) {
            Log.i("IOException", IOe.getMessage());
        }

        return result;
        /*jsonResult.getJSONObject(result);

       return jsonResult;*/

    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

}
