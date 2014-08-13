package citruscups.com.sitelinkmobile.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
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

/**
 * Created by Michael on 8/12/2014.
 */
public class PaymentUnitLookupActivity extends Activity {
    private DataSet mDataSet;
    private ProgressDialog mProgressBar;
    private MenuItem mMenuItem;
    private SharedPreferences mSharedPreferences;
    private ListView mainListView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment_unit_lookup);

        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);

        mainListView = (ListView) findViewById(R.id.payment_unitLookup_listView);

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                HashMap<String, Object> selectedRow = (HashMap<String, Object>) adapterView.getItemAtPosition(i);
                String ledgerID = (String) selectedRow.get("LedgerID");
                Toast.makeText(getApplicationContext(), "LedgerID " + ledgerID, Toast.LENGTH_SHORT).show();
            }
        });
        mainListView.setTextFilterEnabled(true);

        new GetAccountBalances().execute();
    }

    private class GetAccountBalances extends AsyncTask<Void, Void, Void>
    {
        private DataTable dataTable;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            mProgressBar = new ProgressDialog(PaymentUnitLookupActivity.this);
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
            final String TenantID = (String) PaymentUnitLookupActivity.this.getIntent().getExtras().get("TenantID");
            final Integer numMonthsPrepay = 0;

            Hashtable<String, Object> params = new Hashtable<String, Object>();
            params.put("sCorpCode", corpCode);
            params.put("sLocationCode", locationCode);
            params.put("sCorpUserName", userName);
            params.put("sCorpPassword", password);
            params.put("iTenantID", TenantID);
            params.put("iNumberOfMonthsPrepay", numMonthsPrepay);
            mDataSet = ServerStuff.callSoapMethod("CustomerAccountsBalanceDetailsWithPrepayment", params);
            if (mDataSet != null)
            {
                dataTable = mDataSet.getTableByName("Table1");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPreExecute();


            if (mProgressBar.isShowing())
                mProgressBar.dismiss();

            //String columns[] = row.keySet().toArray(new String[row.size()]);

            ArrayList<Map<String, Object>> rows = dataTable.getRows();
            ArrayList<Map<String, String>> rows2 = new ArrayList<Map<String, String>>();
            String unitName = "";
            Double unitBalance = 0.00;
            for (Map<String, Object> row : rows)
            {
                if (unitName.equals(row.get("sUnitName"))) {
                    unitBalance += Double.parseDouble((String) row.get("dcBalance"));
                } else {
                    unitName = (String) row.get("sUnitName");
                    unitBalance = Double.parseDouble((String) row.get("dcBalance"));
                }

                // Other Fees is always the last entry for a given unit
                if ("Other Fees".equalsIgnoreCase((String) row.get("sItem"))) {
                    Map<String, String> tempRow = new Hashtable<String, String>();
                    tempRow.put("sUnitName", "Unit Name: " + unitName);
                    tempRow.put("sBalance", "Balance: " + String.format("%.2f", unitBalance));
                    rows2.add(tempRow);
                }

            }
            String columns[] = new String[]{"sUnitName", "sBalance"};
            int fields[] = new int[]{android.R.id.text1, android.R.id.text2};
            ListAdapter adapter = new SimpleAdapter(PaymentUnitLookupActivity.this, rows2, android.R.layout.simple_list_item_2, columns, fields);
            ListView paymentUnitLookupListView = (ListView) findViewById(R.id.payment_unitLookup_listView);
            paymentUnitLookupListView.setAdapter(adapter);

            if (mMenuItem != null)
            {
                mMenuItem.collapseActionView();
                mMenuItem.setActionView(null);
            }
        }
    }
}
