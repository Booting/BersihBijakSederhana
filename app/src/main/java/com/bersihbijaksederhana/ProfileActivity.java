package com.bersihbijaksederhana;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bersihbijaksederhana.SupportClass.FontCache;
import com.bersihbijaksederhana.SupportClass.Referensi;
import com.bersihbijaksederhana.SupportClass.TypeFaceSpan;
import com.bersihbijaksederhana.SupportClass.callURL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private Typeface fontLatoBold, fontLatoRegular, fontLatoHeavy, fontLatoBlack, fontLatoItalic;
    private Toolbar toolbar;
    private EditText txtEmail, txtFirstName, txtLastName, txtPhoneNumber, txtTanggalLahir;
    private TextView lblEmail, lblFirstName, lblLastName, lblPhoneNumber, lblTanggalLahir, txtLogOut;
    private RequestQueue queue;
    private ProgressDialog pDialog;
    private SharedPreferences appsPref;
    private JSONArray str_login  = null;
    private int year, month, day;
    private LinearLayout linLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        setContentView(R.layout.activity_profile);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        initToolbar();
        initUI();
        getUserDetail();
    }

    private void initToolbar() {
        SpannableString spanToolbar = new SpannableString("Profile");
        spanToolbar.setSpan(new TypeFaceSpan(ProfileActivity.this, "Lato-Bold"), 0, spanToolbar.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

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
        queue    	    = Volley.newRequestQueue(this);
        fontLatoBold    = FontCache.get(ProfileActivity.this, "Lato-Bold");
        fontLatoRegular = FontCache.get(ProfileActivity.this, "Lato-Regular");
        fontLatoHeavy   = FontCache.get(ProfileActivity.this, "Lato-Heavy");
        fontLatoBlack   = FontCache.get(ProfileActivity.this, "Lato-Black");
        fontLatoItalic  = FontCache.get(ProfileActivity.this, "Lato-Italic");
        lblEmail        = (TextView) findViewById(R.id.lblEmail);
        lblFirstName    = (TextView) findViewById(R.id.lblFirstName);
        lblLastName     = (TextView) findViewById(R.id.lblLastName);
        lblPhoneNumber  = (TextView) findViewById(R.id.lblPhoneNumber);
        lblTanggalLahir = (TextView) findViewById(R.id.lblTanggalLahir);
        txtEmail        = (EditText) findViewById(R.id.txtEmail);
        txtFirstName    = (EditText) findViewById(R.id.txtFirstName);
        txtLastName     = (EditText) findViewById(R.id.txtLastName);
        txtPhoneNumber  = (EditText) findViewById(R.id.txtPhoneNumber);
        txtTanggalLahir = (EditText) findViewById(R.id.txtTanggalLahir);
        linLogOut       = (LinearLayout) findViewById(R.id.linLogOut);
        txtLogOut       = (TextView) findViewById(R.id.txtLogOut);

        lblEmail.setTypeface(fontLatoBold);
        lblFirstName.setTypeface(fontLatoBold);
        lblLastName.setTypeface(fontLatoBold);
        lblPhoneNumber.setTypeface(fontLatoBold);
        lblTanggalLahir.setTypeface(fontLatoBold);
        txtEmail.setTypeface(fontLatoRegular);
        txtFirstName.setTypeface(fontLatoRegular);
        txtLastName.setTypeface(fontLatoRegular);
        txtPhoneNumber.setTypeface(fontLatoRegular);
        txtTanggalLahir.setTypeface(fontLatoRegular);
        txtLogOut.setTypeface(fontLatoHeavy);

        pDialog = new ProgressDialog(ProfileActivity.this);
        pDialog.setMessage("Working...");
        pDialog.setCancelable(false);

        final Calendar c = Calendar.getInstance();
        year  = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day   = c.get(Calendar.DAY_OF_MONTH);

        txtTanggalLahir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                showDialog(999);
            }
        });

        txtFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                invalidateOptionsMenu();
            }
        });

        txtLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                invalidateOptionsMenu();
            }
        });

        linLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = appsPref.edit();
                editor.putString("UserId", "");
                editor.commit();

                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this, myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            txtTanggalLahir.setText(new StringBuilder().append(arg3).append(" ").append(formatMonth(arg2 + 1)).append(" ").append(arg1));
            invalidateOptionsMenu();
        }
    };

    public String formatMonth(int month) {
        DateFormat formatter = new SimpleDateFormat("MMMM", Locale.US);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, month-1);
        return formatter.format(calendar.getTime());
    }

    public void getUserDetail() {
        pDialog.show();
        String url = Referensi.url+"/getUserDetail.php?UserId="+appsPref.getString("UserId", "");
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    str_login  = response.getJSONArray("info");

                    for (int i = 0; i < str_login.length(); i++){
                        JSONObject ar = str_login.getJSONObject(i);

                        txtEmail.setText(ar.getString("Email"));
                        txtFirstName.setText(ar.getString("Firstname"));
                        txtLastName.setText(ar.getString("Lastname"));
                        txtPhoneNumber.setText(ar.getString("ContactNumber"));
                        txtTanggalLahir.setText(ar.getString("TanggalLahir"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                Toast.makeText(getBaseContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsObjRequest);
    }

    private class updateUserDetail extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            String url = "";
            try {
                String mFirstName    = URLEncoder.encode(txtFirstName.getText().toString().replace("\"", "'"), "utf-8");
                String mLastName     = URLEncoder.encode(txtLastName.getText().toString().replace("\"", "'"), "utf-8");
                String mTanggalLahir = URLEncoder.encode(txtTanggalLahir.getText().toString().replace("\"", "'"), "utf-8");

                url = Referensi.url + "/service.php?ct=UPDATEUSER" +
                        "&Firstname="+mFirstName+
                        "&Lastname="+mLastName+
                        "&TanggalLahir="+mTanggalLahir+
                        "&ContactNumber="+txtPhoneNumber.getText().toString()+
                        "&UserId="+appsPref.getString("UserId", "");

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String hasil = callURL.call(url);
            return hasil;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if (result.equalsIgnoreCase("true")) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileActivity.this);
                alertDialog.setTitle("Success");
                alertDialog.setMessage("Update data succesfully!");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        setResult(Activity.RESULT_OK);
                        dialog.cancel();
                    }
                });
                if (!isFinishing()) { alertDialog.show(); }
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileActivity.this);
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Update data error! Please try again or close apps and open again.");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.cancel();
                    }
                });
                if (!isFinishing()) { alertDialog.show(); }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);

        if (txtFirstName.getText().length()==0 || txtLastName.getText().length()==0) {
            menu.getItem(0).setEnabled(false);
        } else {
            menu.getItem(0).setEnabled(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu:
                new updateUserDetail().execute();
                break;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
