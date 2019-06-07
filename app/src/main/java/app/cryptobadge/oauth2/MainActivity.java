package app.cryptobadge.oauth2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int RC_AUTH = 100;

    private AuthStateManager mStateManager;
    private AuthorizationService mAuthorizationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.validate();

        mStateManager = new AuthStateManager(this);
        if (mStateManager.getCurrent().isAuthorized()) {
            Log.i(TAG, "User is already authenticated, proceeding to token activity");
            final String accessToken = mStateManager.getCurrent().getAccessToken();
            launchDetailActivity(accessToken);
            return;
        }

        mAuthorizationService = new AuthorizationService(this);

        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_login).setOnClickListener((View view) -> startAuth());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAuthorizationService != null) {
            mAuthorizationService.dispose();
        }
    }

    @MainThread
    void startAuth() {
        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse(Configuration.AuthorizationEndpointURL) /* auth endpoint */,
                Uri.parse(Configuration.TokenEndpointURL) /* token endpoint */
        );

        Uri redirectUri = Uri.parse(Configuration.RedirectURI);
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                serviceConfiguration,
                Configuration.ClientID,
                ResponseTypeValues.CODE,
                redirectUri
        );
        builder.setScopes(Configuration.Scope);
        AuthorizationRequest authRequest = builder.build();

        Intent authIntent = mAuthorizationService.getAuthorizationRequestIntent(authRequest);
        startActivityForResult(authIntent, RC_AUTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_AUTH) {
                AuthorizationResponse authResponse = AuthorizationResponse.fromIntent(data);
                AuthorizationException authException = AuthorizationException.fromIntent(data);
                mStateManager.update(authResponse, authException);

                if (authResponse != null && authResponse.authorizationCode != null) {
                    exchangeAuthorizationCode(authResponse);
                }
            }
        }
    }

    @MainThread
    private void exchangeAuthorizationCode(AuthorizationResponse authorizationResponse) {
        TokenRequest tokenRequest = authorizationResponse.createTokenExchangeRequest();
        ClientAuthentication clientAuthentication;
        try {
            clientAuthentication = mStateManager.getCurrent().getClientAuthentication();
        } catch (ClientAuthentication.UnsupportedAuthenticationMethod ex) {
            Log.d(TAG, "Token request cannot be made, client authentication for the token "
                    + "endpoint could not be constructed (%s)", ex);
            return;
        }

        mAuthorizationService.performTokenRequest(tokenRequest, clientAuthentication, this::handleCodeExchangeResponse);
    }

    @WorkerThread
    private void handleCodeExchangeResponse(
            @Nullable TokenResponse tokenResponse,
            @Nullable AuthorizationException authException) {
        mStateManager.update(tokenResponse, authException);
        if (mStateManager.getCurrent().isAuthorized()) {
            final String accessToken = tokenResponse.accessToken;
            Log.d(TAG, "Access Token: " + accessToken);
            runOnUiThread(() -> launchDetailActivity(accessToken));
        } else {
            String message = "Authorization Code exchange failed"
                    + ((authException != null) ? authException.error : "");
            Log.e(TAG, message);
        }
    }

    @MainThread
    private void launchDetailActivity(String accessToken) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.ACCESS_TOKEN_KEY, accessToken);
        startActivity(intent);
        finish();
    }
}
