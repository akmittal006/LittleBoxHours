package com.ankurmittal.littleboxhours;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by AnkurMittal2 on 28-05-2016.
 */
public interface WorkingHourService {
    //Method to update working hours for user
    @POST("user/{user}/working_hours")
    Call<WorkingHours> updateWorkingHours(@Path("user") String user, @Body WorkingHours workingHours);

    // MEthod to fetch working hour for user
    @GET("users/{user}/")
    Call<User> fetchWorkingHours(@Path("user") String user);
}

