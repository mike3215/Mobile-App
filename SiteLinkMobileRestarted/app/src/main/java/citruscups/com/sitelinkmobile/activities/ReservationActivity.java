package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import java.util.Map;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.dataStructures.DataSet;
import citruscups.com.sitelinkmobile.helper.Helper;
import citruscups.com.sitelinkmobile.server.ServerStuff;

public class ReservationActivity extends Activity
{
    private final static int CALLBACK_RESULT = 1;
    private int mTenantId;
    private int mUnitId;
    private int mWaitingId;
    private int mConcessionId = -999;
    private HashMap<String, Object> mTenantMap;
    private HashMap<String, Object> mUnitMap;
    private HashMap<String, Object> mWaitingMap;
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
        if (extras != null)
        {
            mTenantId = extras.containsKey("TenantID") ? Integer.parseInt(extras.get("TenantID").toString()) : 0;
            mUnitId = extras.containsKey("UnitID") ? Integer.parseInt(extras.get("UnitID").toString()) : 0;
            mWaitingId = extras.containsKey("WaitingID") ? Integer.parseInt(extras.get("WaitingID").toString()) : 0;
            mTenantMap = extras.containsKey("TenantMap") ? (HashMap<String, Object>) extras.get("TenantMap") : null;
            mUnitMap = extras.containsKey("UnitMap") ? (HashMap<String, Object>) extras.get("UnitMap") : null;
            mWaitingMap = extras.containsKey("WaitingMap") ? (HashMap<String, Object>) extras.get("WaitingMap") : null;

            fillFields();
        }

        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);


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

        Button addDiscount = (Button) findViewById(R.id.addDiscount);
        addDiscount.setOnClickListener(new View.OnClickListener()
                                       {
                                           @Override
                                           public void onClick(View v)
                                           {
                                               Intent intent = new Intent(ReservationActivity.this, DiscountLookupActivity.class);
                                               intent.putExtra("UnitID", mUnitId);
                                               intent.putExtra("UnitMap", mUnitMap);

                                               startActivityForResult(intent, CALLBACK_RESULT);
                                           }
                                       }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CALLBACK_RESULT)
        {
            if (resultCode == RESULT_OK)
            {
                mConcessionId = Integer.valueOf(data.getExtras().get("ConcessionID").toString());
                Map<String, Object> discountMap = (Map<String, Object>) data.getExtras().get("DiscountMap");
                String discountPlan = discountMap.get("sPlanName").toString();
                TextView discount = (TextView) findViewById(R.id.discount);
                discount.setText(discountPlan);
            }
        }
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
        if (mTenantMap != null)
        {
            TextView tenantName = (TextView) findViewById(R.id.tenantName);
            tenantName.setText(mTenantMap.get("sFName") + " " + mTenantMap.get("sLName"));
        }
        if (mUnitMap != null && mUnitMap.size() > 0) //empty if edit
        {
            TextView unitName = (TextView) findViewById(R.id.unitName);
            unitName.setText(mUnitMap.get("sUnitName").toString());

            try
            {
                final Double w = Double.parseDouble(mUnitMap.get("dcWidth").toString());
                final Double l = Double.parseDouble(mUnitMap.get("dcLength").toString());
                DecimalFormat format = new DecimalFormat("###,##0.00");

                TextView unitSize = (TextView) findViewById(R.id.unitSize);
                unitSize.setText(format.format(w) + "X" + format.format(l));

                TextView standardRate = (TextView) findViewById(R.id.stdRate);
                final Double sr = Double.parseDouble(mUnitMap.get("dcStdRate").toString());
                standardRate.setText(format.format(sr));
            }
            catch (NumberFormatException nfe)
            {
            }

        }

        if (mWaitingMap != null)
        {
            if (mWaitingMap.containsKey("dcRate_Quoted"))
            {
                TextView quotedRate = (TextView) findViewById(R.id.quotedRate);
                quotedRate.setText(mWaitingMap.get("dcRate_Quoted").toString());
            }
            if (mWaitingMap.containsKey("dNeeded"))
            {
                TextView needed = (TextView) findViewById(R.id.neededDate);
                needed.setText(Helper.formatDisplayDate(mWaitingMap.get("dNeeded").toString(), "yyyy-MM-dd"));
            }
            if (mWaitingMap.containsKey("dFollowup"))
            {
                TextView followup = (TextView) findViewById(R.id.followupDate);
                followup.setText(Helper.formatDisplayDate(mWaitingMap.get("dFollowup").toString(), "yyyy-MM-dd"));
            }
            if (mWaitingMap.containsKey("dExpires"))
            {
                TextView expires = (TextView) findViewById(R.id.expiresDate);
                expires.setText(Helper.formatDisplayDate(mWaitingMap.get("dExpires").toString(), "yyyy-MM-dd"));
            }
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

            EditText needed = (EditText) findViewById(R.id.neededDate);
            EditText followup = (EditText) findViewById(R.id.followupDate);
            EditText expires = (EditText) findViewById(R.id.expiresDate);
            Editable f = followup.getText();
            Editable e = expires.getText();
            String date = Helper.formatDate(needed.getText().toString(), "MM/dd/yyyy");
            String followupDate = f.toString().equalsIgnoreCase("") ? "1900-01-01T00:00:00" : Helper.formatDate(f.toString(), "MM/dd/yyyy");
            String expiresDate = e.toString().equalsIgnoreCase("") ? "1900-01-01T00:00:00" : Helper.formatDate(e.toString(), "MM/dd/yyyy");

            //TODO validation against text
            String note = ((TextView) findViewById(R.id.reservationNote)).getText().toString();
            int QTRentalTypeID = ((RadioButton) findViewById(R.id.quote)).isChecked() ? 1 : 2;
            
            LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
            params.put("sCorpCode", corpCode);
            params.put("sLocationCode", locationCode);
            params.put("sCorpUserName", userName);
            params.put("sCorpPassword", password);
            params.put("sTenantID", mTenantId);
            params.put("sUnitID", mUnitId);
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
            params.put("ConcessionID", mConcessionId);

            mDataSet = ServerStuff.callSoapMethod("ReservationNewWithSource_v5", params);
            if (mDataSet != null)
            {
                int retCode = Helper.getRtValue(mDataSet);
                if (retCode > 0)
                {
                    waitingId = retCode;
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
                finish();
            }

        }
    }
}
