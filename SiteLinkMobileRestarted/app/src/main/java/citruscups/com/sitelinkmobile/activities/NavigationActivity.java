package citruscups.com.sitelinkmobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.helper.Constants;

/**
 * Created by Michael on 8/9/2014.
 */
public class NavigationActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell the activity which XML layout is right
        setContentView(R.layout.activity_navigation);

        // Access the Buttons from XML
        Button buttonPayment = (Button) findViewById(R.id.btnNavPayment);
        Button buttonMoveIn = (Button) findViewById(R.id.btnNavMoveIn);
        Button buttonInqRes = (Button) findViewById(R.id.btnNavInqRes);
        Button buttonTenant = (Button) findViewById(R.id.btnNavTenants);

        buttonTenant.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Create an Intent to take us over to a new TenantSelectActivity
                Intent TenantLookupActivity = new Intent(NavigationActivity.this, TenantLookupActivity.class);
                TenantLookupActivity.putExtra("UsedFor", Constants.UsedFor.TenantLookup);
                startActivity(TenantLookupActivity);
            }
        });

        buttonPayment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Create an Intent to take us over to a new TenantSelectActivity
                Intent TenantLookupActivity = new Intent(NavigationActivity.this, TenantLookupActivity.class);
                TenantLookupActivity.putExtra("UsedFor", Constants.UsedFor.Payment);
                startActivity(TenantLookupActivity);
            }
        });

        buttonInqRes.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Create an Intent to take us over to a new TenantSelectActivity
                Intent ReservationLookupActivity = new Intent(NavigationActivity.this, ReservationLookupActivity.class);
                ReservationLookupActivity.putExtra("UsedFor", Constants.UsedFor.InqRes);
                startActivity(ReservationLookupActivity);
            }
        });

        buttonMoveIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Create an Intent to take us over to a new TenantSelectActivity
                Intent unitLookupActivity = new Intent(NavigationActivity.this, UnitLookupActivity.class);
                unitLookupActivity.putExtra("UsedFor", Constants.UsedFor.MoveIn);
                startActivity(unitLookupActivity);
            }
        });
    }

    @Override
    public void onBackPressed() {
        //Disable the back button from this screen
    }
}
