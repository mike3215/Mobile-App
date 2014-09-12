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
import citruscups.com.sitelinkmobile.helper.Constants;
import citruscups.com.sitelinkmobile.helper.Helper;
import citruscups.com.sitelinkmobile.server.ServerStuff;

public class TenantLookupActivity extends Activity implements SearchView.OnQueryTextListener
{
    private DataSet mDataSet;
    private ProgressDialog mProgressBar;
    private MenuItem mMenuItem;
    private SharedPreferences mSharedPreferences;
    private Constants.UsedFor mUsedFor;
    private ListView mListView;
    private UnitLookupAdapter mAdapter;

    private int mUnitId;
    private HashMap<String, Object> mUnitMap;

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
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setDisplayShowTitleEnabled(false);
        }

        Bundle extras = getIntent().getExtras();
        if (this.getIntent().hasExtra("UsedFor")) {
            mUsedFor = (Constants.UsedFor) this.getIntent().getExtras().get("UsedFor");
        } else {
            mUsedFor = Constants.UsedFor.TenantLookup;
        }
        mUnitId = extras.containsKey("UnitID") ? Integer.parseInt(extras.get("UnitID").toString()) : 0;
        mUnitMap = extras.containsKey("UnitMap") ? (HashMap<String, Object>) extras.get("UnitMap") : new HashMap<String, Object>();

        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                HashMap<String, Object> selectedRow = (HashMap<String, Object>) adapterView.getItemAtPosition(i);
                String tenantId = (String) selectedRow.get("TenantID");

                Intent intent;
                switch (mUsedFor)
                {
                    case Payment:
                        intent = new Intent(TenantLookupActivity.this, PaymentUnitLookupActivity.class);
                        intent.putExtra("TenantID", tenantId);
                        intent.putExtra("TenantMap", selectedRow);
                        startActivity(intent);
                        break;

                    case TenantLookup:
                        intent = new Intent(TenantLookupActivity.this, NewTenantActivity.class);
                        intent.putExtra("TenantID", tenantId);
                        intent.putExtra("TenantMap", selectedRow);
                        startActivity(intent);
                        break;

                    case InqRes:
                        intent = new Intent(TenantLookupActivity.this, ReservationLookupActivity.class);
                        intent.putExtra("TenantID", tenantId);
                        intent.putExtra("TenantMap", selectedRow);
                        intent.putExtra("UsedFor", Constants.UsedFor.InqRes);
                        startActivity(intent);
                        break;

                    case MoveIn:
                        intent = new Intent(TenantLookupActivity.this, MoveInActivity.class);
                        intent.putExtra("TenantID", tenantId);
                        intent.putExtra("UnitID", mUnitId);
                        intent.putExtra("TenantMap", selectedRow);
                        intent.putExtra("UnitMap", mUnitMap);
                        intent.putExtra("UsedFor", Constants.UsedFor.MoveIn);
                        startActivity(intent);
                        break;
                }
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                HashMap<String, Object> selectedRow = (HashMap<String, Object>) parent.getItemAtPosition(position);
                String tenantId = (String) selectedRow.get("TenantID");

                Intent intent = new Intent(TenantLookupActivity.this, NewTenantActivity.class);
                intent.putExtra("TenantID", tenantId);
                intent.putExtra("TenantMap", selectedRow);
                startActivity(intent);
                return false;
            }
        });
        mListView.setTextFilterEnabled(true);
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
        new GetTenants().execute(s);

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
            case R.id.action_add_tenant:
                Intent newTenantActivity = new Intent(this, NewTenantActivity.class);
                startActivity(newTenantActivity);
                return true;
            case R.id.action_search:
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

    private void createAdapter()
    {
        if (mAdapter == null)
        {
            final String columns[] = new String[]{"sFName", "sLName", "sCompany", "sCity", "sPostalCode", "sPhone", "sRegion", "TenantID"};
            final int to[] = new int[]{R.id.firstName, R.id.lastName, R.id.company, R.id.city, R.id.postalCode, R.id.phone, R.id.state, R.id.tenantId};

            mAdapter = new UnitLookupAdapter(TenantLookupActivity.this, mDataSet.getTables().get(0), R.layout.tenant_lookup_list_item, columns, to);
            mListView.setAdapter(mAdapter);
        }
    }

    public void UpdateTenants()
    {
        DataTable dataTable = mDataSet.getTableByName("Table");
        mAdapter.updateData(dataTable);
    }

    private class GetTenants extends AsyncTask<String, Void, Void>
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
        protected Void doInBackground(String... arg0)
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

            String search = arg0[0];
            String num = Helper.isNumeric(search) ? search : "";

            params.put("sTenantFirstName", search);
            params.put("sTenantLastName", search);
            params.put("sAddressLine1", search);
            params.put("sAddressLine2", search);
            params.put("sCity", search);
            params.put("sState", search);
            params.put("sZipCode", search);
            params.put("sEmailAddress", search);
            params.put("sPhoneNumber", num);
            params.put("sPhoneNumber2", num);
            params.put("sPhoneNumber3", num);
            params.put("sPhoneNumber4", num);

            mDataSet = ServerStuff.callSoapMethod("TenantSearchDetailed", params);
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
