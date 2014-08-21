package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedHashMap;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.dataStructures.DataSet;
import citruscups.com.sitelinkmobile.helper.Helper;
import citruscups.com.sitelinkmobile.server.ServerStuff;

public class ReservationActivity extends Activity
{
    private int mTenantId;
    private int mUnitId;
    private HashMap<String, Object> mTenantMap;
    private HashMap<String, Object> mUnitMap;

    private SharedPreferences mSharedPreferences;
    private ProgressDialog mProgressBar;
    private DataSet mDataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        Bundle extras = getIntent().getExtras();
        mTenantId = extras.containsKey("TenantID") ? Integer.parseInt(extras.get("TenantID").toString()) : 0;
        mUnitId = extras.containsKey("UnitID") ? Integer.parseInt(extras.get("UnitID").toString()) : 0;
        mTenantMap = extras.containsKey("TenantMap") ? (HashMap<String, Object>) extras.get("TenantMap") : new HashMap<String, Object>();
        mUnitMap = extras.containsKey("UnitMap") ? (HashMap<String, Object>) extras.get("UnitMap") : new HashMap<String, Object>();

        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);

        fillFields();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reservation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_save:
                new SaveReservation().execute();
                return true;
            case R.id.action_cancel:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fillFields()
    {

        if (mTenantMap.containsKey("sFName") && mTenantMap.containsKey("sLName"))
        {
            TextView tenantName = (TextView) findViewById(R.id.tenantName);
            tenantName.setText(mTenantMap.get("sFName") + " " + mTenantMap.get("sLName"));
        }
        if (mUnitMap.containsKey("sUnitName"))
        {
            TextView unitName = (TextView) findViewById(R.id.unitName);
            unitName.setText(mUnitMap.get("sUnitName").toString());
        }
        if (mUnitMap.containsKey("dcWidth") && mUnitMap.containsKey("dcLength"))
        {
            TextView unitSize = (TextView) findViewById(R.id.unitSize);
            unitSize.setText(mUnitMap.get("dcWidth") + "X" + mUnitMap.get("dcLength"));
        }
    }

    private class SaveReservation extends AsyncTask<Void, Void, Void>
    {
        private int waitingId;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            mProgressBar = new ProgressDialog(ReservationActivity.this);
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

            DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
            String date = datePicker.getYear() + "-" + String.format("%02d", datePicker.getMonth()) + "-" + datePicker.getDayOfMonth() + "T00:00:00";
            //TODO validation against text
            String note = ((TextView) findViewById(R.id.reservationNote)).getText().toString();

            LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
            params.put("sCorpCode", corpCode);
            params.put("sLocationCode", locationCode);
            params.put("sCorpUserName", userName);
            params.put("sCorpPassword", password);
            params.put("sTenantID", mTenantId);
            params.put("sUnitID1", mUnitId);
            params.put("sUnitID2", "");
            params.put("sUnitID3", "");
            params.put("dNeeded", date); //1975-01-01
            params.put("sComment", note);
            mDataSet = ServerStuff.callSoapMethod("ReservationNew", params);
            if (mDataSet != null)
            {
                int retCode = Helper.getRtValue(mDataSet);
                if (retCode > 0)
                    waitingId = retCode;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPreExecute();

            if (mProgressBar.isShowing())
                mProgressBar.dismiss();

            if (waitingId > 0)
            {
                //TODO show dialog as to status
               /* new AlertDialog.Builder(ReservationActivity.this)
                        .setTitle("Success")
                        .setMessage("Reservation successfully saved!")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();*/
            }

            finish();
        }
    }
}
