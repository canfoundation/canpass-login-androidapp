package app.cryptobadge.oauth2;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;

public class AuthStateManager {
    private static final String TAG = "AuthStateManager";
    private static final String STORE_NAME = "AuthState";
    private static final String KEY_STATE = "state";

    private final SharedPreferences mPrefs;
    private AuthState mAuthState;

    public AuthStateManager(Context context) {
        mPrefs = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE);
    }

    public void update(
            @Nullable AuthorizationResponse authResponse,
            @Nullable AuthorizationException authException) {
        AuthState authState = getCurrent();
        authState.update(authResponse, authException);
        writeState(authState);
    }

    public void update(
            @Nullable TokenResponse tokenResponse,
            @Nullable AuthorizationException authException) {
        AuthState authState = getCurrent();
        authState.update(tokenResponse, authException);
        writeState(authState);
    }

    public AuthState getCurrent() {
        if (mAuthState == null) {
            mAuthState = readState();
        }
        return mAuthState;
    }

    public void clear() {
        AuthState clearedState = new AuthState();
        writeState(clearedState);
    }

    private AuthState readState() {
        String currentState = mPrefs.getString(KEY_STATE, null);
        if (currentState == null) {
            return new AuthState();
        }

        try {
            return AuthState.jsonDeserialize(currentState);
        } catch (JSONException ex) {
            Log.w(TAG, "Failed to deserialize stored auth state - discarding");
            return new AuthState();
        }
    }

    private void writeState(@Nullable AuthState state) {
        SharedPreferences.Editor editor = mPrefs.edit();
        if (state == null) {
            editor.remove(KEY_STATE);
        } else {
            editor.putString(KEY_STATE, state.jsonSerializeString());
        }

        if (!editor.commit()) {
            throw new IllegalStateException("Failed to write state to shared prefs");
        }
    }
}
