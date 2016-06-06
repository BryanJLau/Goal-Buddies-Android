package me.bryanlau.goalbuddiesandroid.Goals;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import me.bryanlau.goalbuddiesandroid.R;
import me.bryanlau.goalbuddiesandroid.Requests.AddGoalRequest;
import me.bryanlau.goalbuddiesandroid.Requests.RequestUtils;

public class AddGoalActivity extends AppCompatActivity {

    private LocalBroadcastManager broadcastManager;

    private BroadcastReceiver addGoalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            showProgress(false);

            int statusCode = extras.getInt("statusCode");
            if (RequestUtils.isOk(statusCode)) {
                Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.add_goal_success),
                        Toast.LENGTH_LONG
                ).show();
                finish();
            } else if (RequestUtils.isBad(statusCode)) {
                // Unauthorized, expired token most likely
                // For simplicity, just redirect to login screen
                // in case password was changed
                Toast.makeText(getApplicationContext(),
                        extras.getString("error"),
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);
        setTitle("Add Goal");

        final EditText descriptionEditText =
                (EditText) findViewById(R.id.add_goal_section_body_description);
        final EditText daysToFinishEditText =
                (EditText) findViewById(R.id.add_goal_section_body_days_to_finish);

        final Spinner typeSpinner = (Spinner) findViewById(R.id.add_goal_section_body_type);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.add_goal_type_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (typeSpinner != null)
            typeSpinner.setAdapter(typeAdapter);

        final Spinner iconSpinner = (Spinner) findViewById(R.id.add_goal_section_body_icon);
        ArrayAdapter<CharSequence> iconAdapter = ArrayAdapter.createFromResource(this,
                R.array.add_goal_icon_array, android.R.layout.simple_spinner_item);
        iconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (iconSpinner != null)
            iconSpinner.setAdapter(iconAdapter);

        Button submitButton = (Button) findViewById(R.id.add_goal_section_button_submit);
        if (submitButton != null)
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (daysToFinishEditText != null && descriptionEditText != null
                            && typeSpinner != null && iconSpinner != null) {
                        AddGoalRequest request = new AddGoalRequest(
                                getApplicationContext(),
                                descriptionEditText.getText().toString(),
                                typeSpinner.getSelectedItem().toString(),
                                iconSpinner.getSelectedItem().toString(),
                                daysToFinishEditText.getText().toString()
                        );
                        request.execute();

                        showProgress(true);
                    }
                }
            });

        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();

        broadcastManager.registerReceiver(
                addGoalBroadcastReceiver,
                RequestUtils.addGoalFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        broadcastManager.unregisterReceiver(addGoalBroadcastReceiver);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        final View addGoalForm = findViewById(R.id.add_goal_form);
        final View mProgressView = findViewById(R.id.add_goal_progress);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            addGoalForm.setVisibility(show ? View.GONE : View.VISIBLE);
            addGoalForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    addGoalForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            addGoalForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
