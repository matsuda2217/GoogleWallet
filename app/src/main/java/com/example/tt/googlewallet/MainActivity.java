package com.example.tt.googlewallet;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.NotifyTransactionStatusRequest;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.Wallet.WalletOptions;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.BuyButtonText;
import com.google.android.gms.wallet.fragment.Dimension;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks
        ,GoogleApiClient.OnConnectionFailedListener{
    private GoogleApiClient mgoogleApiClient;
    private SupportWalletFragment mWalletFragment;
    private SupportWalletFragment mXmlWallerFragment;

    private MaskedWallet maskedWallet;
    private FullWallet mfullWallet;

    public static final int MASKED_WALLET_REQUEST_CODE = 888;
    public static final int FULL_WALLET_REQUEST_CODE = 889;
    public static final String WALLET_FRAGMENT_ID = "wallet_fragment";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWalletFragment =(SupportWalletFragment) getSupportFragmentManager().findFragmentByTag(WALLET_FRAGMENT_ID);

        WalletFragmentInitParams startParams;
        WalletFragmentInitParams.Builder startParamsBuilder = WalletFragmentInitParams.newBuilder()
                .setMaskedWalletRequest(generateMaskedWalletRequest())
                .setMaskedWalletRequestCode(MASKED_WALLET_REQUEST_CODE);

        startParams = startParamsBuilder.build();

        if (mWalletFragment == null) {
            WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                    .setBuyButtonText(BuyButtonText.BUY_WITH_GOOGLE)
                    .setBuyButtonWidth(Dimension.MATCH_PARENT);

            WalletFragmentOptions walletFragmentOptions =WalletFragmentOptions.newBuilder()
                    .setEnvironment(WalletConstants.ENVIRONMENT_SANDBOX)
                    .setFragmentStyle(walletFragmentStyle)
                    .setTheme(WalletConstants.THEME_LIGHT)
                    .setMode(WalletFragmentMode.BUY_BUTTON)
                    .build();

            mWalletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);
            mWalletFragment.initialize(startParams);

        }
        getSupportFragmentManager().beginTransaction().replace(R.id.wallet_button_holder, mWalletFragment ,WALLET_FRAGMENT_ID)
                .commit();

        mgoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(Wallet.API,new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_SANDBOX)
                    .setTheme(WalletConstants.THEME_HOLO_LIGHT)
                        .build())
        .build();
        setContentView(R.layout.activity_main);
        }

    @Override
    protected void onStart() {
        mgoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case  MASKED_WALLET_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        maskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                        break;
                    case  RESULT_CANCELED:
                        break;
                    default:
                        Toast.makeText(this, "An Error", Toast.LENGTH_SHORT).show();
                }
                break;
            case FULL_WALLET_REQUEST_CODE:
                switch (requestCode) {
                    case RESULT_OK:
                        mfullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
                        Toast.makeText(this, mfullWallet.getProxyCard().getPan().toString(), Toast.LENGTH_SHORT).show();
                        Wallet.Payments.notifyTransactionStatus(mgoogleApiClient,
                                generateNotifyTransactionStatusRequest(mfullWallet.getGoogleTransactionId(),
                                        NotifyTransactionStatusRequest.Status.SUCCESS));

                        break;
                    default:
                        Toast.makeText(this, "AN ERROR", Toast.LENGTH_SHORT).show();
                }
            case WalletConstants.RESULT_ERROR:
                Toast.makeText(this, "AN ERROR", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public static NotifyTransactionStatusRequest generateNotifyTransactionStatusRequest(
            String googleId, int status) {
        return NotifyTransactionStatusRequest.newBuilder()
                .setGoogleTransactionId(googleId)
                .setStatus(status).build();
    }

    private MaskedWalletRequest generateMaskedWalletRequest() {
        MaskedWalletRequest maskedWalletRequest = MaskedWalletRequest.newBuilder()
                .setMerchantName("Google I/O Codelab")
                .setPhoneNumberRequired(true)
                .setShippingAddressRequired(true)
                .setCurrencyCode("USD")
                .setShouldRetrieveWalletObjects(true)
                .setEstimatedTotalPrice("10.00")
                .setCart(Cart.newBuilder()
                        .setCurrencyCode("UDS")
                        .setTotalPrice("10.00")
                        .addLineItem(LineItem.newBuilder().setCurrencyCode("USD")
                                .setQuantity("1")
                                .setUnitPrice("10.00")
                                .setTotalPrice("10.00")
                                .build())
                        .build())
                .build();
      return   maskedWalletRequest;

    }

    private FullWalletRequest generateFullWalletRequest(String googleTransactionId) {
        FullWalletRequest fullWalletRequest = FullWalletRequest.newBuilder()
                .setCart(Cart.newBuilder()
                        .setCurrencyCode("USD")
                        .setTotalPrice("10.00")
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode("USD")
                                .setQuantity("1")
                                .setUnitPrice("10.00")
                                .setTotalPrice("10.00")
                                .build())

                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode("USD")
                                .setDescription("Tax")
                                .setRole(LineItem.Role.TAX)
                                .setTotalPrice(".10")
                                .build())
                        .build())
                .build();
        return fullWalletRequest;
    }

    public void requestFullWalletView(View view) {
        if (mgoogleApiClient.isConnected()) {
            Wallet.Payments.loadFullWallet(mgoogleApiClient
                    ,generateFullWalletRequest(maskedWallet.getGoogleTransactionId())
                    ,FULL_WALLET_REQUEST_CODE);
        }
    }
    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}



