package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.dataStructures.DataSet;
import citruscups.com.sitelinkmobile.dataStructures.DataTable;
import citruscups.com.sitelinkmobile.helper.Helper;
import citruscups.com.sitelinkmobile.server.ServerStuff;

public class NewTenantActivity extends Activity
{
    private boolean mNameFieldDirty = false;
    private boolean mNameFieldIgnoreEvents = false;
    private SharedPreferences mSharedPreferences;
    private int TenantId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tenant);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        mSharedPreferences = getSharedPreferences("citruscups.com.sitelinkmobile", MODE_PRIVATE);

        EditText name = (EditText) findViewById(R.id.name);
        name.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (mNameFieldIgnoreEvents) return;

                mNameFieldDirty = true;
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        ImageButton expand = (ImageButton) findViewById(R.id.expand_name);
        expand.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                EditText name = (EditText) findViewById(R.id.name);
                EditText firstName = (EditText) findViewById(R.id.firstName);
                EditText middleName = (EditText) findViewById(R.id.middleName);
                EditText lastName = (EditText) findViewById(R.id.lastName);

                RelativeLayout expandedView = (RelativeLayout) findViewById(R.id.expandedView);
                if (expandedView.getVisibility() == View.VISIBLE)
                {
                    expandedView.setVisibility(View.GONE);
                    final String first = firstName.getText().toString().trim();
                    final String middle = middleName.getText().toString().trim();
                    final String last = lastName.getText().toString().trim();

                    Collection<String> buffer = new ArrayList<String>();
                    if (first.length() > 0) buffer.add(first);
                    if (middle.length() > 0) buffer.add(middle);
                    if (last.length() > 0) buffer.add(last);
                    mNameFieldIgnoreEvents = true;
                    name.setText(TextUtils.join(" ", buffer));
                    mNameFieldIgnoreEvents = false;
                }
                else
                {
                    expandedView.setVisibility(View.VISIBLE);
                    if (!mNameFieldDirty) return;

                    HashMap<String, String> names = displayNameToNameParts(name.getText().toString());
                    firstName.setText(names.get("firstName"));
                    middleName.setText(names.get("middleName"));
                    lastName.setText(names.get("lastName"));


                    mNameFieldIgnoreEvents = false;
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_tenant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_cancel)
        {
            finish();
            return true;
        }
        else if (id == R.id.action_save)
        {
            if (mNameFieldDirty)
            {
                HashMap<String, String> names = displayNameToNameParts(((EditText) findViewById(R.id.name)).getText().toString());
                ((EditText) findViewById(R.id.firstName)).setText(names.get("firstName"));
                ((EditText) findViewById(R.id.middleName)).setText(names.get("middleName"));
                ((EditText) findViewById(R.id.lastName)).setText(names.get("lastName"));
            }
            new SaveTenant().execute();
        }

        return super.onOptionsItemSelected(item);
    }

    private HashMap<String, String> displayNameToNameParts(String displayName)
    {
        HashMap<String, String> nameMap = new HashMap<String, String>();

        String nameParts[] = displayName.split(" ");
        switch (nameParts.length)
        {
            case 0:
                nameMap.put("firstName", "");
                nameMap.put("middleName", "");
                nameMap.put("lastName", "");
                break;
            case 1:
                nameMap.put("firstName", nameParts[0]);
                nameMap.put("middleName", "");
                nameMap.put("lastName", "");
                break;
            case 2:
                nameMap.put("firstName", nameParts[0]);
                nameMap.put("middleName", "");
                nameMap.put("lastName", nameParts[1]);
                break;
            case 3:
                nameMap.put("firstName", nameParts[0]);
                nameMap.put("middleName", nameParts[1]);
                nameMap.put("lastName", nameParts[2]);
                break;
        }

        return nameMap;
    }

    private class SaveTenant extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... arg0)
        {
            String firstName = ((EditText) findViewById(R.id.firstName)).getText().toString();
            String middleName = ((EditText) findViewById(R.id.middleName)).getText().toString();
            String lastName = ((EditText) findViewById(R.id.lastName)).getText().toString();
            String company = ((EditText) findViewById(R.id.company)).getText().toString();
            String phone = ((EditText) findViewById(R.id.phone)).getText().toString();
            String email = ((EditText) findViewById(R.id.email)).getText().toString();
            String address1 = ((EditText) findViewById(R.id.address1)).getText().toString();
            String address2 = ((EditText) findViewById(R.id.address2)).getText().toString();
            String city = ((EditText) findViewById(R.id.city)).getText().toString();
            String state = ((EditText) findViewById(R.id.state)).getText().toString();
            String postalCode = ((EditText) findViewById(R.id.postalCode)).getText().toString();

            final String corpCode = mSharedPreferences.getString("CorpCode", "DEMO");
            final String locationCode = mSharedPreferences.getString("LocationCode", "DEMO");
            final String userName = mSharedPreferences.getString("UserName", "DEMO");
            final String password = mSharedPreferences.getString("Password", "DEMO");

            LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

            parameters.put("sCorpCode", corpCode);
            parameters.put("sLocationCode", locationCode);
            parameters.put("sCorpUserName", userName);
            parameters.put("sCorpPassword", password);
            parameters.put("sWebPassword", "");
            parameters.put("sMrMrs", "");
            parameters.put("sFName", firstName);
            parameters.put("sMI", middleName);
            parameters.put("sLName", lastName);
            parameters.put("sCompany", company);
            parameters.put("sAddr1", address1);
            parameters.put("sAddr2", address2);
            parameters.put("sCity", city);
            parameters.put("sRegion", state);
            parameters.put("sPostalCode", postalCode);
            parameters.put("sCountry", "");
            parameters.put("sPhone", phone);
            parameters.put("sMrMrsAlt", "");
            parameters.put("sFNameAlt", "");
            parameters.put("sMIAlt", "");
            parameters.put("sLNameAlt", "");
            parameters.put("sAddr1Alt", "");
            parameters.put("sAddr2Alt", "");
            parameters.put("sCityAlt", "");
            parameters.put("sRegionAlt", "");
            parameters.put("sPostalCodeAlt", "");
            parameters.put("sCountryAlt", "");
            parameters.put("sPhoneAlt", "");
            parameters.put("sMrMrsBus", "");
            parameters.put("sFNameBus", "");
            parameters.put("sMIBus", "");
            parameters.put("sLNameBus", "");
            parameters.put("sCompanyBus", "");
            parameters.put("sAddr1Bus", "");
            parameters.put("sAddr2Bus", "");
            parameters.put("sCityBus", "");
            parameters.put("sRegionBus", "");
            parameters.put("sPostalCodeBus", "");
            parameters.put("sCountryBus", "");
            parameters.put("sPhoneBus", "");
            parameters.put("sFax", "");
            parameters.put("sEmail", email);
            parameters.put("sPager", "");
            parameters.put("sMobile", "");
            parameters.put("bCommercial", false);
            parameters.put("bCompanyIsTenant", false);
            parameters.put("dDOB", "1975-01-01T00:00:00\n");
            parameters.put("sTenNote", "");
            parameters.put("sLicense", "");
            parameters.put("sLicRegion", "");
            parameters.put("sSSN", "");

            DataSet result = ServerStuff.callSoapMethod("TenantNewDetailed", parameters);
            if (result != null)
            {
                DataTable rtTable = result.getTableByName("RT");
                if (rtTable != null)
                {
                    int retCode = Helper.getRtValue(rtTable);
                    if (retCode == 1)
                    {
                        ArrayList<Map<String, Object>> rows = rtTable.getRows();
                        for (Map<String, Object> row : rows)
                        {
                            if (row.containsKey("TenantID"))
                            {
                                TenantId = Integer.parseInt(row.get("TenantID").toString());

                                finish();
                                return null;
                            }
                        }
                    }
                }
            }

            return null;
        }
    }
}
