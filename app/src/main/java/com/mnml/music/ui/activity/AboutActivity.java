package com.mnml.music.ui.activity;

import android.app.PendingIntent;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.vending.billing.IInAppBillingService;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.LibsConfiguration;
import com.mikepenz.aboutlibraries.entity.Library;
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment;
import com.mnml.music.R;
import com.mnml.music.utils.Config;
import com.mnml.music.utils.Utils;
import io.reactivex.disposables.Disposable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AboutActivity extends AestheticActivity {

    @BindView(R.id.about_toolbar) Toolbar toolbar;
    private boolean darkMode = false, hasGoogleServices = false, googleServicesEnabled;
    private Unbinder unbinder;
    private Disposable darkModeSubscription, primaryColorSubscription;
    private int primaryColor;
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
    private IInAppBillingService billingService;
    private final LibsConfiguration.LibsListener libsListener =
            new LibsConfiguration.LibsListener() {
                @Override
                public void onIconClicked(View view) {
                    String url = "https://github.com/MnmlOS/Music";
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setInstantAppsEnabled(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(view.getContext(), Uri.parse(url));
                }

                @Override
                public boolean onLibraryAuthorClicked(View view, Library library) {
                    Uri url = Uri.parse(library.getAuthorWebsite());
                    CustomTabsIntent.Builder builder;
                    CustomTabsIntent customTabsIntent;
                    builder = new CustomTabsIntent.Builder();
                    builder.setInstantAppsEnabled(true);
                    builder.setToolbarColor(primaryColor);
                    customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(view.getContext(), url);
                    return true;
                }

                @Override
                public boolean onLibraryContentClicked(View view, Library library) {
                    Uri url = Uri.parse(library.getLibraryWebsite());
                    CustomTabsIntent.Builder builder;
                    CustomTabsIntent customTabsIntent;
                    builder = new CustomTabsIntent.Builder();
                    builder.setInstantAppsEnabled(true);
                    builder.setToolbarColor(primaryColor);
                    customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(view.getContext(), url);
                    return true;
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
                                    .onPositive((materialDialog, dialogAction) -> materialDialog.dismiss())
                                    .show();
                            return true;
                        case "SPECIAL2":
                            new MaterialDialog.Builder(view.getContext())
                                    .title(getString(R.string.contributors))
                                    .items(R.array.contributors)
                                    .autoDismiss(false)
                                    .positiveText(getString(R.string.done))
                                    .onPositive((materialDialog, dialogAction) -> materialDialog.dismiss())
                                    .itemsCallback(
                                            (materialDialog, view12, i, charSequence) -> {
                                                String firstChar = charSequence.subSequence(0, 1).toString();
                                                if (firstChar.equals("J") || firstChar.equals("F")) {
                                                    String url;
                                                    CustomTabsIntent.Builder builder;
                                                    CustomTabsIntent customTabsIntent;
                                                    builder = new CustomTabsIntent.Builder();
                                                    builder.setInstantAppsEnabled(true);
                                                    builder.setToolbarColor(primaryColor);
                                                    customTabsIntent = builder.build();
                                                    switch (firstChar) {
                                                        case "J":
                                                            url = "https://github.com/boswelja/";
                                                            customTabsIntent.launchUrl(view.getContext(), Uri.parse(url));
                                                            break;
                                                        case "F":
                                                            url = "https://github.com/F4uzan/";
                                                            customTabsIntent.launchUrl(view.getContext(), Uri.parse(url));
                                                            break;
                                                    }
                                                }
                                            })
                                    .show();
                            return true;
                        case "SPECIAL3":
                            if (hasGoogleServices && googleServicesEnabled) {
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
                                                if (sku != null)
                                                    optionsList.add(sku.substring(0, sku.indexOf("(") - 1));
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        hasGoogleServices = Utils.isGooglePlayServicesAvailable(this);
        googleServicesEnabled = sharedPrefs.getBoolean("google_services", false);
        super.onCreate(savedInstanceState);
        darkModeSubscription = Aesthetic.get().isDark().subscribe(aBoolean -> darkMode = aBoolean);
        primaryColorSubscription = Aesthetic.get().colorPrimary().subscribe(integer -> primaryColor = integer);
        setContentView(R.layout.activity_about);
        unbinder = ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        LibsBuilder builder = new LibsBuilder()
                .withActivityStyle(darkMode
                        ? Libs.ActivityStyle.DARK
                        : Libs.ActivityStyle.LIGHT)
                .withSortEnabled(true)
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
            final Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");
            bindService(serviceIntent, billingConnection, Context.BIND_AUTO_CREATE);
        } else {
            billingConnection = null;
            billingService = null;
        }

        LibsSupportFragment libsFragment = builder.supportFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.about_libs_holder, libsFragment).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        if (!darkModeSubscription.isDisposed()) darkModeSubscription.dispose();
        if (!primaryColorSubscription.isDisposed()) primaryColorSubscription.dispose();
        if (billingService != null) {
            unbindService(billingConnection);
        }
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
