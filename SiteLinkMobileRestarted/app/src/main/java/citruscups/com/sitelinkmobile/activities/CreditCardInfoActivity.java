package citruscups.com.sitelinkmobile.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.helper.CreditCardValidator;

/**
 * Created by Michael on 8/19/2014.
 */
public class CreditCardInfoActivity extends Activity {

    private Spinner monthSpinner, yearSpinner;
    private ImageView ccImage;
    private String mUnitID, mTenantID;
    private double mPmtAmt;
    private int mCCProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_creditcard_info);

        if (getIntent().hasExtra("UnitID")) mUnitID = getIntent().getStringExtra("UnitID");
        if (getIntent().hasExtra("TenantID")) mTenantID = getIntent().getStringExtra("TenantID");
        if (getIntent().hasExtra("pmtAmt")) mPmtAmt = getIntent().getDoubleExtra("pmtAmt", 0);

        monthSpinner = (Spinner) findViewById(R.id.month_spinner);
        yearSpinner = (Spinner) findViewById(R.id.year_spinner);

        ccImage = (ImageView) findViewById(R.id.cc_imageview);
        ccImage.setImageResource(R.drawable.credit);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        addItemsOnYearSpinner();

        // Access the Buttons from XML
        Button buttonContinue = (Button) findViewById(R.id.billing_continue_btn);
        final EditText CCNumber = (EditText) findViewById(R.id.billing_cardNumber);

        buttonContinue.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!validateInputs()) return;

                // Create an Intent to take us over to a new PaymentReviewActivity
                Intent paymentConfirmActivity = new Intent(CreditCardInfoActivity.this, PaymentConfirmActivity.class);

                packIntent(paymentConfirmActivity);

                startActivity(paymentConfirmActivity);
            }
        });

        CCNumber.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //TODO: Run card through Luhn algorithm
            }
        });

        CCNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    CreditCardValidator v = new CreditCardValidator();

                    if (!v.isValid(CCNumber.getText().toString())) {
                        ccImage.setImageResource(R.drawable.credit);
                        CCNumber.setError("Invalid Credit Card number");
                    } else {
                        int ccProviderID = v.getCardProvider(CCNumber.getText().toString());

                        switch (ccProviderID) {
                            case 1:
                                ccImage.setImageResource(R.drawable.visa);
                                mCCProvider = 6;
                                break;
                            case 2:
                                ccImage.setImageResource(R.drawable.amex);
                                mCCProvider = 7;
                                break;
                            case 3:
                                ccImage.setImageResource(R.drawable.discover);
                                mCCProvider = 8;
                                break;
                            case 4:
                                ccImage.setImageResource(R.drawable.mastercard);
                                mCCProvider = 5;
                                break;
                            case 5:
                                ccImage.setImageResource(R.drawable.diners);
                                mCCProvider = 9;
                                break;
                        }
                    }
                }
            }
        });
    }

    private void packIntent(Intent intent) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(String.valueOf(yearSpinner.getSelectedItem())));
        cal.set(Calendar.MONTH, monthSpinner.getSelectedItemPosition());
        Date date = cal.getTime();

        intent.putExtra("dCCExp", sdf.format(date));
        intent.putExtra("sCCNumber", ((EditText) findViewById(R.id.billing_cardNumber)).getText().toString());
        intent.putExtra("sCVV2", ((EditText) findViewById(R.id.cvv2)).getText().toString());
        intent.putExtra("UnitID", mUnitID);
        intent.putExtra("TenantID", mTenantID);
        intent.putExtra("pmtAmt", mPmtAmt);
        intent.putExtra("sBillingName", ((EditText) findViewById(R.id.cardholders_name)).getText().toString());
        intent.putExtra("sBillingAddress", ((EditText) findViewById(R.id.billing_address)).getText().toString());
        intent.putExtra("sBillingPostalCode", ((EditText) findViewById(R.id.billing_postal_code)).getText().toString());
        intent.putExtra("CCProvider", mCCProvider);
    }

    private boolean validateInputs() {
        CreditCardValidator v = new CreditCardValidator();
        Boolean validInputs = true;

        if (!v.isValid(((EditText) findViewById(R.id.billing_cardNumber)).getText().toString())) {
            validInputs = false;
            ((EditText) findViewById(R.id.billing_cardNumber)).setError("Invalid Credit Card number");
        }

        if (((EditText) findViewById(R.id.cvv2)).getText().toString().length() < 3) {
            validInputs = false;
            ((EditText) findViewById(R.id.cvv2)).setError(getString(R.string.error_field_required));
        }

        if (((EditText) findViewById(R.id.billing_address)).getText().toString().length() == 0) {
            validInputs = false;
            ((EditText) findViewById(R.id.billing_address)).setError(getString(R.string.error_field_required));
        }

        if (((EditText) findViewById(R.id.cardholders_name)).getText().toString().length() == 0) {
            validInputs = false;
            ((EditText) findViewById(R.id.cardholders_name)).setError(getString(R.string.error_field_required));
        }

        if (((EditText) findViewById(R.id.billing_postal_code)).getText().toString().length() < 3) {
            validInputs = false;
            ((EditText) findViewById(R.id.billing_postal_code)).setError(getString(R.string.error_field_required));
        }

        return validInputs;
    }

    private void addItemsOnYearSpinner() {
        Calendar c = Calendar.getInstance();
        List list = new ArrayList();
        list.add(c.get(Calendar.YEAR) + "");
        for (int i = 0; i < 5; i++) {
            c.add(Calendar.YEAR, 1);
            list.add(c.get(Calendar.YEAR) + "");
        }

        ArrayAdapter dataAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(dataAdapter);
    }
}
