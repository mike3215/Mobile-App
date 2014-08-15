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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
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
                Intent intent = new Intent(PaymentUnitLookupActivity.this, PaymentActivity.class);
                startActivity(intent);
            }
        });
        mainListView.setTextFilterEnabled(true);

        new GetAccountBalances().execute();
    }

    private void UpdateBalances(ArrayList<Map<String, String>> rows) {
        mAdapter.updateData(rows);
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
            ArrayList<Map<String, String>> rows2 = new ArrayList<Map<String, String>>();
            List<Double> values = new ArrayList<Double>();
            String unitID = "";
            for (Map<String, Object> row : rows)
            {
                if (unitID.equals(row.get("UnitID"))) {
                    values.add(Double.parseDouble((String) row.get("dcBalance")));
                } else {
                    unitID = (String) row.get("UnitID");
                    values = new ArrayList<Double>();
                    values.add(Double.parseDouble((String) row.get("dcBalance")));
                }

                // Other Fees is always the last entry for a given unit
                if ("Other Fees".equalsIgnoreCase((String) row.get("sItem"))) {
                    Map<String, String> tempRow = new Hashtable<String, String>();
                    tempRow.put("sUnitName", (String) row.get("sUnitName"));
                    tempRow.put("sRent", String.format("%.2f", values.get(2)));
                    tempRow.put("sInsurance", String.format("%.2f", values.get(3)));
                    tempRow.put("sFees", String.format("%.2f", values.get(4) + values.get(5) + values.get(6)));
                    tempRow.put("sOther", String.format("%.2f", values.get(0) + values.get(1)));
                    tempRow.put("sBalance", String.format("%.2f", values.get(0) + values.get(1) + values.get(2)
                                                                    + values.get(3) + values.get(4) + values.get(5)
                                                                    + values.get(6)));
                    tempRow.put("UnitID", (String) row.get("UnitID"));
                    tempRow.put("LedgerID", (String) row.get("LedgerID"));
                    rows2.add(tempRow);
                }

            }

            UpdateBalances(rows2);
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
