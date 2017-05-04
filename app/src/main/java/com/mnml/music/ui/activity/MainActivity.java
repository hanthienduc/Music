package com.mnml.music.ui.activity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.*;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.vending.billing.IInAppBillingService;
import com.mnml.music.R;
import com.mnml.music.adapters.ViewPagerAdapter;
import com.mnml.music.service.MusicService;
import com.mnml.music.ui.fragments.*;
import com.mnml.music.utils.Config;
import com.mnml.music.utils.PlaylistHelper;
import com.mnml.music.utils.shortcuts.ShortcutHandler;
import com.mnml.music.utils.Utils;
import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.MaterialDialogsUtil;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.LibsConfiguration;
import com.mikepenz.aboutlibraries.entity.Library;
import com.mikepenz.aboutlibraries.util.Colors;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import org.json.JSONException;
import org.json.JSONObject;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import java.util.ArrayList;

@RuntimePermissions
public class MainActivity extends ATHToolbarActivity {

    @BindView(R.id.main_toolbar) Toolbar toolbar;
    @BindView(R.id.main_viewpager) ViewPager viewPager;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.sliding_layout) SlidingUpPanelLayout slidingUpPanelLayout;
    @BindView(R.id.main_tab_layout) TabLayout tabLayout;

    private Unbinder unbinder;
    private Bundle savedInstance;
    private boolean darkMode = false, hasGoogleServices = false, googleServicesEnabled = false;
    private Drawer drawer;
    private IInAppBillingService billingService;
    private ServiceConnection billingConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            billingService = IInAppBillingService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            billingService = null;
        }
    };
    private PlaylistFragment playlistFragment;
    private int primaryColor, accentColor;
    private MusicService service;
    private PlayerFragment player;
    private final ServiceConnection serviceConnection =
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    service = ((MusicService.MyBinder) binder).getService(MainActivity.this);
                    updatePlayer();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    service = null;
                }
            };
    private final LibsConfiguration.LibsListener libsListener =
            new LibsConfiguration.LibsListener() {
                @Override
                public void onIconClicked(View view) {
                    String url = "https://github.com/MnmlOS/Music";
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setInstantAppsEnabled(true);
                    builder.setToolbarColor(ThemeStore.primaryColor(view.getContext()));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(view.getContext(), Uri.parse(url));
                }

                @Override
                public boolean onLibraryAuthorClicked(View view, Library library) {
                    return false;
                }

                @Override
                public boolean onLibraryContentClicked(View view, Library library) {
                    return false;
                }

                @Override
                public boolean onLibraryBottomClicked(View view, Library library) {
                    return false;
                }

                @Override
                public boolean onExtraClicked(View view, Libs.SpecialButton specialButton) {
                    switch (specialButton.toString()) {
                        case "SPECIAL1":
                            new MaterialDialog.Builder(view.getContext())
                                    .title(getString(R.string.changelog))
                                    .items(R.array.changelog)
                                    .autoDismiss(false)
                                    .positiveText(getString(R.string.done))
                                    .positiveColor(ThemeStore.accentColor(view.getContext()))
                                    .onPositive((materialDialog, dialogAction) -> materialDialog.dismiss())
                                    .show();
                            return true;
                        case "SPECIAL2":
                            new MaterialDialog.Builder(view.getContext())
                                    .title(getString(R.string.contributors))
                                    .items(R.array.contributors)
                                    .autoDismiss(false)
                                    .positiveText(getString(R.string.done))
                                    .positiveColor(ThemeStore.accentColor(view.getContext()))
                                    .onPositive((materialDialog, dialogAction) -> materialDialog.dismiss())
                                    .itemsCallback(
                                            (materialDialog, view12, i, charSequence) -> {
                                                String url;
                                                CustomTabsIntent.Builder builder;
                                                CustomTabsIntent customTabsIntent;
                                                switch (charSequence.subSequence(0, 1).toString()) {
                                                    case "J":
                                                        url = "https://github.com/boswelja/";
                                                        builder = new CustomTabsIntent.Builder();
                                                        builder.setInstantAppsEnabled(true);
                                                        builder.setToolbarColor(ThemeStore.primaryColor(view.getContext()));
                                                        customTabsIntent = builder.build();
                                                        customTabsIntent.launchUrl(view.getContext(), Uri.parse(url));
                                                        break;
                                                    case "F":
                                                        url = "https://github.com/F4uzan/";
                                                        builder = new CustomTabsIntent.Builder();
                                                        builder.setInstantAppsEnabled(true);
                                                        builder.setToolbarColor(ThemeStore.primaryColor(view.getContext()));
                                                        customTabsIntent = builder.build();
                                                        customTabsIntent.launchUrl(view.getContext(), Uri.parse(url));
                                                }
                                            })
                                    .show();
                            return true;
                        case "SPECIAL3":
                            if(hasGoogleServices && googleServicesEnabled) {
                                ArrayList<String> skuList = new ArrayList<>();
                                skuList.add(Config.DONATE_10);
                                skuList.add(Config.DONATE_5);
                                skuList.add(Config.DONATE_2);
                                Bundle querySkus = new Bundle();
                                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
                                try {
                                    Bundle skuDetails =
                                            billingService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
                                    int response = skuDetails.getInt("RESPONSE_CODE");
                                    if (response == 0) {
                                        ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                                        if (responseList != null) {
                                            ArrayList<String> optionsList = new ArrayList<>();
                                            for (String thisResponse : responseList) {
                                                JSONObject object = new JSONObject(thisResponse);
                                                String sku = object.getString("title");
                                                if (sku != null) optionsList.add(sku.substring(0, sku.indexOf("(") - 1));
                                            }
                                            new MaterialDialog.Builder(view.getContext())
                                                    .title("Donate")
                                                    .items(optionsList)
                                                    .itemsCallback(
                                                            (materialDialog, view1, i, charSequence) -> {
                                                                try {
                                                                    String sku =
                                                                            charSequence
                                                                                    .toString()
                                                                                    .toLowerCase()
                                                                                    .replaceAll(" ", "")
                                                                                    .replace("$", "");
                                                                    if (sku.equals(Config.DONATE_2)
                                                                            || sku.equals(Config.DONATE_5)
                                                                            || sku.equals(Config.DONATE_10)) {
                                                                        Bundle buyIntentBundle =
                                                                                billingService.getBuyIntent(
                                                                                        3, getPackageName(), sku, "inapp", "");
                                                                        PendingIntent pendingIntent =
                                                                                buyIntentBundle.getParcelable("BUY_INTENT");
                                                                        if (pendingIntent != null)
                                                                            startIntentSenderForResult(
                                                                                    pendingIntent.getIntentSender(),
                                                                                    Config.DONATE_REQUEST_CODE,
                                                                                    new Intent(),
                                                                                    0,
                                                                                    0,
                                                                                    0);
                                                                    }
                                                                } catch (RemoteException | IntentSender.SendIntentException e) {
                                                                    e.printStackTrace();
                                                                    Toast.makeText(
                                                                            view.getContext(),
                                                                            "Failed to send request",
                                                                            Toast.LENGTH_SHORT)
                                                                            .show();
                                                                }
                                                            })
                                                    .autoDismiss(false)
                                                    .positiveText(getString(R.string.done))
                                                    .positiveColor(ThemeStore.accentColor(view.getContext()))
                                                    .onPositive((materialDialog, dialogAction) -> materialDialog.dismiss())
                                                    .show();
                                        }
                                    }
                                } catch (RemoteException | JSONException | NullPointerException e) {
                                    e.printStackTrace();
                                    Toast.makeText(view.getContext(), "Failed to get option data", Toast.LENGTH_SHORT).show();
                                }
                            }
                            return true;
                    }
                    return false;
                }

                @Override
                public boolean onIconLongClicked(View view) {
                    return false;
                }

                @Override
                public boolean onLibraryAuthorLongClicked(View view, Library library) {
                    return false;
                }

                @Override
                public boolean onLibraryContentLongClicked(View view, Library library) {
                    return false;
                }

                @Override
                public boolean onLibraryBottomLongClicked(View view, Library library) {
                    return false;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        hasGoogleServices = Utils.isGooglePlayServicesAvailable(this);
        if(!hasGoogleServices) {
            sharedPrefs.edit().putBoolean("google_services", false).apply();
        }
        googleServicesEnabled = sharedPrefs.getBoolean("google_services", false);
        boolean subsTheme = sharedPrefs.getBoolean("substratum_theme", false);
        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this)
                    .activityTheme(R.style.AppTheme_Light)
                    .primaryColorRes(R.color.colorPrimary)
                    .accentColorRes(R.color.colorAccent)
                    .commit();
        }
        if (subsTheme) {
            ThemeStore.editTheme(this)
                    .primaryColorRes(R.color.colorPrimary)
                    .accentColorRes(R.color.colorAccent)
                    .commit();
        }
        primaryColor = ThemeStore.primaryColor(this);
        accentColor = ThemeStore.accentColor(this);
        darkMode = sharedPrefs.getBoolean("dark_theme", false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        ATH.setActivityToolbarColorAuto(this, toolbar);
        tabLayout.setTabTextColors(
                ToolbarContentTintHelper.toolbarSubtitleColor(this, primaryColor),
                ToolbarContentTintHelper.toolbarTitleColor(this, primaryColor));
        tabLayout.setBackgroundColor(primaryColor);
        tabLayout.setSelectedTabIndicatorColor(accentColor);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        savedInstance = savedInstanceState;

        MainActivityPermissionsDispatcher.initWithCheck(this);
    }

    public MusicService getService() {
        return service;
    }

    public void updatePlayer() {
        player.updatePlayer();
    }

    public void updatePlayingList() {
        player.updatePlayingList();
    }

    public void updatePlayerPlayState() {
        player.updatePlayState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        ToolbarContentTintHelper.setToolbarContentColorBasedOnToolbarColor(
                this, toolbar, menu, primaryColor);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.play_all:
                if (service != null) service.playAllSongs();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void init() {
        final ShortcutHandler shortcutHandler = new ShortcutHandler();
        shortcutHandler.create(this);
        TintHelper.setTintAuto(fab, accentColor, true);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Intent i = new Intent(MainActivity.this, MusicService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(i);

        setupViewPager();
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);
        TintHelper.setTintAuto(tabLayout, primaryColor, true);

        setDrawer();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        player = new PlayerFragment();
        transaction.replace(R.id.player_holder, player).commit();

        MaterialDialogsUtil.updateMaterialDialogsThemeSingleton(this);

        if(hasGoogleServices && googleServicesEnabled) {
            Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");
            bindService(serviceIntent, billingConnection, Context.BIND_AUTO_CREATE);
        } else {
            billingConnection = null;
            billingService = null;
        }
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        playlistFragment = new PlaylistFragment();
        adapter.addFrag(new SongsFragment(), getResources().getString(R.string.songs));
        adapter.addFrag(new AlbumsFragment(), getResources().getString(R.string.album));
        adapter.addFrag(new ArtistsFragment(), getResources().getString(R.string.artist));
        adapter.addFrag(playlistFragment, getResources().getString(R.string.playlist));
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        viewPager.setOffscreenPageLimit(4);
        fab.setOnClickListener(v ->
                new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.add_playlist)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(
                                getString(R.string.playlist_example),
                                null,
                                (dialog, input) -> {
                                    if (!input.toString().equals("")) {
                                        PlaylistHelper helper = new PlaylistHelper(MainActivity.this);
                                        helper.createNewPlayList(input.toString());
                                        playlistFragment.updateList();
                                    } else {
                                        Toast.makeText(
                                                MainActivity.this,
                                                R.string.playlist_name_empty_warning,
                                                Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                })
                        .positiveText(getString(R.string.ok))
                        .negativeText(getString(R.string.cancel))
                        .show());
    }

    public SlidingUpPanelLayout getSlidingPanel() {
        return slidingUpPanelLayout;
    }

    private void setDrawer() {
        final PrimaryDrawerItem songs = new PrimaryDrawerItem()
                .withSelectedTextColor(accentColor)
                .withSelectedIconColor(accentColor)
                .withIdentifier(0)
                .withName(R.string.songs)
                .withIcon(MaterialDesignIconic.Icon.gmi_audio);
        final PrimaryDrawerItem albums = new PrimaryDrawerItem()
                .withSelectedTextColor(accentColor)
                .withSelectedIconColor(accentColor)
                .withIdentifier(1)
                .withName(R.string.albums)
                .withIcon(MaterialDesignIconic.Icon.gmi_album);
        final PrimaryDrawerItem artists = new PrimaryDrawerItem()
                .withSelectedTextColor(accentColor)
                .withSelectedIconColor(accentColor)
                .withIdentifier(2)
                .withName(R.string.artist)
                .withIcon(MaterialDesignIconic.Icon.gmi_account);
        final PrimaryDrawerItem playlist = new PrimaryDrawerItem()
                .withSelectedTextColor(accentColor)
                .withSelectedIconColor(accentColor)
                .withIdentifier(3)
                .withName(R.string.playlist)
                .withIcon(MaterialDesignIconic.Icon.gmi_playlist_audio);
        final SecondaryDrawerItem settings = new SecondaryDrawerItem()
                .withIdentifier(5)
                .withName(getString(R.string.settings))
                .withSelectable(false)
                .withIcon(MaterialDesignIconic.Icon.gmi_settings);
        final SecondaryDrawerItem about = new SecondaryDrawerItem()
                .withIdentifier(6)
                .withName(R.string.about)
                .withSelectable(false)
                .withIcon(MaterialDesignIconic.Icon.gmi_info);
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHeader(R.layout.header)
                .withActionBarDrawerToggle(true)
                .withFullscreen(true)
                .withCloseOnClick(true)
                .addDrawerItems(
                        songs,
                        albums,
                        artists,
                        playlist,
                        new DividerDrawerItem(),
                        settings,
                        about)
                .withOnDrawerItemClickListener(
                        (view, position, drawerItem) -> {
                            switch ((int) drawerItem.getIdentifier()) {
                                case 0:
                                    viewPager.setCurrentItem(0);
                                    break;
                                case 1:
                                    viewPager.setCurrentItem(1);
                                    break;
                                case 2:
                                    viewPager.setCurrentItem(2);
                                    break;
                                case 3:
                                    viewPager.setCurrentItem(3);
                                    break;
                                case 5:
                                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                    startActivity(intent);
                                    break;
                                case 6:
                                    Colors colors = new Colors(primaryColor, Utils.getAutoStatColor(primaryColor));
                                    LibsBuilder builder = new LibsBuilder()
                                            .withActivityStyle(darkMode
                                                    ? Libs.ActivityStyle.DARK
                                                    : Libs.ActivityStyle.LIGHT)
                                            .withSortEnabled(true)
                                            .withActivityColor(colors)
                                            .withAboutIconShown(true)
                                            .withAboutVersionShown(true)
                                            .withActivityTitle(getString(R.string.about))
                                            .withAboutDescription(getString(R.string.app_desc))
                                            .withAboutSpecial1(getString(R.string.changelog))
                                            .withAboutSpecial1Description("Button 1")
                                            .withAboutSpecial2(getString(R.string.contributors))
                                            .withAboutSpecial2Description("Button 2")
                                            .withListener(libsListener);
                                    if (hasGoogleServices && googleServicesEnabled) {
                                        builder.withAboutSpecial3(getString(R.string.donate))
                                                .withAboutSpecial3Description("Button 3");
                                    }
                                    builder.start(this);
                                    break;
                            }
                            return true;
                        })
                .withSavedInstance(savedInstance)
                .build();
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        drawer.getHeader().findViewById(R.id.header).setBackgroundColor(primaryColor);
        setStatusBarColor(Utils.getAutoStatColor(primaryColor));
        drawer.getActionBarDrawerToggle()
                .getDrawerArrowDrawable()
                .setColor(ToolbarContentTintHelper.toolbarContentColor(this, primaryColor));
        viewPager.addOnPageChangeListener(
                new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        if (position == 3) fab.show();
                        else fab.hide();
                    }

                    @Override
                    public void onPageSelected(int position) {
                        drawer.setSelection(position, false);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {}
                });
        drawer.setSelectionAtPosition(1);
    }

    public void setStatusBarColor(final int color) {
        drawer.getDrawerLayout().getStatusBarBackgroundDrawable().setTint(color);
        ATH.setLightStatusbarAuto(this, color);
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        unbindService(serviceConnection);
        if (billingService != null) {
            unbindService(billingConnection);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (service != null) player.updatePlayer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Config.DONATE_REQUEST_CODE) {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    switch (sku) {
                        case "donate2":
                            Toast.makeText(this, "Thanks for donating $2", Toast.LENGTH_SHORT).show();
                            break;
                        case "donate5":
                            Toast.makeText(this, "Thanks for donating $5", Toast.LENGTH_SHORT).show();
                            break;
                        case "donate10":
                            Toast.makeText(this, "Thanks for donating $10", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
