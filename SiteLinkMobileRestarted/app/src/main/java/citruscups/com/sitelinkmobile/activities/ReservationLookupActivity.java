package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.HashMap;
import java.util.LinkedHashMap;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.adapters.UnitLookupAdapter;
import citruscups.com.sitelinkmobile.dataStructures.DataSet;
import citruscups.com.sitelinkmobile.dataStructures.DataTable;
import citruscups.com.sitelinkmobile.server.ServerStuff;

public class ReservationLookupActivity extends Activity implements SearchView.OnQueryTextListener
{
    private SharedPreferences mSharedPreferences;
    private ListView mListView;
    private MenuItem mMenuItem;
    private ProgressDialog mProgressBar;

    private UnitLookupAdapter mAdapter;
    private DataSet mDataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reservation_lookup);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }


        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                HashMap<String, Object> selectedRow = (HashMap<String, Object>) adapterView.getItemAtPosition(i);
                String waitingId = (String) selectedRow.get("WaitingID");

                Intent intent;
                intent = new Intent(ReservationLookupActivity.this, ReservationActivity.class);
                intent.putExtra("WaitingID", waitingId);
                intent.putExtra("WaitingMap", selectedRow);
                startActivity(intent);
            }
        });
        mListView.setTextFilterEnabled(true);

        new GetReservations().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reservation_lookup, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchView != null)
        {
            searchView.setIconifiedByDefault(false);
            searchView.setOnQueryTextListener(this);
            searchView.setSubmitButtonEnabled(true);
            searchView.setQueryHint("Search Here");
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String s)
    {

        return true;
    }

    @Override
    public boolean onQueryTextChange(String s)
    {
        if (s.equals(""))
        {
            mListView.clearTextFilter();
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_add_reservation:
                Intent reservationActivity = new Intent(this, ReservationActivity.class);
                startActivity(reservationActivity);
                return true;
            case R.id.action_search:
                return true;
            case R.id.action_refresh:
                mMenuItem = item;
                mMenuItem.setActionView(R.layout.progressbar);
                mMenuItem.expandActionView();

                new GetReservations().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createAdapter()
    {
        if (mAdapter == null)
        {
            final String columns[] = new String[]{"sFName", "sLName", "sUnitName", "dNeeded", "TenantID"};
            final int to[] = new int[]{R.id.firstName, R.id.lastName, R.id.unitName, R.id.neededDate, R.id.tenantId};

            mAdapter = new UnitLookupAdapter(ReservationLookupActivity.this, mDataSet.getTables().get(0), R.layout.reservation_lookup_list_item, columns, to);
            mListView.setAdapter(mAdapter);
        }
    }

    public void UpdateTenants()
    {
        DataTable dataTable = mDataSet.getTableByName("Table");
        mAdapter.updateData(dataTable);
    }

    private class GetReservations extends AsyncTask<Void, Void, Void>
    {
        private DataTable dataTable;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            mProgressBar = new ProgressDialog(ReservationLookupActivity.this);
            mProgressBar.setMessage("Please wait...");
            mProgressBar.setCancelable(false);
            mProgressBar.show();
        }

        @Override
        protected Void doInBackground(Void... arg0)
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
            params.put("iGlobalWaitingNum", -999);

            mDataSet = ServerStuff.callSoapMethod("ReservationList", params);
            if (mDataSet != null)
            {
                dataTable = mDataSet.getTableByName("Table");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPreExecute();


            if (mProgressBar.isShowing())
                mProgressBar.dismiss();

            createAdapter();

            // Cannot update views from a different thread.
            UpdateTenants();

            if (mMenuItem != null)
            {
                mMenuItem.collapseActionView();
                mMenuItem.setActionView(null);
            }
        }
    }
}
