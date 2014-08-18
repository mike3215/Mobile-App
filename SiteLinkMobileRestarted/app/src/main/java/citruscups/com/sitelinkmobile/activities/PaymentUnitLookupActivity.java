package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.adapters.PaymentUnitLookupAdapter;
import citruscups.com.sitelinkmobile.dataStructures.DataSet;
import citruscups.com.sitelinkmobile.dataStructures.DataTable;
import citruscups.com.sitelinkmobile.server.ServerStuff;

/**
 * Created by Michael on 8/12/2014.
 */
public class PaymentUnitLookupActivity extends Activity {
    private DataSet mDataSet;
    private ProgressDialog mProgressBar;
    private SharedPreferences mSharedPreferences;
    private ListView mainListView;
    private PaymentUnitLookupAdapter mAdapter;
    private String mTenantID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment_unit_lookup);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        if (getIntent().hasExtra("TenantID")) {
            mTenantID = getIntent().getExtras().getString("TenantID");
        }

        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);

        mAdapter = new PaymentUnitLookupAdapter(this, getLayoutInflater());

        mainListView = (ListView) findViewById(R.id.payment_unitLookup_listView);

        mainListView.setAdapter(mAdapter);
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                // TODO: Pass along appropriate data
                /*
                Map<String, String> selectedRow = (Map<String, String>) adapterView.getItemAtPosition(i);
                String ledgerID = selectedRow.get("LedgerID");
                Toast.makeText(getApplicationContext(), "LedgerID " + ledgerID, Toast.LENGTH_SHORT).show();
                */
                Map<String, String> selectedRow = (Map<String, String>) adapterView.getItemAtPosition(i);
                int ledgerID = Integer.parseInt(selectedRow.get("LedgerID"));
                Intent intent = new Intent(PaymentUnitLookupActivity.this, PaymentActivity.class);
                intent.putExtra("acctBalTable", mDataSet.getTableByName("AcctBal"));
                intent.putExtra("LedgerID", ledgerID);
                intent.putExtra("TenantID", mTenantID);
                startActivity(intent);
            }
        });
        mainListView.setTextFilterEnabled(true);

        new GetAccountBalances().execute();
    }

    private void UpdateBalances(DataTable dataTable) {
        mAdapter.updateData(dataTable);
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
            final String TenantID = mTenantID;
            final Integer numMonthsPrepay = 0;

            LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
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
            DataTable dataTable = new DataTable();
            dataTable.setName("AcctBal");
            String unitID = "";
            Map<String, Object> newRow = new Hashtable<String, Object>();
            for (Map<String, Object> row : rows)
            {
                if (!unitID.equals(row.get("UnitID"))) {
                    unitID = (String) row.get("UnitID");
                    newRow = new Hashtable<String, Object>();
                }

                newRow.put(row.get("sCategory") + "Bal", Double.parseDouble((String) row.get("dcBalance")));
                newRow.put(row.get("sCategory") + "Amt", Double.parseDouble((String) row.get("dcAmt")));

                // Other Fees is always the last entry for a given unit
                if ("Other Fees".equalsIgnoreCase((String) row.get("sItem"))) {
                    newRow.put("sUnitName", row.get("sUnitName"));
                    newRow.put("UnitID", row.get("UnitID"));
                    newRow.put("LedgerID", row.get("LedgerID"));
                    dataTable.addRow(newRow);
                }

            }

            mDataSet = new DataSet();
            mDataSet.addTable(dataTable);
            UpdateBalances(dataTable);
            /*
            String columns[] = new String[]{"sUnitName", "sBalance"};
            int fields[] = new int[]{android.R.id.text1, android.R.id.text2};
            ListAdapter adapter = new SimpleAdapter(PaymentUnitLookupActivity.this, rows2, android.R.layout.simple_list_item_2, columns, fields);
            ListView paymentUnitLookupListView = (ListView) findViewById(R.id.payment_unitLookup_listView);
            paymentUnitLookupListView.setAdapter(adapter);
            */
        }
    }
}
