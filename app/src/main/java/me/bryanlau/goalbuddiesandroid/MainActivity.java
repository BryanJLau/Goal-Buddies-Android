package me.bryanlau.goalbuddiesandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import me.bryanlau.goalbuddiesandroid.Requests.GoalListRequest;
import me.bryanlau.goalbuddiesandroid.Requests.RequestUtils;
import me.bryanlau.goalbuddiesandroid.Requests.SocialRequest;

public class MainActivity extends AppCompatActivity
        implements MainGoalFragment.OnFragmentInteractionListener,
        MainSocialFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    private GoalSectionsPagerAdapter mGoalSectionsPagerAdapter;
    private SocialSectionsPagerAdapter mSocialSectionsPagerAdapter;
    private ViewPager mGoalViewPager, mSocialViewPager;

    private boolean currentPageGoals;

    private void refreshFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        List<Fragment> fragments = fragmentManager.getFragments();

        // So we can't actually call the adapter to refresh
        // because the content view is *never* created.
        // We'll just take out the fragment and put it back
        // to "simulate" a refresh
        // This will throw an exception on hot deploy when developing
        for(Fragment f : fragments) {
            if(f != null) {
                transaction.detach(f).attach(f);
            }
        }
        transaction.commit();
    }

    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver goalListBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            int statusCode = extras.getInt("statusCode");
            if(RequestUtils.isOk(statusCode)) {
                refreshFragments();
                Snackbar.make(findViewById(R.id.main_content), "Refreshed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else if(RequestUtils.isBad(statusCode)) {
                // Unauthorized, expired token most likely
                // For simplicity, just redirect to login screen
                // in case password was changed
                Toast.makeText(getApplicationContext(),
                        "Your login has expired, please login again!",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        FragmentManager fragmentManager = getSupportFragmentManager();
        mGoalSectionsPagerAdapter = new GoalSectionsPagerAdapter(fragmentManager);
        mSocialSectionsPagerAdapter = new SocialSectionsPagerAdapter(fragmentManager);

        // Set up the ViewPager with the sections adapter.
        mGoalViewPager = (ViewPager) findViewById(R.id.goalViewPager);
        mGoalViewPager.setAdapter(mGoalSectionsPagerAdapter);
        mSocialViewPager = (ViewPager) findViewById(R.id.socialViewPager);
        mSocialViewPager.setAdapter(mSocialSectionsPagerAdapter);
        //PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pagerTabStrip);

        IntentFilter filter = new IntentFilter("goalbuddies.goalList");
        IntentFilter socialFilter = new IntentFilter("goalbuddies.social");

        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        broadcastManager.registerReceiver(goalListBroadcastReceiver, filter);
        broadcastManager.registerReceiver(goalListBroadcastReceiver, socialFilter);

        currentPageGoals = true;

        setTitle("My Goals");
    }

    @Override
    public void onResume() {
        super.onResume();

        GoalListRequest grequest = new GoalListRequest(getApplicationContext());
        grequest.execute();

        SocialRequest srequest = new SocialRequest(getApplicationContext());
        srequest.execute();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            GoalListRequest request = new GoalListRequest(getApplicationContext());
            request.execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        ViewPager mViewPager;

        switch(id) {
            default:
            case R.id.nav_current_recurring:
            case R.id.nav_current_onetime:
            case R.id.nav_finished_recurring:
            case R.id.nav_finished_onetime:
                if(!currentPageGoals) {
                    currentPageGoals = true;
                    mSocialViewPager.setVisibility(View.GONE);
                    mGoalViewPager.setVisibility(View.VISIBLE);

                    GoalListRequest grequest = new GoalListRequest(getApplicationContext());
                    grequest.execute();
                }
                mViewPager = mGoalViewPager;

                setTitle("My Goals");
                break;
            case R.id.nav_friends:
            case R.id.nav_incoming:
            case R.id.nav_blocked:
                if(currentPageGoals) {
                    currentPageGoals = false;
                    mGoalViewPager.setVisibility(View.GONE);
                    mSocialViewPager.setVisibility(View.VISIBLE);

                    SocialRequest srequest = new SocialRequest(getApplicationContext());
                    srequest.execute();
                }
                mViewPager = mSocialViewPager;

                setTitle("Social");
                break;
            case R.id.nav_about:
                Intent i = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(i);
                return true;
            case R.id.nav_logout:
                SharedPreferences preferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                // Commit because we need to purge it NOW
                preferences.edit().clear().commit();
                finish();
                return true;
        }

        int currentPosition;

        switch(id) {
            default:
            case R.id.nav_current_recurring:
            case R.id.nav_friends:
                currentPosition = 0;
                break;
            case R.id.nav_current_onetime:;
            case R.id.nav_incoming:
                currentPosition = 1;
                break;
            case R.id.nav_finished_recurring:
            case R.id.nav_blocked:
                currentPosition = 2;
                break;
            case R.id.nav_finished_onetime:
                currentPosition = 3;
                break;
        }
        mViewPager.setCurrentItem(currentPosition, true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class GoalSectionsPagerAdapter extends FragmentStatePagerAdapter {

        public GoalSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return MainGoalFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Pending Recurring";
                case 1:
                    return "Pending One Time";
                case 2:
                    return "Finished Recurring";
                case 3:
                    return "Finished One Time";
            }
            return null;
        }
    }

    public class SocialSectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SocialSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return MainSocialFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Friends";
                case 1:
                    return "Pending Requests";
                case 2:
                    return "Blocked";
            }
            return null;
        }
    }
}
