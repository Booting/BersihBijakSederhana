package com.bersihbijaksederhana;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bersihbijaksederhana.SupportClass.Referensi;
import com.bersihbijaksederhana.SupportClass.TypeFaceSpan;
import org.json.JSONArray;

public class ModuleActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView rcModule;
    private RequestQueue queue;
    private ProgressBar progressBar;
    private ModuleAdapter moduleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        initToolbar();
        initUI();
    }

    private void initToolbar() {
        SpannableString spanToolbar = new SpannableString("Module");
        spanToolbar.setSpan(new TypeFaceSpan(ModuleActivity.this, "Lato-Bold"), 0, spanToolbar.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

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
        queue    	= Volley.newRequestQueue(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBusy);
        rcModule	= (RecyclerView) findViewById(R.id.rcModule);

        getAllData();
    }

    public void getAllData() {
        progressBar.setVisibility(View.VISIBLE);

        String url = Referensi.url + "/getAllModule.php";
        JsonArrayRequest jsObjRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                progressBar.setVisibility(View.GONE);
                rcModule.setVisibility(View.VISIBLE);

                moduleAdapter = new ModuleAdapter(ModuleActivity.this, response);
                rcModule.setAdapter(moduleAdapter);
                rcModule.setHasFixedSize(true);
                LinearLayoutManager layoutManager = new LinearLayoutManager(ModuleActivity.this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                rcModule.setLayoutManager(layoutManager);
                rcModule.setItemAnimator(new DefaultItemAnimator());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ModuleActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsObjRequest);
    }
}
