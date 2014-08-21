package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.adapters.UnitLookupAdapter;
import citruscups.com.sitelinkmobile.dataStructures.DataSet;
import citruscups.com.sitelinkmobile.dataStructures.DataTable;
import citruscups.com.sitelinkmobile.helper.Helper;
import citruscups.com.sitelinkmobile.interfaces.ICommand;
import citruscups.com.sitelinkmobile.server.ServerStuff;

public class UnitLookupActivity extends Activity implements SearchView.OnQueryTextListener
{
    private UnitLookupUsedFor mUsedFor;
    private SharedPreferences mSharedPreferences;
    private UnitLookupAdapter mAdapter;
    private ListView mListView;
    private DataSet mDataSet;
    private ProgressDialog mProgressBar;
    private MenuItem mMenuItem;
    private long mLastPolled;
    private int mTenantId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_lookup);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        final Bundle extras = getIntent().getExtras();
        mUsedFor = extras.containsKey("UsedFor") ? (UnitLookupUsedFor) extras.get("UsedFor") : UnitLookupUsedFor.MoveIn;
        mTenantId = extras.containsKey("TenantID") ? Integer.parseInt(extras.get("TenantID").toString()) : 0;

        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                HashMap<String, Object> selectedRow = (HashMap<String, Object>) adapterView.getItemAtPosition(i);
                String unitId = (String) selectedRow.get("UnitID");

                Toast.makeText(getApplicationContext(), "UnitId: " + unitId, Toast.LENGTH_SHORT).show();
                Intent intent;
                switch (mUsedFor)
                {
                    case InqRes:
                        intent = new Intent(UnitLookupActivity.this, ReservationActivity.class);
                        intent.putExtra("TenantID", mTenantId);
                        intent.putExtra("UnitID", unitId);
                        intent.putExtra("UnitMap", selectedRow);
                        if (extras.containsKey("TenantMap"))
                            intent.putExtra("TenantMap", (HashMap<String, Object>) extras.get("TenantMap"));
                        startActivity(intent);
                        break;

                    case MoveIn:

                        break;
                }
            }
        });
        mListView.setTextFilterEnabled(true);

        new GetUnits().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.unit_lookup, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchView != null)
        {
            searchView.setIconifiedByDefault(false);
            searchView.setOnQueryTextListener(this);
            searchView.setSubmitButtonEnabled(true);
            searchView.setQueryHint("Search Here");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_search:
                return true;
            case R.id.action_refresh:
                mMenuItem = item;
                mMenuItem.setActionView(R.layout.progressbar);
                mMenuItem.expandActionView();

                new GetUnits().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        mAdapter.getFilter().filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        if (newText.equals(""))
        {
            mListView.clearTextFilter();
            mAdapter.getFilter().filter(newText);
        }

        return false;
    }

    public void UpdateUnits()
    {
        DataTable dataTable = mDataSet.getTableByName("Table");
        mAdapter.updateData(dataTable);
    }

    public enum UnitLookupUsedFor
    {
        Payment,
        TenantLookup,
        InqRes,
        MoveIn
    }

    private class GetUnits extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            mProgressBar = new ProgressDialog(UnitLookupActivity.this);
            mProgressBar.setMessage("Please wait...");
            mProgressBar.setCancelable(false);
            mProgressBar.show();
        }

        @Override
        protected Void doInBackground(Void... param)
        {
            //TODO remove DEMO creds before release
            final String corpCode = mSharedPreferences.getString("CorpCode", "DEMO");
            final String locationCode = mSharedPreferences.getString("LocationCode", "DEMO");
            final String userName = mSharedPreferences.getString("UserName", "DEMO");
            final String password = mSharedPreferences.getString("Password", "DEMO");

            LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
            params.put("sCorpCode", corpCode);
            params.put("sLocationCode", locationCode);
            params.put("sCorpUserName", userName);
            params.put("sCorpPassword", password);
            params.put("lngLastTimePolled", "1");
            mDataSet = ServerStuff.callSoapMethod("UnitsInformationAvailableUnitsOnly_v2", params);
            if (mDataSet != null)
            {
                int retCode = Helper.getRtValue(mDataSet);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPreExecute();

            if (mProgressBar.isShowing())
                mProgressBar.dismiss();


            if (mAdapter == null)
            {
                final String[] columns = new String[]{"sUnitName", "sTypeName", "dcWidth", "dcLength", "dcStdRate", "iFloor", "bClimate", "bInside", "bPower", "bAlarm"};
                final int[] to = new int[]{R.id.unitName, R.id.typeName, R.id.width, R.id.length, R.id.standardRate, R.id.floor, R.id.climate, R.id.inside, R.id.power, R.id.alarm};
                mAdapter = new UnitLookupAdapter(UnitLookupActivity.this, mDataSet.getTables().get(0), R.layout.unit_lookup_list_item, columns, to);
                Map<Integer, ICommand> commandMap = new Hashtable<Integer, ICommand>();
                commandMap.put(R.id.standardRate, new ICommand()
                {
                    @Override
                    public int executeColor(String text)
                    {
                        if (Double.parseDouble(text) > 75.00)
                            return Color.RED;
                        else
                            return Color.BLACK;
                    }
                });
                mAdapter.setCommandMap(commandMap);
                mListView.setAdapter(mAdapter);
            }

            // Cannot update views from a different thread.
            UpdateUnits();

            if (mMenuItem != null)
            {
                mMenuItem.collapseActionView();
                mMenuItem.setActionView(null);
            }
        }
    }
}
