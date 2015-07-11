package garden.stick.umbizo.umbizo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class Garden extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private static final String TAG = "LocationServices";

    private static final String KEY_IN_RESOLUTION = "is_in_resolution";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;
    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;


    public static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    public static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    private Location mLastLocation;
    public ProgressBar mActivityIndicator;
    private TextView locationView;
    private Button btnCreateAccount;

    ArrayList<HashMap<String, String>> MY_PEDS_LIST=new ArrayList<HashMap<String, String>>();




    /**
     * Called when the activity is starting. Restores the activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_garden);
        if (savedInstanceState != null) {
            mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_launcher);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //connect to the google api client.
        GservicesConnection();

        //mLocationClient = new LocationClient(this, this, this);

        btnCreateAccount=(Button) findViewById(R.id.btncreateAccount);

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createAccount=new Intent(getApplicationContext(),CreatePedaccountActivity.class);

                startActivity(createAccount);
            }
        });


    }

    private synchronized void GservicesConnection(){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mGoogleApiClient.connect();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_garden, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_settings:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT)
                        .show();
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * Called when the Activity is made visible.
     * A connection to Play Services need to be initiated as
     * soon as the activity is visible. Registers {@code ConnectionCallbacks}
     * and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onStart() {
        super.onStart();
        GservicesConnection();

    }
    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
    }

    /**
     * Handles Google Play Services resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                retryConnecting();
                break;
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
           GservicesConnection();
            pullLocation();
        }else{
            pullLocation();
        }
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
        // TODO: Start making API requests.
        mActivityIndicator=(ProgressBar) findViewById(R.id.addressProgress);
        mActivityIndicator.setVisibility(View.VISIBLE);
        pullLocation();

    }

    @Override
    public void onDisconnected() {
        retryConnecting();

    }
    /**
     * Called when {@code mGoogleApiClient} connection is suspended.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
        retryConnecting();
    }
    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), this, 0, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            retryConnecting();
                        }
                    }).show();
            return;
        }
        // If there is an existing resolution error being displayed or a resolution
        // activity has started before, do nothing and wait for resolution
        // progress to be completed.
        if (mIsInResolution) {
            return;
        }
        mIsInResolution = true;
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            retryConnecting();
        }
    }
    //pull users location from the Google Services API Client
    public void pullLocation(){
        LocationManager service=(LocationManager) getSystemService(LOCATION_SERVICE);

        if(isLocationEnabled(getApplicationContext())){
            if(servicesConnected()) {
                Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, new com.google.android.gms.location.LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                setLocation(location);
                            }
                        });
                String coords=getLatLng(getApplicationContext(),currentLocation);

                new GetPeddlers().execute(coords);


            }else{
                Log.i("Play Services","not available");
            }
        }else{
            GPSSettingsDialog();
        }




    }


    /*Display to the user their current human readable location.*/
    public void setLocation(Location myLocation){
        new GetAddressTask(this).execute(myLocation);
    }

    public static String getLatLng(Context context, Location currentLocation) {
        // If the location is valid
        if (currentLocation != null) {
            // Return the latitude and longitude as strings
            return context.getString(R.string.latitude_longitude,currentLocation.getLatitude(),currentLocation.getLongitude());
        } else {
            // Otherwise, return the empty string
            return "";
        }
    }
    public String getUserAddress(Address address) {
        String userAddress =getApplicationContext().getString(R.string.address_output_string,
                // If there's a street address, add it
                address.getMaxAddressLineIndex() > 0 ?
                        address.getAddressLine(0) : "",

                // Locality is usually a city
                address.getLocality(),

                // The country of the address
                address.getCountryName());
        return userAddress;
    }

    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Play Services", "Play Services available");

            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Log.d("Play Services", "Play Services  NOT available");

            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) { }

    @Override
    public void onProviderDisabled(String s) {

    }
    public void GPSSettingsDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Click Okay to enable Location Services on your device")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
        builder.create().show();
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }



    private class GetAddressTask extends AsyncTask<Location, Void, String> {

        // Store the context passed to the AsyncTask when the system instantiates it.
        Context localContext;


        // Constructor called by the system to instantiate the task
        public GetAddressTask(Context context) {

            // Required by the semantics of AsyncTask
            super();

            // Set a Context for the background task
            localContext = context;
        }

        /**
         * Get a geocoding service instance, pass latitude and longitude to it, format the returned
         * address, and return the address to the UI thread.
         */
        @Override
        protected String doInBackground(Location... params) {
            /*
             * Get a new geocoding service instance, set for localized addresses. This example uses
             * android.location.Geocoder, but other geocoders that conform to address standards
             * can also be used.
             */

            Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

            // Get the current location from the input parameter list
            Location location = params[0];

            // Create a list to contain the result address
            List<Address> addresses = null;

            // Try to get an address for the current location. Catch IO or network problems.
            try {

                /*
                 * Call the synchronous getFromLocation() method with the latitude and
                 * longitude of the current location. Return at most 1 address.
                 */
                if(geocoder!=null){
                    addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(), 1);
                }else{
                    retryConnecting();
                    pullLocation();
                }


                // Catch network or other I/O problems.
            } catch (IOException exception1) {

                // Log an error and return an error message
                Log.e("GeoCode Error", localContext.getString(R.string.IO_Exception_getFromLocation));

                // print the stack trace
                exception1.printStackTrace();

                // Return an error message
                return (localContext.getString(R.string.IO_Exception_getFromLocation));

                // Catch incorrect latitude or longitude values
            } catch (IllegalArgumentException exception2) {

                // Construct a message containing the invalid arguments
                String errorString = localContext.getString(
                        R.string.illegal_argument_exception,
                        location.getLatitude(),
                        location.getLongitude()
                );
                // Log the error and print the stack trace
                Log.e("Illegal Argument", errorString);
                exception2.printStackTrace();
                //
                return errorString;
            } catch(RuntimeException KimbiaKataa){
                Log.i("Run Time Error",KimbiaKataa.getMessage());
                KimbiaKataa.printStackTrace();
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {

                // Get the first address
                Address address = addresses.get(0);

                // Format the first line of address
                String addressText = getUserAddress(address);
                // Return the text
                return addressText;
                // If there aren't any addresses, post a message
            } else {
                return localContext.getString(R.string.no_address_found);
            }
        }

        /**
         * A method that's called once doInBackground() completes. Set the text of the
         * UI element that displays the address. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(String address) {
            // Turn off the progress bar
            mActivityIndicator=(ProgressBar) findViewById(R.id.addressProgress);
            mActivityIndicator.setVisibility(View.GONE);
            // Set the address in the UI
            locationView=(TextView) findViewById(R.id.myLocation);
            locationView.setText(address);

        }

        private String getUserAddress(Address address) {
            String userAddress =localContext.getString(R.string.address_output_string,
                    // If there's a street address, add it
                    address.getMaxAddressLineIndex() > 0 ?
                            address.getAddressLine(0) : "",
                    // Locality is usually a city
                    address.getSubLocality(),
                    // The country of the address
                    address.getLocality());
            return userAddress;
        }
    }


    /*get peddlers class*/

    private class GetPeddlers extends AsyncTask<String, Void, JSONObject> {

        private String serverURL="http://196.201.216.14/umb/data.json";
        HttpClient httpclient;
        InputStream inputStream = null;
        String result;
         InputStream is = null;
         JSONObject jObj = null;
         String json = "";
         String PED_NAME="pedName";
         String LOCATION="location";
         String COORDS="coords";
         String PHONE_NUMBER="phoneNumber";

        JSONArray peddlers;
        ArrayList<HashMap<String, String>> MY_PEDS_LIST=new ArrayList<HashMap<String, String>>();


        // make GET request to the given URL
        HttpResponse httpResponse;

        @Override
        protected JSONObject doInBackground(String... coords) {
            JSONObject jsonResult=null;
            try {
                jsonResult= fetchPeds(coords[0]);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsonResult;

        }

        protected void onPostExecute(JSONObject jsonResult){
//        Log.i("Server response:",jsonResult.toString());
            try {
                peddlers=jsonResult.getJSONArray("data");
                for(int i=0;i<peddlers.length();i++){

                    JSONObject O=peddlers.getJSONObject(i);
                    String name=O.getString(PED_NAME);
                    String location=O.getString(COORDS);
                    String phonenumber=O.getString(PHONE_NUMBER);


                    HashMap<String, String> PEDDLER = new HashMap<String, String>();

                    PEDDLER.put(PED_NAME,name);
                    PEDDLER.put(LOCATION,location);
                    PEDDLER.put(PHONE_NUMBER,phonenumber);

                    MY_PEDS_LIST.add(PEDDLER);
                }

                ListAdapter adapter;
                adapter = new SimpleAdapter(
                        Garden.this, MY_PEDS_LIST,
                        R.layout.peds_list, new String[] { PED_NAME, LOCATION,
                        PHONE_NUMBER }, new int[] { R.id.name,
                        R.id.location, R.id.mobile });


                ListView PEDLIST=(ListView) findViewById(R.id.myPeddlerListView);
                PEDLIST.setAdapter(adapter);
                PEDLIST.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        String name=((TextView) findViewById(R.id.name)).getText().toString();
                        String location=((TextView) findViewById(R.id.location)).getText().toString();
                        String phone=((TextView) findViewById(R.id.mobile)).getText().toString();

                        Intent pedInfoIntent=new Intent(getApplicationContext(),PedInfo.class);

                        pedInfoIntent.removeExtra("PED_NAME");
                        pedInfoIntent.removeExtra("PED_LOCATION");
                        pedInfoIntent.removeExtra("PED_PHONE");

                        pedInfoIntent.putExtra("PED_NAME",name);
                        pedInfoIntent.putExtra("PED_LOCATION",location);
                        pedInfoIntent.putExtra("PED_PHONE",phone);
                        startActivity(pedInfoIntent);
                    }
                });


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        public JSONObject fetchPeds(String coords) throws IOException, JSONException {
            JSONObject jsonR = new JSONObject();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user-coords",coords));
            params.add(new BasicNameValuePair("request","local-peds"));
            jsonR=makeHttpRequest(serverURL,"GET",params);
            return jsonR;
        }

        private String convertInputStreamToString(InputStream inputStream) throws IOException{
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;

        }

        public JSONObject makeHttpRequest(String url, String method,List<NameValuePair> params) {

            // Making HTTP request
            try {

                // check for request method
                if(method == "POST"){
                    // request method is POST
                    // defaultHttpClient
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(url);

                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    is = httpEntity.getContent();

                }else if(method == "GET"){
                    // request method is GET
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    String paramString = URLEncodedUtils.format(params, "utf-8");
                    url += "?" + paramString;
                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    is = httpEntity.getContent();
                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                json = sb.toString();
                Log.i("json",json);

            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }

            // try parse the string to a JSON object
            try {
                jObj = new JSONObject(json);
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }

            // return JSON String
            return jObj;

        }
    }
}
