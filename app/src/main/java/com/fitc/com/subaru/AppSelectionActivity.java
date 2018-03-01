package com.fitc.com.subaru;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TwoLineListItem;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class AppSelectionActivity extends ListActivity {

    private static final boolean LOGGING =  true;
    private ArrayList<ResolveInfo> mResolveInfo;
    private ArrayAdapter<ResolveInfo> mAdapter;
    private String mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent startIntent  = getIntent();
        if (startIntent!=null){
            if (startIntent.hasExtra("intent-filter")) {
                mIntentFilter = startIntent.getStringExtra("intent-filter");
            }
        }

        mResolveInfo = new ArrayList<>();

        mAdapter = new ArrayAdapter<ResolveInfo>(this,
                android.R.layout.simple_expandable_list_item_2, mResolveInfo) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final TwoLineListItem row;
                if (convertView == null){
                    final LayoutInflater inflater =
                            (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    row = (TwoLineListItem) inflater.inflate(android.R.layout.simple_list_item_2, null);
                } else {
                    row = (TwoLineListItem) convertView;
                }

                final ResolveInfo info = mResolveInfo.get(position);
                final String name = info.activityInfo.name;
                final String pkg = info.resolvePackageName;



                row.getText1().setText(name);

                row.getText2().setText(pkg);

                return row;
            }

        };
        ;


        // Bind to our new adapter.
        setListAdapter(mAdapter);


        getListView().setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (LOGGING) Log.d(TAG, "Pressed item " + position);
                if (position >= mResolveInfo.size()) {
                    if (LOGGING) Log.w(TAG, "Illegal position.");
                    return;
                }

                final ResolveInfo ri = mResolveInfo.get(position);

                // Create intent to deliver some kind of result data
                Intent result = new Intent("com.example.RESULT_ACTION");
                result.putExtra(Constants.EXTRA_RESULT_DATA1, ri);
                setResult(Activity.RESULT_OK, result);
                finish();

            }
        });

        new UpdateDevicesTask().execute(new String[]{mIntentFilter});
    }


    private class UpdateDevicesTask extends AsyncTask<String, Void, List<ResolveInfo>> {
        @Override
        protected List<ResolveInfo> doInBackground(String... params) {


            if (LOGGING) Log.d(TAG, "Refreshing app list ...");

            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);



            List<ResolveInfo> result = AppSelectionActivity.this.getPackageManager().queryIntentActivities( mainIntent, 0);



            return result;
        }

        @Override
        protected void onPostExecute(List<ResolveInfo> result) {
            mResolveInfo.clear();
            mResolveInfo.addAll(result);
            mAdapter.notifyDataSetChanged();

            if (LOGGING) Log.d(TAG, "Done refreshing, " + mResolveInfo.size() + " entries found.");
        }
    }

}
