package citruscups.com.sitelinkmobile.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
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
import android.widget.Toast;

import java.util.Hashtable;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.DataStructures.DataSet;
import citruscups.com.sitelinkmobile.helper.Helper;
import citruscups.com.sitelinkmobile.server.ServerStuff;

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

        private String mMessage;

        UserLoginTask(String corpCode, String locationCode, String username, String password) {
            mCorpCode = corpCode;
            mLocationCode = locationCode;
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... param)
        {
            Hashtable<String, Object> params = new Hashtable<String, Object>();
            params.put("sCorpCode", mCorpCode);
            params.put("sLocationCode", mLocationCode);
            params.put("sCorpUserName", mUsername);
            params.put("sCorpPassword", mPassword);

            DataSet ds = ServerStuff.callSoapMethod("SiteInformation", params);
            if (ds == null) return false;
            //for SiteInformation we don't want an RT table

            int retCode = Helper.getRtValue(ds);
            if (retCode == 0)
            {
                mMessage = "Login successful";
                return true;
            }
            else
            {
                //error
                mMessage = "Call return value: " + Helper.getMessageFromRetCode(retCode);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            Log.i("Ret", mMessage);
            if(success) {
                // Create an Intent to take us over to a new TenantSelectActivity
                Intent NavigationActivity = new Intent(LoginActivity.this, NavigationActivity.class);

                // TODO: pack away the data from the SiteInformation call
                //tenantSelectActivity.putExtra()

                startActivity(NavigationActivity);


            } else {
                //mUsernameView.setError(getString(R.string.error_incorrect_username));
                //mPasswordView.setError((getString(R.string.error_incorrect_password)));

                Toast.makeText(getApplicationContext(), getString(R.string.error_incorrect_userPass), Toast.LENGTH_LONG).show();
                mUsernameView.selectAll();
                mUsernameView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
