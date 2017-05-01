package com.boswelja.lastfm;

import com.boswelja.lastfm.models.artist.LastFMArtist;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LastFMRequest {

    public LastFMRequest() {

    }

    public static final class Builder {

        private LastFMApi lastFMApi;
        private String mode, query;
        private Retrofit.Builder retrofitBuilder;
        private Callback<LastFMArtist> artistCallback;

        public Builder() {
            retrofitBuilder = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://ws.audioscrobbler.com/2.0/");
        }

        public Builder setMode(final String mode) {
            this.mode = mode;
            return this;
        }

        public Builder queryName(String query) {
            this.query = query;
            return this;
        }

        public Builder setCustomClient(final OkHttpClient client) {
            retrofitBuilder.client(client);
            return this;
        }

        public Builder setCallback(Callback<LastFMArtist> callback) {
            artistCallback = callback;
            return this;
        }
        public LastFMRequest build() {
            Retrofit retrofit = retrofitBuilder.build();
            lastFMApi = retrofit.create(LastFMApi.class);
            Call<LastFMArtist> artistCall = lastFMApi.getArtist(query);
            artistCall.enqueue(artistCallback);
            return new LastFMRequest();
        }
    }
}
