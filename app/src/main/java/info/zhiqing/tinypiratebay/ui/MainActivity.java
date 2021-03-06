package info.zhiqing.tinypiratebay.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import info.zhiqing.tinypiratebay.R;
import info.zhiqing.tinypiratebay.entities.SearchHistory;
import info.zhiqing.tinypiratebay.util.ConfigUtil;
import info.zhiqing.tinypiratebay.util.SearchUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FloatingSearchView.OnMenuItemClickListener, FloatingSearchView.OnSearchListener {
    public static final String TAG = "MainActivity";

    public static final String KEY_FIRST_START = "first_start";

    private Fragment browseFragment = null;

    private FloatingSearchView floatingSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                e.onNext(pref.getBoolean(KEY_FIRST_START, true));
                pref.edit().putBoolean(KEY_FIRST_START, false).apply();
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            Intent intent = new Intent(MainActivity.this, IntroActivity.class);
                            startActivity(intent);
                        }
                    }
                });

        initView();

    }

    private void initView() {
        setContentView(R.layout.activity_main);

        browseFragment = new CategoryFragment();

        floatingSearchView = (FloatingSearchView) findViewById(R.id.floating_search);
        floatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                SearchHistory history = (SearchHistory) searchSuggestion;
                SearchUtil.addToHistory(MainActivity.this, history.getTitle());
            }

            @Override
            public void onSearchAction(String currentQuery) {
                SearchUtil.addToHistory(MainActivity.this, currentQuery);
                SearchActivity.actionStart(MainActivity.this, ConfigUtil.BASE_URL + "/search/" + currentQuery, currentQuery);
            }
        });
        floatingSearchView.setOnMenuItemClickListener(this);
        SearchUtil.swapHistory(floatingSearchView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingSearchView.setSearchFocused(true);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        floatingSearchView.attachNavigationDrawerToMenuButton(drawer);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        showFragment(browseFragment, R.string.title_app);
    }

    private void showFragment(Fragment fragment, int titleId) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        floatingSearchView.setSearchBarTitle(getResources().getString(R.string.app_name));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_browse) {
            showFragment(browseFragment, R.string.title_browse);
        } else if (id == R.id.nav_recent) {
            SearchActivity.actionStart(this, ConfigUtil.BASE_URL + "/recent", getResources().getString(R.string.title_recent));
        } else if (id == R.id.nav_search) {
            floatingSearchView.setSearchFocused(true);
        } else if (id == R.id.nav_settings) {
            SettingsActivity.actionStart(this);
        } else if (id == R.id.nav_help) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.faq_title)
                    .setMessage(R.string.faq_content)
                    .setCancelable(true)
                    .setPositiveButton(R.string.faq_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .show();
        } else if (id == R.id.nav_about) {
            AboutActivity.actionStart(this);
        }
        /* else if (id == R.id.nav_share) {
        } else if (id == R.id.nav_send) {
            Uri uri = Uri.parse("mailto:lizhiqing1996@gmail.com");
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.tips_email_subject));
            if (intent.resolveActivity(getPackageManager()) == null) {
                Snackbar.make(findViewById(R.id.floating_search),
                        getString(R.string.tips_no_email_client),
                        Snackbar.LENGTH_LONG).show();
            }
            startActivity(intent);
        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

    }

    @Override
    public void onSearchAction(String currentQuery) {

    }

    @Override
    public void onActionMenuItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SettingsActivity.actionStart(this);
        } else if (id == R.id.action_about) {
            AboutActivity.actionStart(this);
        }

    }
}
