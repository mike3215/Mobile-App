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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.helper.CreditCardValidator;

/**
 * Created by Michael on 8/19/2014.
 */
public class CreditCardInfoActivity extends Activity {

    private Spinner monthSpinner, yearSpinner;
    private ImageView ccImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_creditcard_info);

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
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, Integer.parseInt(String.valueOf(yearSpinner.getSelectedItem())));
                cal.set(Calendar.MONTH, monthSpinner.getSelectedItemPosition());
                Date date = cal.getTime();

                Toast.makeText(CreditCardInfoActivity.this, "Selected Expiry Date : " + date.toString(),
                                Toast.LENGTH_SHORT).show();
                /*
                // Create an Intent to take us over to a new PaymentReviewActivity
                Intent CreditCardInfoActivity = new Intent(CreditCardInfoActivity.this, PaymentReviewActivity.class);
                startActivity(CreditCardInfoActivity);
                */
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
                                break;
                            case 2:
                                ccImage.setImageResource(R.drawable.amex);
                                break;
                            case 3:
                                ccImage.setImageResource(R.drawable.discover);
                                break;
                            case 4:
                                ccImage.setImageResource(R.drawable.mastercard);
                                break;
                            case 5:
                                ccImage.setImageResource(R.drawable.diners);
                                break;
                        }
                    }
                }
            }
        });
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

    private boolean validateCCNumber(String creditCard) {
        int sum = 0;
        int length = creditCard.length();
        for (int i = 0; i < creditCard.length(); i++) {
            if (0 == (i % 2)) {
                sum += creditCard.charAt(length - i - 1) - '0';
            } else {
                sum += sumDigits((creditCard.charAt(length - i - 1) - '0') * 2);
            }
        }
        return 0 == (sum % 10);
    }

    private int sumDigits(int i) {
        return (i % 10) + (i / 10);
    }
}
