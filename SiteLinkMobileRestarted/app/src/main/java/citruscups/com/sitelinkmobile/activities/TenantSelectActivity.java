package citruscups.com.sitelinkmobile.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import citruscups.com.sitelinkmobile.R;

public class TenantSelectActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell the activity which XML layout is right
        setContentView(R.layout.activity_tenantselect);

        // Access the Buttons from XML
        Button buttonNewTenant = (Button) findViewById(R.id.newTenant);
        Button buttonLookupTenant = (Button) findViewById(R.id.lookupTenant);
    }
}
