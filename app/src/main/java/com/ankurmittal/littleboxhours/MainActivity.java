package com.ankurmittal.littleboxhours;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.FloatingActionButton;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    public static String API = "http://sampleapiurl.com";

    public static final String TAG = "Main Activity";

    private boolean isEditingEnabled = false; // keeps track of edit texts
    private EditText toEditText;
    private EditText fromEditText;
    private ProgressDialog dialog;
    private WorkingHourService service;
    private WorkingHours workingHours; // variable to store working hours
    private WorkingHours tempWorkingHours; // variable to store temporary working hours while in editing mode
    public String username = "littleboxuser123"; // current user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FloatingActionButton editFab = (FloatingActionButton) findViewById(R.id.editFab);
        final FloatingActionButton cancelFab = (FloatingActionButton) findViewById(R.id.cancelFab);
        toEditText = (EditText) findViewById(R.id.toEditText);
        fromEditText = (EditText) findViewById(R.id.fromEditText);

        // To hide keyboard
        toEditText.setInputType(InputType.TYPE_NULL);
        fromEditText.setInputType(InputType.TYPE_NULL);

        //disable editing of Edit Texts
        toEditText.setEnabled(false);
        fromEditText.setEnabled(false);

        /**
         *
         * Dummy working hours (Initial)
         */
        workingHours = new WorkingHours();
        /*    From "07:19 PM"
        *     TO   "05:30 PM"
        * */
        workingHours.setFrom("09:00 AM");
        workingHours.setTo("05:30 PM");

        updateEditTexts(workingHours);

        tempWorkingHours = new WorkingHours();


        editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEditingEnabled) {
                    // Edit texts already enabled

                    //change fab icon to edit
                    editFab.setImageResource(R.drawable.ic_action_edit);
                    //change other fab icon to refresh
                    cancelFab.setImageResource(R.drawable.ic_action_refresh);

                    //Make api call to update new working hours
                    updateWorkingHours();

                } else {
                    //change fab icon to save
                    editFab.setImageResource(R.drawable.ic_action_save);
                    //change other fab icon to cancel
                    cancelFab.setImageResource(R.drawable.ic_action_cancel);
                }
                toggleEditingState();

            }
        });

        cancelFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEditingEnabled) {
                    // Edit texts already enabled

                    //change fab icon to edit
                    editFab.setImageResource(R.drawable.ic_action_edit);
                    //change other fab icon to refresh
                    cancelFab.setImageResource(R.drawable.ic_action_refresh);
                    //update edit texts to old working hours
                    updateEditTexts(workingHours);
                    //go back to non editable state
                    toggleEditingState();


                } else {
                    //refresh or fetch working hours from server
                    getWorkingHours(username);
                }
            }
        });

        handleEditTextsClickEvents();
        handleFocusChangeEvents();


        OkHttpClient client = new OkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        service = retrofit.create(WorkingHourService.class);
        //Fetch working hours for current user, First time activty starts
        getWorkingHours(username);

    }

    /*
    * METHODS FOR API CALLS
    *
    * */

    private void getWorkingHours(String username) {
        Call<User> call =
                service.fetchWorkingHours(username);
        //Show waiting indicators
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Fetching your working hours.");
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();
        // background api call started (increment to busy state)
        EspressoIdlingResource.increment();

        call.enqueue(new Callback<User>() {

            @Override
            public void onResponse(Call<User> call, Response<User> response) {



                if (response.isSuccessful()) {
                    //Success
                    workingHours = response.body().getWorkingHours();
                    //simulate network waiting time
                    // And UPDATE edit texts
                    simulateWaitingTimeAndUpdateEditTexts(workingHours);

                } else {
                    //sleep thread to simulate network waiting time and then dismiss dialog
                    simulateWaitingTimeAndShowToast("Sorry an error occurred");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                //Transport level errors such as no internet etc.
                //sleep thread to simulate network waiting time and then dismiss dialog
                simulateWaitingTimeAndShowToast("Network error !");
            }
        });
    }

    private void updateWorkingHours() {

        Call<WorkingHours> call =
                service.updateWorkingHours("littleboxuser123",tempWorkingHours);

        //show progress indicator dialog
        //initializing progress dialog
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Please Wait while we update working hours");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        //Increment to busy state
        EspressoIdlingResource.increment();

        call.enqueue(new Callback<WorkingHours>() {

            @Override
            public void onResponse(Call<WorkingHours> call, Response<WorkingHours> response) {


                if (response.isSuccessful()) {
                    //update editTexts to temp working hours
                    updateEditTexts(tempWorkingHours);
                    workingHours = tempWorkingHours;
                    simulateWaitingTimeAndShowToast("Working hours updated !");
                } else {
                    // update edit texts to original working hours
                    simulateWaitingTimeAndUpdateEditTexts(workingHours);
                    Toast.makeText(MainActivity.this,"Sorry Server error!",Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(Call<WorkingHours> call, Throwable t) {
                //Transport level errors such as no internet etc. and update edit texts to original working hours
                simulateWaitingTimeAndUpdateEditTexts(workingHours);
                Toast.makeText(MainActivity.this,"Network error !",Toast.LENGTH_LONG).show();

            }
        });

    }

    /*
    * METHODS FOR HANDLING TIME PICKER
    *
    * */

    private void handleEditTextsClickEvents() {
        toEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEditingEnabled) {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                            toListener,15,30,false);
                    timePickerDialog.show();
                }
            }
        });

        fromEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEditingEnabled) {

                    TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                            fromListener,15,30,false);
                    timePickerDialog.show();
                }
            }
        });
    }

    private void handleFocusChangeEvents() {
        toEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    // if it has focus show time picker dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                            toListener,15,30,false);
                    timePickerDialog.show();
                }
            }
        });

        fromEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    // if it has focus show time picker dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,fromListener,15,30,false);
                    timePickerDialog.show();
                }
            }
        });
    }

    /*
    * UTILITY METHODS
    *
    * */

    private void toggleEditingState() {
        isEditingEnabled = !isEditingEnabled;
        toEditText.setEnabled(isEditingEnabled);
        fromEditText.setEnabled(isEditingEnabled);

    }

    private void simulateWaitingTimeAndShowToast(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //simulate network waiting time
                try {
                    Thread.sleep(3 * 1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            // Idle state starts
                            EspressoIdlingResource.decrement();
                            Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void simulateWaitingTimeAndUpdateEditTexts(final WorkingHours workingHours) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //simulate network waiting time
                try {
                    Thread.sleep(3 * 1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            // Idle state starts
                            EspressoIdlingResource.decrement();
                            updateEditTexts(workingHours);                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateEditTexts(WorkingHours workingHours) {
        toEditText.setText(workingHours.getTo());
        fromEditText.setText(workingHours.getFrom());
    }

    /*
    * Returns required time string using int and hourr
    * */

    private String getTimeString(int hour, int minute) {
        String time;
        if(hour>12) {

            time = String.format("%02d:%02d PM",hour-12,minute); // Format of type "01:03 PM"
        } else if(hour == 12){
            time = String.format("%02d:%02d PM",hour,minute);
        } else {
            time = String.format("%02d:%02d AM",hour,minute);
        }
        return time;
    }

    /*
    * TIME PICKER LISTENERS
    *
    * */

    TimePickerDialog.OnTimeSetListener fromListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            String time = getTimeString(hour, minute);
            // store working hour in tempWorkingHour , in case we update hours successfully
            tempWorkingHours.setFrom(time);
            tempWorkingHours.setTo(toEditText.getText().toString());
            //update fromEditText
            fromEditText.setText(time);

        }
    };

    TimePickerDialog.OnTimeSetListener toListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            String time = getTimeString(hour, minute);
            // store working hour in tempWorkingHour , in case we update hours successfully
            tempWorkingHours.setTo(time);
            tempWorkingHours.setFrom(fromEditText.getText().toString());
            // store working hour in toWorkingHour , in case we update hours successfully
            toEditText.setText(time);
        }
    };


    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }

}
