package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.dataStructures.DataSet;
import citruscups.com.sitelinkmobile.dataStructures.DataTable;
import citruscups.com.sitelinkmobile.helper.Helper;
import citruscups.com.sitelinkmobile.server.ServerStuff;

/**
 * Created by Michael on 8/21/2014.
 */
public class PaymentConfirmActivity extends Activity {

    private SharedPreferences mSharedPreferences;
    private String mTenantID, mUnitID, mCCNumber, mCVV2, mBillingName, mBillingAddress, mBillingPostalCode, mCCExp;
    private double mPmtAmt;
    private int mCCProvider;
    private ProgressDialog mProgressBar;
    private DataSet mDataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment_confirm);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);

        unpackIntent();

        TextView authorize = (TextView) findViewById(R.id.payment_authorize_TextView);
        authorize.setText("I authorize 'Site Name' to immediately charge the credit/debit card ending in ****" + mCCNumber.substring(mCCNumber.length()-4) + " for " + String.format("$%.2f", mPmtAmt));

        // Access the Buttons from XML
        Button buttonPayment = (Button) findViewById(R.id.payment_submit);

        buttonPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MakePayment().execute();
            }
        });
    }

    private void goToNav() {
        Intent intent = new Intent(PaymentConfirmActivity.this, NavigationActivity.class);
        startActivity(intent);
    }

    private void unpackIntent() {
        mTenantID = getIntent().getStringExtra("TenantID");
        mUnitID = getIntent().getStringExtra("UnitID");
        mCCExp = getIntent().getStringExtra("dCCExp");
        mCCNumber = getIntent().getStringExtra("sCCNumber");
        mCVV2 = getIntent().getStringExtra("sCVV2");
        mPmtAmt = getIntent().getDoubleExtra("pmtAmt", 0);
        mBillingName = getIntent().getStringExtra("sBillingName");
        mBillingAddress = getIntent().getStringExtra("sBillingAddress");
        mBillingPostalCode = getIntent().getStringExtra("sBillingPostalCode");
        mCCProvider = getIntent().getIntExtra("CCProvider", 0);

        TextView amtToPay = (TextView) findViewById(R.id.payment_amtToPay);
        TextView pmtDate = (TextView) findViewById(R.id.payment_pmtDate);
        TextView pmtMethod = (TextView) findViewById(R.id.payment_pmtMethod);

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String currentDate = formatter.format(new Date());

        amtToPay.setText(String.format("$%.2f", mPmtAmt));
        pmtDate.setText(currentDate);
        pmtMethod.setText("****" + mCCNumber.substring(mCCNumber.length()-4));
    }

    private class MakePayment extends AsyncTask<Void, Void, Void>
    {
        private DataTable dataTable;
        private int retCode;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            mProgressBar = new ProgressDialog(PaymentConfirmActivity.this);
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
            final boolean testMode = true;


            LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
            params.put("sCorpCode", corpCode);
            params.put("sLocationCode", locationCode);
            params.put("sCorpUserName", userName);
            params.put("sCorpPassword", password);
            params.put("iTenantID", Integer.parseInt(mTenantID));
            params.put("iUnitID", Integer.parseInt(mUnitID));
            params.put("dcPaymentAmount", mPmtAmt);
            params.put("iCreditCardType", mCCProvider);
            params.put("sCreditCardNumber", mCCNumber);
            params.put("sCreditCardCVV", mCVV2);
            params.put("dExpirationDate", mCCExp);
            params.put("sBillingName", mBillingName);
            params.put("sBillingAddress", mBillingAddress);
            params.put("sBillingZipCode", mBillingPostalCode);
            params.put("bTestMode", testMode);
            params.put("iSource", 10);
            mDataSet = ServerStuff.callSoapMethod("PaymentSimpleWithSource", params);
            if (mDataSet != null)
            {
                dataTable = mDataSet.getTableByName("RT");
                retCode = Helper.getRtValue(dataTable);
                if (retCode == 1 ) {
                    // The payment was successful!
                    Toast.makeText(getApplicationContext(), "Payment Successful!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Payment Failed! Return code: " + retCode, Toast.LENGTH_LONG).show();
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

            //if (retCode == 1) goToNav();

            finish();

        }
    }
}
