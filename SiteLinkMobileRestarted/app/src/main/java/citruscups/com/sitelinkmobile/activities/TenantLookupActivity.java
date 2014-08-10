package citruscups.com.sitelinkmobile.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.dataStructures.DataSet;
import citruscups.com.sitelinkmobile.dataStructures.DataTable;
import citruscups.com.sitelinkmobile.server.ServerStuff;

public class TenantLookupActivity extends Activity
{
    private DataSet _dataSet;
    private ProgressDialog _progressBar;
    private SharedPreferences mSharedPreferences;

    public DataSet getDataSet()
    {
        return _dataSet;
    }

    public void setDataSet(DataSet ds)
    {
        _dataSet = ds;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tenant_lookup);
        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                HashMap<String, Object> selectedRow = (HashMap<String, Object>) adapterView.getItemAtPosition(i);
                String tenantId = (String) selectedRow.get("TenantID");
                Toast.makeText(getApplicationContext(), "TenantId: " + tenantId, Toast.LENGTH_SHORT).show();
            }
        });
        new GetTenants().execute();
    }

    private class GetTenants extends AsyncTask<Void, Void, Void>
    {
        private DataTable dataTable;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            _progressBar = new ProgressDialog(TenantLookupActivity.this);
            _progressBar.setMessage("Please wait...");
            _progressBar.setCancelable(false);
            _progressBar.show();
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
            _dataSet = ServerStuff.callSoapMethod("TenantListDetailed", params);
            if (_dataSet != null)
            {
                dataTable = _dataSet.getTableByName("Table");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPreExecute();

            if (_progressBar.isShowing())
                _progressBar.dismiss();

            //String columns[] = row.keySet().toArray(new String[row.size()]);
            ArrayList<Map<String, Object>> rows = dataTable.getRows();
            String columns[] = new String[]{"sFName", "sLName", "sCompany", "sCity", "sPostalCode", "sPhone", "TenantID"};
            int fields[] = new int[]{R.id.firstName, R.id.lastName, R.id.company, R.id.city, R.id.postalCode, R.id.phone, R.id.tenantId};
            ListAdapter adapter = new SimpleAdapter(TenantLookupActivity.this, rows, R.layout.tenant_lookup_list_item, columns, fields);

            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(adapter);
        }
    }
}
