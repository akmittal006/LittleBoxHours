package com.ankurmittal.littleboxhours;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class User {

    @SerializedName("user")
    @Expose
    private String user;
    @SerializedName("working_hours")
    @Expose
    private WorkingHours workingHours;

    /**
     *
     * @return
     * The user
     */
    public String getUser() {
        return user;
    }

    /**
     *
     * @param user
     * The user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     *
     * @return
     * The workingHours
     */
    public WorkingHours getWorkingHours() {
        return workingHours;
    }

    /**
     *
     * @param workingHours
     * The working_hours
     */
    public void setWorkingHours(WorkingHours workingHours) {
        this.workingHours = workingHours;
    }

}
