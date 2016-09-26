package com.bersihbijaksederhana;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.bersihbijaksederhana.SupportClass.Referensi;
import com.bersihbijaksederhana.SupportClass.callURL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends Activity implements Animation.AnimationListener {
    private Typeface fontLatoBold, fontLatoRegular, fontLatoHeavy, fontLatoBlack, fontLatoItalic;
    private TextView lblTitle, lblError, lblCopyright, lblLogin, lblRegister;
    private EditText txtEmail, txtPassword, txtEmailR, txtPasswordR, txtConfirmPassword;
    private Button btnLogin, btnRegister;
    private Animation animSlideUp, animSlideDown;
    private LinearLayout linBoxSignIn, linBoxRegister, linErrorAlert;
    private RelativeLayout relLogin, relRegister;
    private View viewLogin, viewRegister;
    private RequestQueue queue;
    private ProgressDialog pDialog;
    private String url="", strDeviceId="";
    private SharedPreferences appsPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        appsPref    	   = getSharedPreferences(Referensi.PREF_NAME, Activity.MODE_PRIVATE);
        queue    	       = Volley.newRequestQueue(this);
        fontLatoBold       = FontCache.get(MainActivity.this, "Lato-Bold");
        fontLatoRegular    = FontCache.get(MainActivity.this, "Lato-Regular");
        fontLatoHeavy      = FontCache.get(MainActivity.this, "Lato-Heavy");
        fontLatoBlack      = FontCache.get(MainActivity.this, "Lato-Black");
        fontLatoItalic     = FontCache.get(MainActivity.this, "Lato-Italic");
        lblTitle           = (TextView) findViewById(R.id.lblTitle);
        txtEmail           = (EditText) findViewById(R.id.txtEmail);
        txtPassword        = (EditText) findViewById(R.id.txtPassword);
        lblError           = (TextView) findViewById(R.id.lblError);
        btnLogin           = (Button) findViewById(R.id.btnLogin);
        lblCopyright       = (TextView) findViewById(R.id.lblCopyright);
        animSlideUp        = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        animSlideDown      = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        linBoxSignIn       = (LinearLayout) findViewById(R.id.linBoxSignIn);
        linErrorAlert      = (LinearLayout) findViewById(R.id.linErrorAlert);
        lblLogin           = (TextView) findViewById(R.id.lblLogin);
        lblRegister        = (TextView) findViewById(R.id.lblRegister);
        linBoxRegister     = (LinearLayout) findViewById(R.id.linBoxRegister);
        txtEmailR          = (EditText) findViewById(R.id.txtEmailR);
        txtPasswordR       = (EditText) findViewById(R.id.txtPasswordR);
        txtConfirmPassword = (EditText) findViewById(R.id.txtConfirmPassword);
        relLogin           = (RelativeLayout) findViewById(R.id.relLogin);
        relRegister        = (RelativeLayout) findViewById(R.id.relRegister);
        viewLogin          = (View) findViewById(R.id.viewLogin);
        viewRegister       = (View) findViewById(R.id.viewRegister);
        btnRegister        = (Button) findViewById(R.id.btnRegister);

        txtEmail.addTextChangedListener(textWatcher);
        txtPassword.addTextChangedListener(textWatcher);
        txtEmailR.addTextChangedListener(textWatcherR);
        txtPasswordR.addTextChangedListener(textWatcherR);
        txtConfirmPassword.addTextChangedListener(textWatcherR);

        animSlideUp.setAnimationListener(this);
        animSlideDown.setAnimationListener(this);

        lblTitle.setTypeface(fontLatoHeavy);
        txtEmail.setTypeface(fontLatoRegular);
        txtPassword.setTypeface(fontLatoRegular);
        lblError.setTypeface(fontLatoItalic);
        btnLogin.setTypeface(fontLatoBold);
        lblCopyright.setTypeface(fontLatoRegular);
        lblLogin.setTypeface(fontLatoBold);
        lblRegister.setTypeface(fontLatoBold);
        txtEmailR.setTypeface(fontLatoRegular);
        txtPasswordR.setTypeface(fontLatoRegular);
        txtConfirmPassword.setTypeface(fontLatoRegular);
        btnRegister.setTypeface(fontLatoBold);

        btnLogin.setEnabled(false);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtEmail.getText().length()==0) {
                    linBoxSignIn.setActivated(true);
                    if (linErrorAlert.getVisibility()==View.GONE) {
                        linErrorAlert.setVisibility(View.VISIBLE);
                        linErrorAlert.startAnimation(animSlideDown);
                    }
                } else if (txtPassword.getText().length()==0) {
                    linBoxSignIn.setActivated(true);
                    if (linErrorAlert.getVisibility()==View.GONE) {
                        linErrorAlert.setVisibility(View.VISIBLE);
                        linErrorAlert.startAnimation(animSlideDown);
                    }
                } else {
                    linBoxSignIn.setActivated(false);
                    if (linErrorAlert.getVisibility()==View.VISIBLE) {
                        linErrorAlert.startAnimation(animSlideUp);
                    }

                    // Call Login Method
                    pDialog.show();
                    getLogin(txtEmail.getText().toString(), txtPassword.getText().toString());
                }
            }
        });

        btnRegister.setEnabled(false);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtEmailR.getText().length()==0) {
                    linBoxRegister.setActivated(true);
                    if (linErrorAlert.getVisibility()==View.GONE) {
                        linErrorAlert.setVisibility(View.VISIBLE);
                        linErrorAlert.startAnimation(animSlideDown);
                    }
                } else if (txtPasswordR.getText().length()==0) {
                    linBoxRegister.setActivated(true);
                    if (linErrorAlert.getVisibility()==View.GONE) {
                        linErrorAlert.setVisibility(View.VISIBLE);
                        linErrorAlert.startAnimation(animSlideDown);
                    }
                } else if (!txtPasswordR.getText().toString().equalsIgnoreCase(txtConfirmPassword.getText().toString())) {
                    linBoxRegister.setActivated(true);
                    if (linErrorAlert.getVisibility()==View.GONE) {
                        linErrorAlert.setVisibility(View.VISIBLE);
                        linErrorAlert.startAnimation(animSlideDown);
                    }
                } else if (!Referensi.isValidEmail(txtEmailR.getText().toString())) {
                    linBoxRegister.setActivated(true);
                    if (linErrorAlert.getVisibility()==View.GONE) {
                        linErrorAlert.setVisibility(View.VISIBLE);
                        linErrorAlert.startAnimation(animSlideDown);
                    }
                } else {
                    linBoxRegister.setActivated(false);
                    if (linErrorAlert.getVisibility()==View.VISIBLE) {
                        linErrorAlert.startAnimation(animSlideUp);
                    }

                    // Call Register Method
                    cekEmail();
                }
            }
        });

        relRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linBoxSignIn.setVisibility(View.GONE);
                linBoxRegister.setVisibility(View.VISIBLE);
                viewLogin.setVisibility(View.GONE);
                viewRegister.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.GONE);
                btnRegister.setVisibility(View.VISIBLE);
                linBoxRegister.setActivated(false);
                linBoxSignIn.setActivated(false);
                if (linErrorAlert.getVisibility()==View.VISIBLE) {
                    linErrorAlert.startAnimation(animSlideUp);
                }
                btnRegister.setEnabled(false);
                btnLogin.setEnabled(false);
                txtEmailR.setText("");
                txtPasswordR.setText("");
                txtConfirmPassword.setText("");
            }
        });

        relLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linBoxRegister.setVisibility(View.GONE);
                linBoxSignIn.setVisibility(View.VISIBLE);
                viewRegister.setVisibility(View.GONE);
                viewLogin.setVisibility(View.VISIBLE);
                btnRegister.setVisibility(View.GONE);
                btnLogin.setVisibility(View.VISIBLE);
                linBoxRegister.setActivated(false);
                linBoxSignIn.setActivated(false);
                if (linErrorAlert.getVisibility()==View.VISIBLE) {
                    linErrorAlert.startAnimation(animSlideUp);
                }
                btnRegister.setEnabled(false);
                btnLogin.setEnabled(false);
                txtEmail.setText("");
                txtPassword.setText("");
            }
        });

        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setMessage("Working...");
        pDialog.setCancelable(false);

        if (!appsPref.getString("UserId", "").equals("")) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
        }
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        public void afterTextChanged(Editable s) {
            if (txtEmail.getText().length()==0 && txtPassword.getText().length()==0) {
                btnLogin.setEnabled(false);
            } else {
                /** HIDE ERROR VIEW **/
                if (linErrorAlert.getVisibility()==View.VISIBLE) {
                    linErrorAlert.startAnimation(animSlideUp);
                }
                /** ENABLE BUTTON 'SIGN IN' **/
                btnLogin.setEnabled(true);
            }}
    };

    private final TextWatcher textWatcherR = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        public void afterTextChanged(Editable s) {
            if (txtEmailR.getText().length()==0 && txtPasswordR.getText().length()==0 && txtConfirmPassword.getText().length()==0) {
                btnRegister.setEnabled(false);
            } else {
                /** HIDE ERROR VIEW **/
                if (linErrorAlert.getVisibility()==View.VISIBLE) {
                    linErrorAlert.startAnimation(animSlideUp);
                }
                /** ENABLE BUTTON 'SIGN IN' **/
                btnRegister.setEnabled(true);
            }}
    };

    @Override
    public void onAnimationEnd(Animation animation) {
        // Take any action after completing the animation
        // check for zoom in animation
        if (animation == animSlideUp) {
            linBoxSignIn.setActivated(false);
            linErrorAlert.setVisibility(View.GONE);
        }
    }
    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub
    }

    public void cekEmail() {
        pDialog.show();
        String url = Referensi.url+"/cekEmail.php?Email="+txtEmailR.getText().toString();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equalsIgnoreCase("true")) {
                        Toast.makeText(getApplicationContext(), "Sorry, email was registered!", Toast.LENGTH_LONG).show();
                        pDialog.dismiss();
                    } else {
                        new addNewUser().execute();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    pDialog.dismiss();
                }
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

    private class addNewUser extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            url = Referensi.url + "/service.php?ct=REGISTER&Email=" + txtEmailR.getText().toString() +
                    "&Password=" + txtPasswordR.getText().toString() +
                    "&DeviceId=" + strDeviceId;

            String hasil = callURL.call(url);
            return hasil;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(getApplicationContext(), "Register succesfully!", Toast.LENGTH_LONG).show();
            getLogin(txtEmailR.getText().toString(), txtPasswordR.getText().toString());
        }
    }

    public void getLogin(String strEmail, String strPassword) {
        String url = Referensi.url+"/login.php?Email="+strEmail+"&Password="+strPassword;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jArrLogin  = response.getJSONArray("statuslogin");

                    for (int i = 0; i<jArrLogin.length(); i++){
                        JSONObject jObjLogin = jArrLogin.getJSONObject(i);

                        if (jObjLogin.getString("st").equalsIgnoreCase("ok")) {
                            SharedPreferences.Editor editor = appsPref.edit();
                            editor.putString("UserId", jObjLogin.optString("UserId"));
                            editor.putString("UserType", jObjLogin.optString("UserType"));
                            editor.putString("DeviceId", jObjLogin.optString("DeviceId"));
                            editor.commit();

                            new addNewLog().execute();
                        }
                        Toast.makeText(getApplicationContext(), jObjLogin.getString("hasil"), Toast.LENGTH_LONG).show();
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

    private class addNewLog extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            Calendar calendar   = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            String strDateNow   = df.format(calendar.getTime());
            String strDateNoww  = null;
            try {
                strDateNoww = URLEncoder.encode(strDateNow, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            url = Referensi.url + "/service.php?ct=LOGINLOG&UserId=" + appsPref.getString("UserId", "") +
                    "&Log=" + strDateNoww;

            String hasil = callURL.call(url);
            return hasil;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            strDeviceId = telephonyManager.getDeviceId();
            if (appsPref.getString("DeviceId", "").isEmpty()) {
                new addNewDeviceId().execute();
            } else {
                if (strDeviceId.equalsIgnoreCase(appsPref.getString("DeviceId", ""))) {
                    pDialog.dismiss();
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    new addNewChangeDevice().execute();
                }
            }
        }
    }

    private class addNewDeviceId extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            String urlAddNewDeviceId = Referensi.url + "/service.php?ct=UPDATEDEVICEID&DeviceId=" + strDeviceId +
                    "&UserId=" + appsPref.getString("UserId", "");

            String hasil = callURL.call(urlAddNewDeviceId);
            return hasil;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private class addNewChangeDevice extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            Calendar calendar   = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            String strDateNow   = df.format(calendar.getTime());
            String strDateNoww  = null;
            try {
                strDateNoww = URLEncoder.encode(strDateNow, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String urlChangeDevice = Referensi.url + "/service.php?ct=CHANGEDEVICE&UserId=" + appsPref.getString("UserId", "") +
                    "&DeviceId=" + strDeviceId +
                    "&LastUpdate=" + strDateNoww;

            String hasil = callURL.call(urlChangeDevice);
            return hasil;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("Warning");
            alertDialog.setMessage("Anda menggunakan Device lain!");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    setResult(Activity.RESULT_OK);
                    dialog.cancel();
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
            if (!isFinishing()) { alertDialog.show(); }
        }
    }
}
