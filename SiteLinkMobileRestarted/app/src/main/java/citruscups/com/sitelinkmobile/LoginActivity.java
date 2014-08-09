package citruscups.com.sitelinkmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {

    // Keep track of the login task to ensure we can cancel it if requested
    private UserLoginTask mAuthTask = null;

    // UI references
    private EditText mCorpCodeView;
    private EditText mLocCodeView;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form
        mCorpCodeView = (EditText) findViewById(R.id.corpCode);
        mLocCodeView = (EditText) findViewById(R.id.locCode);
        mUsernameView = (EditText) findViewById(R.id.corpUser);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mAPISignInButton = (Button) findViewById(R.id.api_sign_in_button);
        mAPISignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mCorpCodeView.setError(null);
        mLocCodeView.setError(null);
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt
        String corpCode = mCorpCodeView.getText().toString();
        String locCode = mLocCodeView.getText().toString();
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid location code.
        if (TextUtils.isEmpty(locCode)) {
            mLocCodeView.setError(getString(R.string.error_field_required));
            focusView = mLocCodeView;
            cancel = true;
        }

        // Check for a valid corp code.
        if (TextUtils.isEmpty(corpCode)) {
            mCorpCodeView.setError(getString(R.string.error_field_required));
            focusView = mCorpCodeView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(corpCode, locCode, username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String username) {
        //TODO: Add better username validation?
        return username.length() < 11;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Add better password validation?
        return password.length() > 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }


    /**
     * Represents an asynchronous login/registration task used to authenticate the user
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mCorpCode;
        private final String mLocationCode;
        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String corpCode, String locationCode, String username, String password) {
            mCorpCode = corpCode;
            mLocationCode = locationCode;
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            /**
             * Attempt authentication against SiteLink API
             */
            String NAMESPACE = "http://tempuri.org/CallCenterWs/CallCenterWs";
            String METHOD_NAME = "SiteInformation";
            String URL = "https://api.smdservers.net/CCWs_3.5/CallCenterWs.asmx?WSDL";
            String ACTION = "http://tempuri.org/CallCenterWs/CallCenterWs/SiteInformation";

            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope soapSerializationEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            soapSerializationEnvelope.setOutputSoapObject(Request);
            soapSerializationEnvelope.dotNet = true;
            soapSerializationEnvelope.setAddAdornments(false);
            soapSerializationEnvelope.implicitTypes = true;

/*
            PropertyInfo piCorpCode = new PropertyInfo();
            piCorpCode.setType(PropertyInfo.STRING_CLASS);
            piCorpCode.setName("sCorpCode");
            piCorpCode.setValue(mCorpCode);
            Request.addProperty(piCorpCode);

            PropertyInfo piLocationCode = new PropertyInfo();
            piLocationCode.setType(PropertyInfo.STRING_CLASS);
            piLocationCode.setName("sLocationCode");
            piLocationCode.setValue(mLocationCode);
            Request.addProperty(piLocationCode);

            PropertyInfo piUsername = new PropertyInfo();
            piUsername.setType(PropertyInfo.STRING_CLASS);
            piUsername.setName("sCorpUserName");
            piUsername.setValue(mUsername);
            Request.addProperty(piUsername);

            PropertyInfo piPassword = new PropertyInfo();
            piPassword.setType(PropertyInfo.STRING_CLASS);
            piPassword.setName("sCorpPassword");
            piPassword.setValue(mPassword);
            Request.addProperty(piPassword);*/

            Request.addProperty("sCorpCode", mCorpCode);
            Request.addProperty("sLocationCode", mLocationCode);
            Request.addProperty("sCorpUserName", mUsername);
            Request.addProperty("sCorpPassword", mPassword);

            try {
                HttpTransportSE httpTransportSE = new HttpTransportSE(URL);
                httpTransportSE.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

                httpTransportSE.debug = true;
                httpTransportSE.call(ACTION, soapSerializationEnvelope );
                SoapObject objectResult = (SoapObject)soapSerializationEnvelope.bodyIn;

                Log.d("SiteLink Mobile", objectResult.getProperty(0).toString());

                //Toast.makeText(getApplicationContext(), objectResult.toString(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if(success) {
                // Uncomment this block when the web service actually works.
                /*
                // Create an Intent to take us over to a new TenantSelectActivity
                Intent tenantSelectActivity = new Intent(getParent(), TenantSelectActivity.class);

                // TODO: pack away the data from the SiteInformation call
                //tenantSelectActivity.putExtra()

                startActivity(tenantSelectActivity);
                */
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
