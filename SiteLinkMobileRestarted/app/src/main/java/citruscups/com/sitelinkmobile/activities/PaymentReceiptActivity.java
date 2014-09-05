package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import citruscups.com.sitelinkmobile.R;

/**
 * Created by Michael on 9/4/2014.
 */
public class PaymentReceiptActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment_receipt);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        unpackIntent();

        TextView paymentStatus = (TextView) findViewById(R.id.payment_status_TextView);
        paymentStatus.setText("Your payment has been accepted.");

        // Access the Buttons from XML
        Button buttonPayment = (Button) findViewById(R.id.payment_submit);

        buttonPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNav();
            }
        });
    }

    private void goToNav() {
        Intent intent = new Intent(PaymentReceiptActivity.this, NavigationActivity.class);
        startActivity(intent);
    }

    private void unpackIntent() {
        double PmtAmt = getIntent().getDoubleExtra("pmtAmt", 0);
        String CCNum = getIntent().getStringExtra("ccNum");
        int iReceiptNum = getIntent().getIntExtra("iReceiptNum", -999);

        TextView amtPaid = (TextView) findViewById(R.id.payment_amtPaid);
        TextView pmtDate = (TextView) findViewById(R.id.payment_rcptDate);
        TextView pmtMethod = (TextView) findViewById(R.id.payment_rcptPmtMethod);
        TextView pmtStatus = (TextView) findViewById(R.id.payment_status_TextView);
        TextView rcptNum = (TextView) findViewById(R.id.payment_rcptNum_TextView);

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String currentDate = formatter.format(new Date());

        amtPaid.setText(String.format("$%.2f", PmtAmt));
        pmtDate.setText(currentDate);
        pmtMethod.setText("****" + CCNum.substring(CCNum.length()-4));
        pmtStatus.setText("Your payment has been accepted");
        pmtStatus.setTextColor(Color.GREEN);
        rcptNum.setText("Your receipt number is: " + iReceiptNum);
    }
}
