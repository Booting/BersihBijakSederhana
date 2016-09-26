package com.bersihbijaksederhana;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bersihbijaksederhana.SupportClass.FontCache;
import org.json.JSONArray;
import java.util.ArrayList;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {

    private Context context;
    private JSONArray jArrModule;

    private final String MODULID   = "ModulId";
    private final String MODULNAME = "ModulName";
    private final String URL       = "Url";

    private ArrayList<String> arrayModulId = new ArrayList<String>(),
            arrayModulName = new ArrayList<String>(),
            arrayUrl       = new ArrayList<String>();

    public ModuleAdapter(Context context, JSONArray jArrModule) {
        this.context    = context;
        this.jArrModule = jArrModule;

        for (int i=0; i<this.jArrModule.length(); i++) {
            arrayModulId.add(this.jArrModule.optJSONObject(i).optString(MODULID));
            arrayModulName.add(this.jArrModule.optJSONObject(i).optString(MODULNAME));
            arrayUrl.add(this.jArrModule.optJSONObject(i).optString(URL));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        final View moduleView = mInflater.inflate(R.layout.module_cell, parent, false);

        return new ViewHolder(moduleView);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, final int position) {
        vh.lblModule.setText(arrayModulName.get(position));

        vh.relParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ModulePreviewActivity.class);
                intent.putExtra("name", arrayModulName.get(position));
                intent.putExtra("url", arrayUrl.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jArrModule.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relParent;
        TextView lblModule;
        Typeface fontLatoReguler;

        public ViewHolder(View view) {
            super(view);
            fontLatoReguler = FontCache.get(context, "Lato-Regular");
            relParent       = (RelativeLayout) view.findViewById(R.id.relParent);
            lblModule       = (TextView) view.findViewById(R.id.lblModule);

            lblModule.setTypeface(fontLatoReguler);
        }
    }
}
