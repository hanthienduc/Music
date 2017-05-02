package com.boswelja.lastfm;

import com.boswelja.lastfm.models.artist.LastFMArtist;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LastFMRequest {

    private String query, apiKey;
    private Retrofit.Builder retrofitBuilder;

    public LastFMRequest() {
        retrofitBuilder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://ws.audioscrobbler.com/2.0/");
    }

    public ArtistTask setArtist() {
        Retrofit retrofit = retrofitBuilder.build();
        LastFMApi lastFMApi = retrofit.create(LastFMApi.class);
        return new ArtistTask(lastFMApi, query, apiKey);
    }

    public LastFMRequest setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public LastFMRequest setQuery(String query) {
        this.query = query;
        return this;
    }

    public LastFMRequest setCustomClient(final OkHttpClient client) {
        retrofitBuilder.client(client);
        return this;
    }

    public static class ArtistTask {

        private LastFMApi api;
        private String query, apiKey;
        private Callback<LastFMArtist> callback;

        ArtistTask(LastFMApi api, String query, String apiKey) {
            this.apiKey = apiKey;
            this.api = api;
            this.query = query;
        }

        public ArtistTask setCallback(Callback<LastFMArtist> callback) {
            this.callback = callback;
            return this;
        }

        public void build() {
            Call<LastFMArtist> artistCall = api.getArtist(query, apiKey);
            artistCall.enqueue(callback);
        }
    }
}