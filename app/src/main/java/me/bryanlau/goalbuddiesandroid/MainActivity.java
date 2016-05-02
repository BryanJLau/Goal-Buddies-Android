package me.bryanlau.goalbuddiesandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerTabStrip;
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

    private GoalsSectionsPagerAdapter mGoalsSectionsPagerAdapter;
    private FriendsSectionsPagerAdapter mFriendsSectionsPagerAdapter;
    private ViewPager mViewPager;

    private boolean currentPageGoals;

    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver goalListBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            int statusCode = extras.getInt("statusCode");
            if(RequestUtils.isOk(statusCode)) {
                // So we can't actually call the adapter to refresh
                // because the content view is *never* created.
                // We'll just take out the fragment and put it back
                // to "simulate" a refresh
                FragmentManager fragmentManager = getSupportFragmentManager();

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                List<Fragment> fragments = fragmentManager.getFragments();

                // This will throw an exception on hot deploy when developing
                for(Fragment f : fragments) {
                    transaction.detach(f).attach(f);
                }

                transaction.commit();
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
        mGoalsSectionsPagerAdapter = new GoalsSectionsPagerAdapter(getSupportFragmentManager());
        mFriendsSectionsPagerAdapter = new FriendsSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mGoalsSectionsPagerAdapter);
        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pagerTabStrip);

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

        GoalListRequest request = new GoalListRequest(getApplicationContext());
        request.execute();
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

        switch(id) {
            case R.id.nav_current_recurring:
            case R.id.nav_current_onetime:
            case R.id.nav_finished_recurring:
            case R.id.nav_finished_onetime:
                if(!currentPageGoals) {
                    currentPageGoals = true;

                    GoalListRequest request = new GoalListRequest(getApplicationContext());
                    request.execute();

                    FragmentManager fragmentManager = getSupportFragmentManager();

                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    List<Fragment> fragments = fragmentManager.getFragments();

                    // This will throw an exception on hot deploy when developing
                    for(Fragment f : fragments) {
                        transaction.detach(f);
                        transaction.remove(f);
                    }

                    for(int i = 0; i < mGoalsSectionsPagerAdapter.getCount(); i++) {
                        Fragment f = mGoalsSectionsPagerAdapter.getItem(i);
                        transaction.add(f, Integer.toString(i));
                        transaction.attach(f);
                    }

                    transaction.commit();
                }

                setTitle("My Goals");
                mViewPager.setAdapter(mGoalsSectionsPagerAdapter);
                break;
            case R.id.nav_friends:
            case R.id.nav_incoming:
            case R.id.nav_blocked:
                if(currentPageGoals) {
                    currentPageGoals = false;

                    SocialRequest request = new SocialRequest(getApplicationContext());
                    request.execute();

                    FragmentManager fragmentManager = getSupportFragmentManager();

                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    List<Fragment> fragments = fragmentManager.getFragments();

                    // This will throw an exception on hot deploy when developing
                    for(Fragment f : fragments) {
                        transaction.detach(f);
                        transaction.remove(f);
                    }

                    for(int i = 0; i < mFriendsSectionsPagerAdapter.getCount(); i++) {
                        Fragment f = mFriendsSectionsPagerAdapter.getItem(i);
                        transaction.add(f, Integer.toString(i));
                        transaction.attach(f);
                    }

                    transaction.commit();
                }

                setTitle("Social");
                mViewPager.setAdapter(mFriendsSectionsPagerAdapter);
                break;
            case R.id.nav_about:
                Intent i = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(i);
                break;
        }

        switch(id) {
            case R.id.nav_current_recurring:
            case R.id.nav_friends:
                mViewPager.setCurrentItem(0, true);
                break;
            case R.id.nav_current_onetime:
            case R.id.nav_incoming:
                mViewPager.setCurrentItem(1, true);
                break;
            case R.id.nav_finished_recurring:
            case R.id.nav_blocked:
                mViewPager.setCurrentItem(2, true);
                break;
            case R.id.nav_finished_onetime:
                mViewPager.setCurrentItem(3, true);
                break;
        }

        mViewPager.getAdapter().notifyDataSetChanged();

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
    public class GoalsSectionsPagerAdapter extends FragmentPagerAdapter {

        public GoalsSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public MainGoalFragment getItem(int position) {
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

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class FriendsSectionsPagerAdapter extends FragmentPagerAdapter {

        public FriendsSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public MainSocialFragment getItem(int position) {
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
