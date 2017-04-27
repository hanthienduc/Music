package com.mnml.music.utils.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LastFMApi {
    @GET("?method=artist.getinfo&api_key=493fc564e6533635edcc4e0f371bf1da&format=json")
    Call<LastFMArtist> getArtist(@Query("artist") String name);
}
