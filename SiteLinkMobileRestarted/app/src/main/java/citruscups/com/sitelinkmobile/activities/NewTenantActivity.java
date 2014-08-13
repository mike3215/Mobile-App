package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

import citruscups.com.sitelinkmobile.R;

public class NewTenantActivity extends Activity
{
    private boolean mNameFieldDirty = false;
    private boolean mNameFieldIgnoreEvents = false;

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
                TextView name = (TextView) findViewById(R.id.name);
                TextView firstName = (TextView) findViewById(R.id.firstName);
                TextView middleName = (TextView) findViewById(R.id.middleName);
                TextView lastName = (TextView) findViewById(R.id.lastName);

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

                    String nameParts[] = name.getText().toString().split(" ");
                    switch (nameParts.length)
                    {
                        case 0:
                            firstName.setText("");
                            middleName.setText("");
                            lastName.setText("");
                            break;
                        case 1:
                            firstName.setText(nameParts[0]);
                            middleName.setText("");
                            lastName.setText("");
                            break;
                        case 2:
                            firstName.setText(nameParts[0]);
                            middleName.setText("");
                            lastName.setText(nameParts[1]);
                            break;
                        case 3:
                            firstName.setText(nameParts[0]);
                            middleName.setText(nameParts[1]);
                            lastName.setText(nameParts[2]);
                            break;
                    }

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
            Toast.makeText(NewTenantActivity.this, "Save", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
