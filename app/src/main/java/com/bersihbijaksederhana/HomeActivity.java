package com.bersihbijaksederhana;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bersihbijaksederhana.SupportClass.FontCache;
import com.bersihbijaksederhana.SupportClass.GPSTracker;
import com.bersihbijaksederhana.SupportClass.GoogleMapV2Direction;
import com.bersihbijaksederhana.SupportClass.Referensi;
import com.bersihbijaksederhana.SupportClass.TypeFaceSpan;
import com.bersihbijaksederhana.SupportClass.callURL;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.kennyc.bottomsheet.BottomSheet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by reebonz on 4/23/16.
 */
public class HomeActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener {
    private Toolbar toolbar;
    private GoogleMap mMap;
    private Double newLatitude, newLongitude;
    private JSONArray str_login  = null;
    private SupportMapFragment fragment;
    private GoogleMapV2Direction md;
    private RequestQueue queue;
    private ProgressBar progressBar;
    private GPSTracker gps;
    private LinearLayout linDesc, linRute, linCall, linWhatsApp, linFilter;
    private TextView txtRute, txtCall, txtWhatsApp, txtTitle, txtDetail;
    private Typeface fontLatoBold, fontLatoRegular, fontLatoHeavy, fontLatoBlack, fontLatoItalic;
    private RelativeLayout relMaps;
    private LatLng fromPosition;
    private ArrayList<Marker> markers = new ArrayList<>();
    private long timeInMilliseconds;
    private SharedPreferences appsPref;
    private BottomSheet bottomMoreAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        initToolbar();
        initUI();
    }

    private void initToolbar() {
        SpannableString spanToolbar = new SpannableString("Home");
        spanToolbar.setSpan(new TypeFaceSpan(HomeActivity.this, "Lato-Bold"), 0, spanToolbar.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //Initiate Toolbar/ActionBar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(spanToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initUI() {
        appsPref    	= getSharedPreferences(Referensi.PREF_NAME, Activity.MODE_PRIVATE);
        fontLatoBold    = FontCache.get(HomeActivity.this, "Lato-Bold");
        fontLatoRegular = FontCache.get(HomeActivity.this, "Lato-Regular");
        fontLatoHeavy   = FontCache.get(HomeActivity.this, "Lato-Heavy");
        fontLatoBlack   = FontCache.get(HomeActivity.this, "Lato-Black");
        fontLatoItalic  = FontCache.get(HomeActivity.this, "Lato-Italic");
        queue    	    = Volley.newRequestQueue(this);
        fragment 	    = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        mMap 	        = fragment.getMap();
        progressBar     = (ProgressBar) findViewById(R.id.progressBusy);
        linDesc		    = (LinearLayout) findViewById(R.id.linDesc);
        linRute         = (LinearLayout) findViewById(R.id.linRute);
        linCall         = (LinearLayout) findViewById(R.id.linCall);
        linWhatsApp     = (LinearLayout) findViewById(R.id.linWhatsApp);
        txtRute         = (TextView) findViewById(R.id.txtRute);
        txtCall         = (TextView) findViewById(R.id.txtCall);
        txtWhatsApp     = (TextView) findViewById(R.id.txtWhatsApp);
        txtTitle        = (TextView) findViewById(R.id.txtTitle);
        txtDetail	    = (TextView) findViewById(R.id.txtDetail);
        relMaps         = (RelativeLayout) findViewById(R.id.relMaps);
        md              = new GoogleMapV2Direction();

        mMap.setOnMarkerClickListener(this);
        txtTitle.setTypeface(fontLatoHeavy);
        txtDetail.setTypeface(fontLatoRegular);
        txtRute.setTypeface(fontLatoBlack);
        txtCall.setTypeface(fontLatoBlack);
        txtWhatsApp.setTypeface(fontLatoBlack);

        Calendar c           = Calendar.getInstance();
        SimpleDateFormat df  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        Date mDate;
        try {
            mDate = df.parse(formattedDate);
            timeInMilliseconds = mDate.getTime();
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        mMap.setOnMarkerClickListener(this);
        initLocationManager();
        initMoreActionMenu();
    }

    /**
     * Initialize the location manager.
     */
    private void initLocationManager() {
        // create class object
        gps = new GPSTracker(HomeActivity.this);

        // check if GPS enabled
        if (gps.canGetLocation()) {
            newLatitude  = gps.getLatitude();
            newLongitude = gps.getLongitude();
            new updateUserLocation().execute();
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    private class updateUserLocation extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            String url = Referensi.url+"/service.php?ct=UPDATELOCATION&Latitude="+newLatitude+
                    "&Longitude="+newLongitude+
                    "&LastUpdate="+timeInMilliseconds+
                    "&UserId="+appsPref.getString("UserId", "");
            String hasil = callURL.call(url);
            return hasil;
        }
        @SuppressLint("NewApi")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new updateUserLocationLog().execute();
        }
    }

    private class updateUserLocationLog extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            String url = Referensi.url+"/service.php?ct=UPDATELOCATIONLOG&UserId="+appsPref.getString("UserId", "")+
                    "&Latitude="+newLatitude+
                    "&Longitude="+newLongitude+
                    "&LastUpdate="+timeInMilliseconds;
            String hasil = callURL.call(url);
            return hasil;
        }
        @SuppressLint("NewApi")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            getAllData();
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        try {
            final Handler handler = new Handler();
            final long startTime  = SystemClock.uptimeMillis();
            final long duration   = 2000;

            Projection proj = mMap.getProjection();
            final LatLng markerLatLng = marker.getPosition();
            Point startPoint = proj.toScreenLocation(markerLatLng);
            startPoint.offset(0, -100);
            final LatLng startLatLng = proj.fromScreenLocation(startPoint);
            final Interpolator interpolator = new BounceInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - startTime;
                    float t = interpolator.getInterpolation((float) elapsed / duration);
                    double lng = t * markerLatLng.longitude + (1 - t) * startLatLng.longitude;
                    double lat = t * markerLatLng.latitude + (1 - t) * startLatLng.latitude;
                    marker.setPosition(new LatLng(lat, lng));
                    if (t < 1.0) {
                        handler.postDelayed(this, 16);
                    }
                }
            });

            linDesc.setVisibility(View.VISIBLE);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 13));

            final String[] splitMoreDetail = marker.getTitle().split("\\|");
            final String phoneNumber = splitMoreDetail[1];
            final String userId      = splitMoreDetail[2];

            if (marker.getTitle()!=null) {
                txtTitle.setText(splitMoreDetail[0]);
                txtDetail.setText(Html.fromHtml(marker.getSnippet()));
            } else {
                linDesc.setVisibility(View.GONE);
            }

            linRute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (txtRute.getText().toString().equalsIgnoreCase("Show Log Rute")) {
                        txtRute.setText("Hide Log Rute");
                        getUserLog(userId);
                    } else {
                        mMap.clear();
                        progressBar.setVisibility(View.VISIBLE);
                        getAllData();
                    }
                }
            });

            linCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    if (phoneNumber.equalsIgnoreCase("")) {
                        Toast.makeText(getBaseContext(), "No. Telp is Required!", Toast.LENGTH_SHORT).show();
                    } else {
                        String uri = "tel:" + phoneNumber.replace("-","").replace(" ","");
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse(uri));
                        startActivity(intent);
                    }
                }
            });

            linWhatsApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (phoneNumber.equalsIgnoreCase("")) {
                        Toast.makeText(getBaseContext(), "No. Telp is Required!", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            Uri uri = Uri.parse("smsto:"+ "+62"+phoneNumber.substring(1));
                            Intent i = new Intent(Intent.ACTION_SENDTO, uri);
                            i.setPackage("com.whatsapp");
                            i.putExtra("sms_body", "The text goes here");
                            i.putExtra("chat",true);
                            startActivity(i);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getApplicationContext(), "no whatsapp!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } catch (Exception e) {
            linDesc.setVisibility(View.GONE);
        }
        return true;
    }

    public void getAllData() {
        String url = Referensi.url+"/getAllUser.php";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    str_login = response.getJSONArray("info");

                    for (int i = 0; i < str_login.length(); i++) {
                        JSONObject ar    = str_login.getJSONObject(i);

                        if (!ar.isNull("Longitude") && !ar.isNull("Latitude")) {
                            String Longitude = ar.getString("Longitude");
                            String Latitude  = ar.getString("Latitude");
                            if (!Longitude.isEmpty() || !Latitude.isEmpty()) {
                                initializeMap(Double.parseDouble(Latitude), Double.parseDouble(Longitude), ar);
                            }
                        }

                        if (ar.getString("UserId").equalsIgnoreCase(appsPref.getString("UserId", ""))) {
                            if (!ar.getString("UserType").equalsIgnoreCase("1")) {
                                linRute.setVisibility(View.GONE);
                            } else {
                                linRute.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                txtRute.setText("Show Log Rute");
                progressBar.setVisibility(View.GONE);
                relMaps.setVisibility(View.VISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getBaseContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsObjRequest);
    }

    @SuppressLint("NewApi")
    private void initializeMap(double lat1, double lng1, JSONObject ar) {
        String strUserId        = ar.optString("UserId");
        String strEmail         = ar.optString("Email");
        String strFirstname     = ar.optString("Firstname");
        String strLastname      = ar.optString("Lastname");
        String strContactNumber = ar.optString("ContactNumber");
        String strName          = strFirstname + " " + strLastname;

        if (mMap!=null) {
            fromPosition = new LatLng(newLatitude, newLongitude);
            @SuppressWarnings("unused")
            final LatLng toPosition   = new LatLng(lat1, lng1);

            Location locationA = new Location("point A");
            locationA.setLatitude(newLatitude);
            locationA.setLongitude(newLongitude);

            Location locationB = new Location("point B");
            locationB.setLatitude(lat1);
            locationB.setLongitude(lng1);

            @SuppressWarnings("unused")
            double distance = locationA.distanceTo(locationB);

            // Move the camera instantly to hamburg with a zoom of 15.
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(fromPosition, 11));

            long lastUpdate     = Long.parseLong(ar.optString("LastUpdate"));
            long remainingDays  = Referensi.getRemainingDays(lastUpdate);
            Date dateLastUpdate = new Date(lastUpdate);

            if (remainingDays == 0) {
                Date dtLastUpdate1 = null;
                try {
                    dtLastUpdate1 = Referensi.getSimpleDateFormatHours().parse(Referensi.getSimpleDateFormatHours().format(dateLastUpdate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long eventMillis1   = dtLastUpdate1.getTime();
                long diffMillis1    = Referensi.getCurrentMillis() - eventMillis1;
                long remainingHours = TimeUnit.MILLISECONDS.toHours(diffMillis1);

                if (remainingHours == 0) {
                    long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis1);
                    if (remainingMinutes <= 10) {
                        markers.add(mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat1, lng1))
                                .title(strName + "|" + strContactNumber + "|" + strUserId)
                                .snippet("Email : " + strEmail + "<br>Last Update : " + remainingMinutes + " minutes")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.male_on))));
                    } else {
                        markers.add(mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat1, lng1))
                                .title(strName + "|" + strContactNumber + "|" + strUserId)
                                .snippet("Email : " + strEmail + "<br>Last Update : " + remainingMinutes + " minutes")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.male_idle))));
                    }
                } else {
                    markers.add(mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat1, lng1))
                            .title(strName + "|" + strContactNumber + "|" + strUserId)
                            .snippet("Email : " + strEmail + "<br>Last Update : " + remainingHours + " hours")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.male_off))));
                }
            } else {
                markers.add(mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat1, lng1))
                        .title(strName + "|" + strContactNumber + "|" + strUserId)
                        .snippet("Email : " + strEmail + "<br>Last Update : " + remainingDays + " days")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.male_off))));
            }
        } else {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.container)).getMap();
            Toast.makeText(getApplicationContext(), "MAP NULL", Toast.LENGTH_LONG).show();
        }
    }

    public void getUserLog(String userId) {
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.set(calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH),
                calendarStart.get(Calendar.DAY_OF_MONTH), 6, 0, 0);
        long startTime = calendarStart.getTimeInMillis();

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.set(calendarEnd.get(Calendar.YEAR), calendarEnd.get(Calendar.MONTH),
                calendarEnd.get(Calendar.DAY_OF_MONTH), 21, 0, 0);
        long endTime = calendarEnd.getTimeInMillis();

        progressBar.setVisibility(View.VISIBLE);
        String url = Referensi.url+"/getUserLog.php?UserId="+userId+"&StartDate="+startTime+"&EndDate="+endTime;
        SimpleDateFormat df  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    str_login  = response.getJSONArray("info");

                    mMap.clear();
                    PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng fromPosition=null, toPosition=null;
                    for (int i = 0; i < str_login.length(); i++){
                        JSONObject ar = str_login.getJSONObject(i);

                        LatLng latLng = new LatLng(Double.parseDouble(ar.getString("Latitude")), Double.parseDouble(ar.getString("Longitude")));
                        markerOptions.position(latLng);
                        markerOptions.title("Position "+i);
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));

                        options.add(latLng);
                        mMap.addMarker(markerOptions);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

                        fromPosition = new LatLng(Double.parseDouble(ar.getString("Latitude")), Double.parseDouble(ar.getString("Longitude")));
                        if (i!=str_login.length()-1) {
                            JSONObject jObjToPosition = str_login.getJSONObject(i + 1);
                            toPosition = new LatLng(Double.parseDouble(jObjToPosition.getString("Latitude")),
                                    Double.parseDouble(jObjToPosition.getString("Longitude")));
                            getDirectionMap(fromPosition, toPosition);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getBaseContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsObjRequest);
    }

    private void getDirectionMap(LatLng from, LatLng to) {
        LatLng fromto[] = { from, to };
        new LongOperation().execute(fromto);
    }

    private class LongOperation extends AsyncTask<LatLng, Void, Document> {
        @Override
        protected Document doInBackground(LatLng... params) {
            Document doc = md.getDocument(params[0], params[1], GoogleMapV2Direction.MODE_DRIVING);
            return doc;
        }
        @Override
        protected void onPostExecute(Document result) {
            setResult(result);
        }
        @Override
        protected void onPreExecute() { }
        @Override
        protected void onProgressUpdate(Void... values) { }
    }

    public void setResult(Document doc) {
        ArrayList<LatLng> directionPoint = md.getDirection(doc);
        PolylineOptions rectLine = new PolylineOptions().width(6).color(Color.BLUE);

        for (int i = 0; i < directionPoint.size(); i++) {
            rectLine.add(directionPoint.get(i));
        }

        mMap.addPolyline(rectLine);
    }

    @Override
    public boolean onKeyUp( int keyCode, KeyEvent event ){
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (txtRute.getText().toString().equalsIgnoreCase("Show Log Rute")) {
                finish();
            } else {
                mMap.clear();
                progressBar.setVisibility(View.VISIBLE);
                getAllData();
            }
            return true;
        }
        return super.onKeyUp( keyCode, event );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu:
                bottomMoreAction.show();
                break;
        }
        return true;
    }

    public void initMoreActionMenu() {
        View viewBottom = LayoutInflater.from(this).inflate(R.layout.activity_home_more, (ViewGroup)null);
        BottomSheet.Builder bottomSheetBuilder = new BottomSheet.Builder(HomeActivity.this, R.style.MyBottomSheetStyle).setView(viewBottom);

        bottomMoreAction          = bottomSheetBuilder.create();
        TextView lblMenu          = (TextView) viewBottom.findViewById(R.id.lblMenu);
        TextView lblProfile       = (TextView) viewBottom.findViewById(R.id.lblProfile);
        TextView lblModule        = (TextView) viewBottom.findViewById(R.id.lblModule);
        RelativeLayout relProfile = (RelativeLayout) viewBottom.findViewById(R.id.relProfile);
        RelativeLayout relModule  = (RelativeLayout) viewBottom.findViewById(R.id.relModule);

        lblMenu.setTypeface(fontLatoBold);
        lblProfile.setTypeface(fontLatoRegular);
        lblModule.setTypeface(fontLatoRegular);

        relProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomMoreAction.dismiss();
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        relModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomMoreAction.dismiss();
                Intent intent = new Intent(HomeActivity.this, ModuleActivity.class);
                startActivity(intent);
            }
        });
    }
}
