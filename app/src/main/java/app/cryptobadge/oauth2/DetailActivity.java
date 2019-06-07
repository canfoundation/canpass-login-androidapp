package app.cryptobadge.oauth2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";
    public static final String ACCESS_TOKEN_KEY = "accessToken";

    private AuthStateManager mStateManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStateManager = new AuthStateManager(this);
        setContentView(R.layout.activity_info);
        findViewById(R.id.btn_logout).setOnClickListener(l -> logOut());
    }

    @Override
    protected void onStart() {
        super.onStart();

        String accessToken = getIntent().getStringExtra(ACCESS_TOKEN_KEY);
        Log.d(TAG, "accessToken: " + accessToken);
        fetchUserInfo(accessToken);
    }

    @MainThread
    private void fetchUserInfo(String accessToken) {
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        okHttpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .addHeader("Authorization", "Bearer " + accessToken);
            Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(Configuration.GraphQlEndpointURL)
                .okHttpClient(okHttpClient.build())
                .build();

        apolloClient.query(
                UserInfoQuery
                        .builder()
                        .build()
        ).enqueue(new ApolloCall.Callback<UserInfoQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<UserInfoQuery.Data> response) {
                runOnUiThread(() -> handleQueryResponse(response));
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(TAG, e.getMessage(), e);
                logOut();
            }
        });
    }

    @MainThread
    private void handleQueryResponse(@NotNull Response<UserInfoQuery.Data> response) {
        if (response.hasErrors()) {
            Error error = response.errors().get(0);
            Log.e(TAG, error.message());
            Toast.makeText(DetailActivity.this, error.message(), Toast.LENGTH_SHORT).show();
            logOut();
            return;
        }

        if (response.data() != null && response.data().me != null) {
            final UserInfoQuery.Me userInfo = response.data().me;
            Log.d(TAG, "User info: " + userInfo);
            displayUserInfo(userInfo);
        }
    }

    @MainThread
    private void displayUserInfo(UserInfoQuery.Me userInfo) {
        if (userInfo == null) {
            return;
        }

        if (userInfo.id != null) {
            TextView idView = findViewById(R.id.txt_id);
            idView.setText(userInfo.id);
        }

        if (userInfo.name != null) {
            TextView titleView = findViewById(R.id.tv_title);
            titleView.setText(userInfo.name);

            TextView nameView = findViewById(R.id.txt_name);
            nameView.setText(userInfo.name);
        }

        if (userInfo.email != null) {
            TextView emailView = findViewById(R.id.txt_email);
            emailView.setText(userInfo.email);
        }

        if (userInfo.path != null) {
            TextView pathView = findViewById(R.id.txt_path);
            pathView.setText(userInfo.path);
        }

        if (userInfo.resourceUrl != null) {
            TextView resourceUrlView  = findViewById(R.id.txt_resource_url);
            resourceUrlView.setText(userInfo.resourceUrl);
        }
    }

    @MainThread
    private void logOut() {
        mStateManager.clear();

        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

}
