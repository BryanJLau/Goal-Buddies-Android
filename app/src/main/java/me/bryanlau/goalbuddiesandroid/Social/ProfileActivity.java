package me.bryanlau.goalbuddiesandroid.Social;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import me.bryanlau.goalbuddiesandroid.Goals.Goal;
import me.bryanlau.goalbuddiesandroid.Goals.GoalListAdapter;
import me.bryanlau.goalbuddiesandroid.MainActivity;
import me.bryanlau.goalbuddiesandroid.R;
import me.bryanlau.goalbuddiesandroid.Requests.GoalListRequest;
import me.bryanlau.goalbuddiesandroid.Requests.ProfileRequest;
import me.bryanlau.goalbuddiesandroid.Requests.RelationRequest;
import me.bryanlau.goalbuddiesandroid.Requests.RequestUtils;

public class ProfileActivity extends AppCompatActivity {
    private View mProgressView;
    private View mProfileView;
    private static User mUser;
    private String username;
    private static ArrayList<Goal> mRecurring;
    private static ArrayList<Goal> mOnetime;
    private static ProfileRequest.RELATION relation;
    private LocalBroadcastManager broadcastManager;

    private void refreshFragments() {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            List<Fragment> fragments = fragmentManager.getFragments();

            // So we can't actually call the adapter to refresh
            // because the content view is *never* created.
            // We'll just take out the fragment and put it back
            // to "simulate" a refresh
            for (Fragment f : fragments) {
                if (f != null) {
                    transaction.detach(f).attach(f);
                }
            }
            transaction.commit();
        } catch (IllegalStateException e) {
            // Activity was destroyed, but we're still good
            // Should not happen now that we put the refresh code in
            // onPostResume, but we'll keep it here anyway
        }
    }

    private BroadcastReceiver profileBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            int statusCode = extras.getInt("statusCode");
            if(RequestUtils.isOk(statusCode)) {
                showProgress(false);

                mUser = extras.getParcelable("user");
                if(mUser == null) {
                    finish();
                }

                setTitle(mUser.mUsername);

                relation =
                        (ProfileRequest.RELATION) intent.getSerializableExtra("relation");
                invalidateOptionsMenu();

                if(relation == ProfileRequest.RELATION.FRIENDS) {
                    new GoalListRequest.Builder(getApplicationContext())
                            .username(mUser.mUsername)
                            .build()
                            .execute();
                }

                refreshFragments();
            } else if(RequestUtils.isBad(statusCode)) {
                switch(statusCode) {
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        Toast.makeText(getApplicationContext(),
                                "User not found.",
                                Toast.LENGTH_LONG).show();
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        // Login expired. The app will try logging in
                        // again when fetching your goal list, so let
                        // MainActivity go back to LoginActivity
                        break;
                    default:
                        Toast.makeText(getApplicationContext(),
                                "Something went wrong, please try again later!",
                                Toast.LENGTH_LONG).show();
                }

                finish();
            }
        }
    };

    private BroadcastReceiver goalListBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            int statusCode = extras.getInt("statusCode");
            if(RequestUtils.isOk(statusCode)) {
                mRecurring = extras.getParcelableArrayList("pendingRecurring");
                mOnetime = extras.getParcelableArrayList("pendingOneTime");

                refreshFragments();
            } else if(RequestUtils.isBad(statusCode)) {
                // Unauthorized, expired token most likely
                // For simplicity, just redirect to login screen
                // in case password was changed
                Toast.makeText(getApplicationContext(),
                        "Something went wrong, please try again later!",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    };

    private BroadcastReceiver relationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            int statusCode = extras.getInt("statusCode");
            if(RequestUtils.isBad(statusCode)) {
                // Unauthorized, expired token most likely
                // For simplicity, just redirect to login screen
                // in case password was changed
                Toast.makeText(getApplicationContext(),
                        extras.getString("error"),
                        Toast.LENGTH_LONG).show();
            } else if (extras.getSerializable("requestType") == RelationRequest.REQUEST_TYPE.BLOCK) {
                // Shouldn't show the profile anymore, go back
                finish();
            }

            // Need to refresh the menu and such anyway regardless
            new ProfileRequest(getApplicationContext(), username).execute();
        }
    };

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem[] menuButtons = {null, null};

        if(relation != null) {
            switch (relation) {
                case FRIENDS:
                    menuButtons[0] = menu.findItem(R.id.action_unfriend);
                    break;
                case INCOMING:
                    menuButtons[0] = menu.findItem(R.id.action_accept);
                    menuButtons[1] = menu.findItem(R.id.action_reject);
                    break;
                case OUTGOING:
                    menuButtons[0] = menu.findItem(R.id.action_cancel);
                    break;
                case NONE:
                    menuButtons[0] = menu.findItem(R.id.action_request);
                    menuButtons[1] = menu.findItem(R.id.action_block);
                    break;
            }

            for (MenuItem button : menuButtons) {
                if (button != null)
                    button.setVisible(true);
            }
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        if(mViewPager != null)
            mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if(tabLayout != null)
            tabLayout.setupWithViewPager(mViewPager);

        relation = ProfileRequest.RELATION.NONE;
        mUser = null;

        mProfileView = findViewById(R.id.profile_container);
        mProgressView = findViewById(R.id.profile_progress);

        mRecurring = new ArrayList<>();
        mOnetime = new ArrayList<>();

        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            new ProfileRequest(getApplicationContext(), username).execute();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle(R.string.dialog_username_search_title);
            alert.setMessage(R.string.dialog_username_search_message);

            // Set an EditText view to get user input
            final EditText input = new EditText(getApplicationContext());
            input.setTextColor(Color.BLUE);
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    username = input.getText().toString();
                    new ProfileRequest(getApplicationContext(), username).execute();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                    finish();
                }
            });
            alert.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        broadcastManager.registerReceiver(
                goalListBroadcastReceiver,
                RequestUtils.goalListFilter);
        broadcastManager.registerReceiver(
                profileBroadcastReceiver,
                RequestUtils.profileFilter);
        broadcastManager.registerReceiver(
                relationBroadcastReceiver,
                RequestUtils.relationFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        broadcastManager.unregisterReceiver(goalListBroadcastReceiver);
        broadcastManager.unregisterReceiver(profileBroadcastReceiver);
        broadcastManager.unregisterReceiver(relationBroadcastReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        RelationRequest request = null;

        switch(id) {
            case R.id.action_request:
                request = new RelationRequest(this, username, RelationRequest.REQUEST_TYPE.REQUEST);
                break;
            case R.id.action_accept:
                request = new RelationRequest(this, username, RelationRequest.REQUEST_TYPE.ACCEPT);
                break;
            case R.id.action_reject:
                request = new RelationRequest(this, username, RelationRequest.REQUEST_TYPE.REJECT);
                break;
            case R.id.action_cancel:
                request = new RelationRequest(this, username, RelationRequest.REQUEST_TYPE.CANCEL);
                break;
            case R.id.action_unfriend:
                request = new RelationRequest(this, username, RelationRequest.REQUEST_TYPE.UNFRIEND);
                break;
            case R.id.action_block:
                request = new RelationRequest(this, username, RelationRequest.REQUEST_TYPE.BLOCK);
                break;
        }

        if(request != null)
            request.execute();

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProfileView.setVisibility(show ? View.GONE : View.VISIBLE);
            mProfileView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProfileView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mProfileView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class GoalFragment extends android.support.v4.app.ListFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        private int position;
        private GoalListAdapter adapter;

        public GoalFragment() {
        }

        public static GoalFragment newInstance(int position) {
            GoalFragment fragment = new GoalFragment();
            Bundle args = new Bundle();
            args.putInt("position", position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            position = getArguments().getInt("position");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            return inflater.inflate(R.layout.fragment_profile_goal, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            ArrayList<Goal> goalList;

            switch(position) {
                case 1:
                    goalList = mRecurring != null ? mRecurring : new ArrayList<Goal>();
                    break;
                case 2:
                    goalList = mOnetime != null ? mOnetime : new ArrayList<Goal>();
                    break;
                default:
                    goalList = new ArrayList<>();
            }

            adapter = new GoalListAdapter(getActivity(), goalList);
            setListAdapter(adapter);
        }

        @Override
        public void onResume() {
            super.onResume();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            // TODO implement some logic
        }
    }

    public static class ProfileFragment extends android.support.v4.app.Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public ProfileFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ProfileFragment newInstance() {
            return new ProfileFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

            if(relation == ProfileRequest.RELATION.FRIENDS) {
                View nameLayout = rootView.findViewById(R.id.profile_section_name);
                if (nameLayout != null)
                    nameLayout.setVisibility(View.VISIBLE);

                TextView nameView =
                        (TextView) rootView.findViewById(R.id.profile_section_body_name);
                if (nameView != null) {
                    String fullName = mUser.mFirstName + " " + mUser.mLastName;
                    nameView.setText(fullName);
                }
            }

            if(mUser != null) {
                TextView usernameView =
                        (TextView) rootView.findViewById(R.id.profile_section_body_username);
                if (usernameView != null) {
                    usernameView.setText(mUser.mUsername);
                }
                TextView cityView =
                        (TextView) rootView.findViewById(R.id.profile_section_body_city);
                if (cityView != null) {
                    cityView.setText(mUser.mCity);
                }
                TextView goalsCompletedView =
                        (TextView) rootView.findViewById(R.id.profile_section_body_goals_completed);
                if (goalsCompletedView != null) {
                    goalsCompletedView.setText(Integer.toString(mUser.mGoalsCompleted));
                }
                TextView timesMotivatedView =
                        (TextView) rootView.findViewById(R.id.profile_section_body_times_motivated);
                if (timesMotivatedView != null) {
                    timesMotivatedView.setText(Integer.toString(mUser.mTimesMotivated));
                }
            }

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch(position) {
                case 0:
                    return ProfileFragment.newInstance();
                default:
                    return GoalFragment.newInstance(position);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Profile";
                case 1:
                    return "Recurring";
                case 2:
                    return "One Time";
            }
            return null;
        }
    }
}
