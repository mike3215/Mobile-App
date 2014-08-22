package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.dataStructures.DataSet;
import citruscups.com.sitelinkmobile.dataStructures.DataTable;
import citruscups.com.sitelinkmobile.server.ServerStuff;

/**
 * Created by Michael on 8/14/2014.
 */
public class PaymentActivity extends Activity {

    private DataTable mDataTable;
    private DataSet mDataSet;
    private int mLedgerID;
    private String mTenantID;
    private String mUnitID;
    private ProgressDialog mProgressBar;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);

        if (getIntent().hasExtra("acctBalTable")) {
            mDataTable = (DataTable) getIntent().getExtras().get("acctBalTable");
        }

        if (getIntent().hasExtra("LedgerID")) {
            mLedgerID = getIntent().getIntExtra("LedgerID", -999);
        }

        if (getIntent().hasExtra("TenantID")) {
            mTenantID = getIntent().getStringExtra("TenantID");
        }

        if (getIntent().hasExtra("UnitID")) {
            mUnitID = getIntent().getStringExtra("UnitID");
        }

        // Access the Buttons from XML
        Button buttonPayment = (Button) findViewById(R.id.btnPaymentContinue);

        buttonPayment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                double pmtAmt;
                if (((RadioButton) findViewById(R.id.radio_TotalAmt)).isChecked()) {
                    pmtAmt = Double.parseDouble(((TextView) findViewById(R.id.paymentTotalAmtDue)).getText().toString().substring(1));
                } else if (((RadioButton) findViewById(R.id.radio_OtherAmt)).isChecked()) {
                    pmtAmt = Double.parseDouble(((TextView) findViewById(R.id.paymentOtherAmt)).getText().toString());
                } else {
                    // TODO: make toast indicating the need to select a payment option
                    return;
                }

                // Create an Intent to take us over to a new CreditCardInfoActivity
                Intent intent = new Intent(PaymentActivity.this, CreditCardInfoActivity.class);
                intent.putExtra("TenantID", mTenantID);
                intent.putExtra("UnitID", mUnitID);
                intent.putExtra("pmtAmt", pmtAmt);
                startActivity(intent);
            }
        });

        new GetLedgerInfo().execute();
    }

    private void updateGUI(DataTable dataTable) {
        TextView currentBill = (TextView) findViewById(R.id.paymentCurrentBillAmt);
        TextView paidThruDate = (TextView) findViewById(R.id.paymentPaidThruDate);
        TextView totalDue = (TextView) findViewById(R.id.paymentTotalAmtDue);

        ArrayList<Map<String, Object>> rows = dataTable.getRows();
        for (Map<String, Object> row : rows)
        {
            if (Integer.parseInt((String) row.get("LedgerID")) == mLedgerID) {
                String dateString = (String) row.get("dPaidThru");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                Date convertedDate = new Date();
                try {
                    convertedDate = dateFormat.parse(dateString);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                currentBill.setText(String.format("$%.2f", Double.parseDouble((String) row.get("dcChargeBalance"))));
                totalDue.setText(String.format("$%.2f", Double.parseDouble((String) row.get("dcTotalDue"))));
                paidThruDate.setText(formatter.format(convertedDate));

                if (new Date().after(convertedDate)) {
                    paidThruDate.setTextColor(Color.RED);
                }
            }
        }

    }

    private class GetLedgerInfo extends AsyncTask<Void, Void, Void>
    {
        private DataTable dataTable;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            mProgressBar = new ProgressDialog(PaymentActivity.this);
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

            LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
            params.put("sCorpCode", corpCode);
            params.put("sLocationCode", locationCode);
            params.put("sCorpUserName", userName);
            params.put("sCorpPassword", password);
            params.put("sTenantID", TenantID);
            mDataSet = ServerStuff.callSoapMethod("LedgersByTenantID", params);
            if (mDataSet != null)
            {
                dataTable = mDataSet.getTableByName("Ledgers");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPreExecute();

            if (mProgressBar.isShowing())
                mProgressBar.dismiss();

            updateGUI(dataTable);

        }
    }
}
