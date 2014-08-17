package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.dataStructures.DataTable;

/**
 * Created by Michael on 8/14/2014.
 */
public class PaymentActivity extends Activity {

    private DataTable mDataTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell the activity which XML layout is right
        setContentView(R.layout.activity_payment);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // Access the Buttons from XML
        Button buttonPayment = (Button) findViewById(R.id.btnPaymentContinue);

        if (getIntent().hasExtra("acctBalTable")) {
            mDataTable = (DataTable) getIntent().getExtras().get("acctBalTable");
            Log.i("PaymentActivity", mDataTable.toString());;
        }

        buttonPayment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                /*
                // Create an Intent to take us over to a new TenantSelectActivity
                Intent TenantLookupActivity = new Intent(PaymentActivity.this, "Not Created Yet".class);
                TenantLookupActivity.putExtra("UsedFor", citruscups.com.sitelinkmobile.activities.TenantLookupActivity.TenantLookupUsedFor.TenantLookup);
                startActivity(TenantLookupActivity);
                */
            }
        });
    }
}
