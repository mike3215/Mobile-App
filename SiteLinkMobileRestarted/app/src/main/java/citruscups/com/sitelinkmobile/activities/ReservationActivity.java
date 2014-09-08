package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.dataStructures.DataSet;
import citruscups.com.sitelinkmobile.helper.Helper;
import citruscups.com.sitelinkmobile.server.ServerStuff;

public class ReservationActivity extends Activity
{
    private int mTenantId;
    private int mUnitId;
    private int mWaitingId;

    private HashMap<String, Object> mTenantMap;
    private HashMap<String, Object> mUnitMap;

    private SharedPreferences mSharedPreferences;
    private ProgressDialog mProgressBar;
    private DataSet mDataSet;

    private Calendar mCalendar = Calendar.getInstance();

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
        mWaitingId = extras.containsKey("WaitingID") ? Integer.parseInt(extras.get("WaitingID").toString()) : 0;
        mTenantMap = extras.containsKey("TenantMap") ? (HashMap<String, Object>) extras.get("TenantMap") : new HashMap<String, Object>();
        mUnitMap = extras.containsKey("UnitMap") ? (HashMap<String, Object>) extras.get("UnitMap") : new HashMap<String, Object>();

        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);

        fillFields();

        final DatePickerDialog.OnDateSetListener neededDateSetListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateDateLabel((EditText) findViewById(R.id.neededDate));
            }
        };
        final DatePickerDialog.OnDateSetListener followupDateSetListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateDateLabel((EditText) findViewById(R.id.followupDate));
            }
        };
        final DatePickerDialog.OnDateSetListener expiresDateSetListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateDateLabel((EditText) findViewById(R.id.expiresDate));
            }
        };

        EditText needed = (EditText) findViewById(R.id.neededDate);
        needed.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerDialog datePickerDialog = new DatePickerDialog(ReservationActivity.this, neededDateSetListener, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        EditText followup = (EditText) findViewById(R.id.followupDate);
        followup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerDialog datePickerDialog = new DatePickerDialog(ReservationActivity.this, followupDateSetListener, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        EditText expires = (EditText) findViewById(R.id.expiresDate);
        expires.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerDialog datePickerDialog = new DatePickerDialog(ReservationActivity.this, expiresDateSetListener, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
    }

    private void updateDateLabel(EditText text)
    {
        String myFormat = "MM/dd/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        text.setText(sdf.format(mCalendar.getTime()));
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
            try
            {
                Double w = Double.parseDouble(mUnitMap.get("dcWidth").toString());
                Double l = Double.parseDouble(mUnitMap.get("dcLength").toString());
                DecimalFormat format = new DecimalFormat("###,##0.00");

                TextView unitSize = (TextView) findViewById(R.id.unitSize);
                unitSize.setText(format.format(w) + "X" + format.format(l));
            }
            catch (NumberFormatException nfe)
            {
            }
        }

        TextView standardRate = (TextView) findViewById(R.id.stdRate);
        standardRate.setText(mUnitMap.get("dcStdRate").toString());
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

            EditText needed = (EditText) findViewById(R.id.neededDate);
            EditText followup = (EditText) findViewById(R.id.followupDate);
            EditText expires = (EditText) findViewById(R.id.expiresDate);
            Editable f = followup.getText();
            Editable e = expires.getText();
            String date = Helper.formatDate(needed.getText().toString(), "MM/dd/yyyy");
            String followupDate = Helper.formatDate(f == null ? "" : f.toString(), "MM/dd/yyyy");
            String expiresDate = Helper.formatDate(e == null ? "" : e.toString(), "MM/dd/yyyy");

            //TODO validation against text
            String note = ((TextView) findViewById(R.id.reservationNote)).getText().toString();
            int QTRentalTypeID = ((RadioButton) findViewById(R.id.quote)).isChecked() ? 1 : 2;
            
            LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
            params.put("sCorpCode", corpCode);
            params.put("sLocationCode", locationCode);
            params.put("sCorpUserName", userName);
            params.put("sCorpPassword", password);
            params.put("sTenantID", mTenantId);
            params.put("sUnitID1", mUnitId);
            params.put("sUnitID2", "");
            params.put("sUnitID3", "");
            params.put("dNeeded", date);
            params.put("sComment", note);
            params.put("iSource", 5); //website
            params.put("sSource", getString(R.string.app_name));
            params.put("QTRentalTypeID", QTRentalTypeID);
            params.put("iInquiryType", 0);
            params.put("dcQuotedRate", ((EditText) findViewById(R.id.quotedRate)).getText());
            params.put("dExpires", expiresDate);
            params.put("dFollowUp", followupDate);
            params.put("sTrackingCode", ((EditText) findViewById(R.id.trackingCode)).getText());
            params.put("sCallerID", ((EditText) findViewById(R.id.callerId)).getText());
            params.put("ConcessionID", -999);

            mDataSet = ServerStuff.callSoapMethod("ReservationNewWithSource_v5", params);
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
