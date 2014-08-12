package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.Toast;

import java.util.HashMap;
import java.util.Hashtable;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.dataStructures.DataSet;
import citruscups.com.sitelinkmobile.dataStructures.DataTable;
import citruscups.com.sitelinkmobile.helper.TenantLookupAdapter;
import citruscups.com.sitelinkmobile.server.ServerStuff;

public class TenantLookupActivity extends Activity implements SearchView.OnQueryTextListener
{
    private DataSet mDataSet;
    private ProgressDialog mProgressBar;
    private MenuItem mMenuItem;
    private SharedPreferences mSharedPreferences;
    ListView mainListView;
    TenantLookupAdapter tenantLookupAdapter;

    public DataSet getDataSet()
    {
        return mDataSet;
    }
    public void setDataSet(DataSet ds)
    {
        mDataSet = ds;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tenant_lookup);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);

        tenantLookupAdapter = new TenantLookupAdapter(this, getLayoutInflater());
        mainListView = (ListView) findViewById(R.id.listView);

        mainListView.setAdapter(tenantLookupAdapter);
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                HashMap<String, Object> selectedRow = (HashMap<String, Object>) adapterView.getItemAtPosition(i);
                String tenantId = (String) selectedRow.get("TenantID");
                Toast.makeText(getApplicationContext(), "TenantId: " + tenantId, Toast.LENGTH_SHORT).show();
            }
        });
        mainListView.setTextFilterEnabled(true);

        new GetTenants().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tenant_lookup, menu);

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
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s)
    {
        if (s.equals(""))
        {
            mainListView.clearTextFilter();
            tenantLookupAdapter.getFilter().filter(s);
        }
        else
        {
            //mainListView.getAdapter();
            tenantLookupAdapter.getFilter().filter(s);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_add_tenant:
                //addTenant(tenantId);
                Toast.makeText(getApplicationContext(), "Add Tenant", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_search:
                //search();
                Toast.makeText(getApplicationContext(), "Search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_refresh:
                mMenuItem = item;
                mMenuItem.setActionView(R.layout.progressbar);
                mMenuItem.expandActionView();

                new GetTenants().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void UpdateTenants() {
        DataTable dataTable = mDataSet.getTableByName("Table");
        tenantLookupAdapter.updateData(dataTable);
    }
/*
    public void GetTenants() {
        //Temp
        final String corpCode = mSharedPreferences.getString("CorpCode", "DEMO");
        final String locationCode = mSharedPreferences.getString("LocationCode", "DEMO");
        final String userName = mSharedPreferences.getString("UserName", "DEMO");
        final String password = mSharedPreferences.getString("Password", "DEMO");

        DataTable dataTable = null;

        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("sCorpCode", corpCode);
        params.put("sLocationCode", locationCode);
        params.put("sCorpUserName", userName);
        params.put("sCorpPassword", password);

        setProgressBarIndeterminateVisibility(true);

        mDataSet = ServerStuff.callSoapMethod("TenantListDetailed", params);

        setProgressBarIndeterminateVisibility(false);

        if (mDataSet != null)
        {
            dataTable = mDataSet.getTableByName("Table");
        }

        tenantLookupAdapter.updateData(dataTable);
    }
*/

    private class GetTenants extends AsyncTask<Void, Void, Void>
    {
        private DataTable dataTable;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            mProgressBar = new ProgressDialog(TenantLookupActivity.this);
            mProgressBar.setMessage("Please wait...");
            mProgressBar.setCancelable(false);
            mProgressBar.show();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            //Temp
            final String corpCode = mSharedPreferences.getString("CorpCode", "DEMO");
            final String locationCode = mSharedPreferences.getString("LocationCode", "DEMO");
            final String userName = mSharedPreferences.getString("UserName", "DEMO");
            final String password = mSharedPreferences.getString("Password", "DEMO");

            Hashtable<String, Object> params = new Hashtable<String, Object>();
            params.put("sCorpCode", corpCode);
            params.put("sLocationCode", locationCode);
            params.put("sCorpUserName", userName);
            params.put("sCorpPassword", password);
            mDataSet = ServerStuff.callSoapMethod("TenantListDetailed", params);
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

            // Cannot update views from a different thread.
            UpdateTenants();

            //String columns[] = row.keySet().toArray(new String[row.size()]);

            /*
            ArrayList<Map<String, Object>> rows = dataTable.getRows();
            ArrayList<Map<String, String>> rows2 = new ArrayList<Map<String, String>>();
            for (Map<String, Object> row : rows)
            {
                Map<String, String> tempRow = new Hashtable<String, String>();
                for (Map.Entry<String, Object> entry : row.entrySet())
                {
                    tempRow.put(entry.getKey(), (String) entry.getValue());
                }
                rows2.add(tempRow);
            }
            String columns[] = new String[]{"sFName", "sLName", "sCompany", "sCity", "sPostalCode", "sPhone", "TenantID"};
            int fields[] = new int[]{R.id.firstName, R.id.lastName, R.id.company, R.id.city, R.id.postalCode, R.id.phone, R.id.tenantId};
            ListAdapter adapter = new SimpleAdapter(TenantLookupActivity.this, rows2, R.layout.tenant_lookup_list_item, columns, fields);
            */



            if (mMenuItem != null)
            {
                mMenuItem.collapseActionView();
                mMenuItem.setActionView(null);
            }
        }
    }

}
