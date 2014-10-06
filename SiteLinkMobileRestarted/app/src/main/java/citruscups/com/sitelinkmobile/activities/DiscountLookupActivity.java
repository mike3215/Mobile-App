package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
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

public class DiscountLookupActivity extends Activity
{
    private SharedPreferences mSharedPreferences;

    private HashMap<String, Object> mUnitMap;
    private DataSet mDataSet;
    private UnitLookupAdapter mAdapter;

    private ListView mListView;
    private ProgressDialog mProgressBar;
    private MenuItem mMenuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_lookup);
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            // actionBar.setDisplayShowTitleEnabled(false);
        }

        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            mUnitMap = extras.containsKey("UnitMap") ? (HashMap<String, Object>) extras.get("UnitMap") : new HashMap<String, Object>();
        }

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                HashMap<String, Object> selectedRow = (HashMap<String, Object>) adapterView.getItemAtPosition(i);
                String concessionId = (String) selectedRow.get("ConcessionID");

                Intent intent = new Intent();
                intent.putExtra("ConcessionID", concessionId);
                intent.putExtra("DiscountMap", selectedRow);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        mListView.setTextFilterEnabled(true);

        new GetDiscounts().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.discount_lookup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh)
        {
            new GetDiscounts().execute();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createAdapter()
    {
        if (mAdapter == null)
        {
            final String columns[] = new String[]{"sPlanName", "sPlanDesc", "iExpirMonths", "ConcessionID", "sChgDesc"};
            final int to[] = new int[]{R.id.planName, R.id.planDesc, R.id.expires, R.id.concessionId, R.id.discount};

            mAdapter = new UnitLookupAdapter(DiscountLookupActivity.this, mDataSet.getTables().get(0), R.layout.discount_lookup_list_item, columns, to);
            Map<Integer, ICommand> dateFormat = new Hashtable<Integer, ICommand>();
            dateFormat.put(R.id.expires, new ICommand()
            {
                @Override
                public int executeColor(String text)
                {
                    return 0;
                }

                @Override
                public String executeText(String text, Object data)
                {
                    if (data != null)
                    {
                        Map<String, Object> row = (Map<String, Object>) data;
                        if (row.containsKey("bNeverExpires") && row.get("bNeverExpires").toString().equals("true"))
                        {
                            return "Never";
                        }
                    }

                    return text + " Month(s)";
                }
            });
            dateFormat.put(R.id.discount, new ICommand()
            {
                @Override
                public int executeColor(String text)
                {
                    return 0;
                }

                @Override
                public String executeText(String text, Object data)
                {
                    if (data != null)
                    {
                        Map<String, Object> row = (Map<String, Object>) data;
                        if (row.containsKey("sChgDesc"))
                        {
                            String value;
                            switch (Integer.parseInt(row.get("iAmtType").toString()))
                            {
                                case 0:
                                    value = "$" + Helper.formatDecimal(row.get("dcFixedDiscount").toString()) + " off of " + row.get("sChgDesc");
                                    break;
                                case 1:
                                    value = Helper.formatDecimal(row.get("dcPCDiscount").toString()) + "% off of " + row.get("sChgDesc");
                                    break;
                                case 2:
                                    value = "$" + Helper.formatDecimal(row.get("dcChgAmt").toString()) + " charged for " + row.get("sChgDesc");
                                    break;
                                default:
                                    value = text;

                            }

                            return value;
                        }
                    }

                    return text;
                }
            });
            mAdapter.setTextFormatMap(dateFormat);
            mListView.setAdapter(mAdapter);
        }
    }

    public void UpdateDiscounts()
    {
        DataTable dataTable = mDataSet.getTableByName("ConcessionPlans");
        mAdapter.updateData(dataTable);
    }

    private class GetDiscounts extends AsyncTask<Void, Void, Void>
    {
        private DataTable dataTable;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            mProgressBar = new ProgressDialog(DiscountLookupActivity.this);
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

            mDataSet = ServerStuff.callSoapMethod("DiscountPlansRetrieve", params);
            if (mDataSet != null)
            {
                dataTable = mDataSet.getTableByName("ConcessionPlans");
                if (dataTable == null) return null;

                ArrayList<Map<String, Object>> rowsToRemove = new ArrayList<Map<String, Object>>();
                for (Map<String, Object> row : dataTable.getRows())
                {
                    if (row.containsKey("bForCorp") && row.get("bForCorp").toString().equals("true"))
                    {
                        rowsToRemove.add(row);
                    }
                }
                for (Map<String, Object> row : rowsToRemove)
                {
                    dataTable.removeRow(row);
                }

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
            UpdateDiscounts();

            if (mMenuItem != null)
            {
                mMenuItem.collapseActionView();
                mMenuItem.setActionView(null);
            }
        }
    }
}
