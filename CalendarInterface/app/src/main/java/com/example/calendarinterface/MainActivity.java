package com.example.calendarinterface;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.TimePicker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Calendar;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends Activity
        implements EasyPermissions.PermissionCallbacks {
    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private Button mCallApiButton;
    ProgressDialog mProgress;
    
    //private final ArrayList<String> VALUES = getIntent().getExtras().getStringArrayList("VALUES");
    private ArrayList<String> VALUES = new ArrayList<String>();
    private String VALUEADDER(){
        VALUES.add("THIS IS A SUMMARY!");
        VALUES.add("THIS IS A LOCATION!");
        VALUES.add("THIS IS A DESCRIPTION!");
        VALUES.add("2018-02-17T09:00");
        VALUES.add("2018-02-17T17:00");
        return "5";
    }

    String ten = VALUEADDER();

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "CONFIRM";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY ,CalendarScopes.CALENDAR};

    private TimeZone tz = TimeZone.getDefault();
    private String timezone = tz.getID();
    private String offset(TimeZone tz){
        String offset = Integer.toString(tz.getOffset(new Date().getTime()) / 1000 / 3600);
        if(offset.length()<3){
            offset = offset.substring(0,1)+"0"+offset.substring(1);
        }
        offset+=":00";
        return offset;
    }

    private String concatString = ":00"+offset(tz);
    private String summary= VALUES.get(0);
    private String location= VALUES.get(1);
    private String description= VALUES.get(2);
    private String starttime = VALUES.get(3) + concatString;
    private String endtime = VALUES.get(4) + concatString;
    private EditText one;
    private EditText two;
    private EditText three;
    private TextView four;
    private TextView five;
    private TextView six;
    private TextView seven;
    private CheckBox eight;
    private CheckBox nine;
    private Boolean rec = false;
    private String freq = "";

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        LinearLayout activityLayout = (LinearLayout)findViewById(R.id.linearLayout3);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        int dp = 50;
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(px * 4, px);
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//        activityLayout.setLayoutParams(lp);
//        activityLayout.setOrientation(LinearLayout.VERTICAL);
//        activityLayout.setPadding(16, 16, 16, 16);
//
//        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);



//        System.out.println("THEEEEE TIMEEEE ZONEEEEEEE IS:   "+concatString);



        four = new EditText(this);
        four.setText(starttime);

        mCallApiButton = (Button)findViewById(R.id.button);
//        activityLayout.addView(mCallApiButton);
        mCallApiButton.setText(BUTTON_TEXT);
        mCallApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallApiButton.setEnabled(false);
                //mOutputText.setText("");
                summary = one.getText().toString();
                location = two.getText().toString();
                description = three.getText().toString();
                starttime = four.getText().toString().concat("T".concat(five.getText().toString().concat(concatString)));
                endtime = six.getText().toString().concat("T".concat(seven.getText().toString().concat(concatString)));
                if(nine.isChecked()){
                    rec = true;
                    freq="DAILY";
                } else if(eight.isChecked()){
                    System.out.println("Eight was reached");
                    rec = true;
                    freq="WEEKLY";
                }
                getResultsFromApi();
                mCallApiButton.setEnabled(true);
            }
        });
//        TextView textView1 = new TextView(this);
//        textView1.setText("Event Summary:");
//        activityLayout.addView(textView1);

        one = (EditText) findViewById(R.id.editText1);
        one.setText(summary, TextView.BufferType.EDITABLE);
//        activityLayout.addView(one);

//        TextView textView2 = new TextView(this);
//        textView2.setText("Location:");
//        activityLayout.addView(textView2);

        two = (EditText) findViewById(R.id.editText2);
        two.setText(location, TextView.BufferType.EDITABLE);
//        activityLayout.addView(two);

//        TextView textView3 = new TextView(this);
//        textView3.setText("Description:");
//        activityLayout.addView(textView3);

        three = (EditText) findViewById(R.id.editText3);
        three.setText(description, TextView.BufferType.EDITABLE);
//        activityLayout.addView(three);

//        TextView textView4 = new TextView(this);
//        textView4.setText("Start Date:");
//        activityLayout.addView(textView4);

        four = (TextView) findViewById(R.id.editText4);
//        four.setTextAppearance(android.R.style.TextAppearance_Large);
//        activityLayout.addView(four);
//
//        TextView textView5 = new TextView(this);
//        textView5.setText("Start Time:");
//        activityLayout.addView(textView5);

        five = (TextView) findViewById(R.id.editText5);
//        five.setTextAppearance(android.R.style.TextAppearance_Large);
//        activityLayout.addView(five);
//
//        TextView textView6 = new TextView(this);
//        textView6.setText("End Date:");
//        activityLayout.addView(textView6);

        six = (TextView) findViewById(R.id.editText6);
//        six.setTextAppearance(android.R.style.TextAppearance_Large);
//        activityLayout.addView(six);
//
//        TextView textView7 = new TextView(this);
//        textView7.setText("End Date:");
//        activityLayout.addView(textView7);

        seven = (TextView) findViewById(R.id.editText7);
//        seven.setTextAppearance(android.R.style.TextAppearance_Large);
//        activityLayout.addView(seven);
//
//        TextView repeat = new TextView(this);
//        repeat.setText("Repeat:");
//        activityLayout.addView(repeat);

        eight = (CheckBox) findViewById(R.id.checkBox);
        if (eight.isChecked())
            eight.setChecked(false);
//        activityLayout.addView(eight);
//
//        TextView textView8 = new TextView(this);
//        textView8.setText("Weekly");
//        activityLayout.addView(textView8);

        nine = (CheckBox) findViewById(R.id.checkBox2);
        if (nine.isChecked())
            nine.setChecked(false);
//        activityLayout.addView(nine);
//
//        TextView textView9 = new TextView(this);
//        textView9.setText("Daily");
//        activityLayout.addView(textView9);


        String temp = dateFromDateTime(starttime);
        four.setText(temp);

        final Calendar myCalendar = Calendar.getInstance();

        String temp1 = timeFromDateTime(starttime);
        five.setText(temp1);

        String temp2 = dateFromDateTime(endtime);
        six.setText(temp2);

        String temp3 = timeFromDateTime(endtime);
        seven.setText(temp3);


        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                myCalendar.set(Calendar.YEAR, i);
                myCalendar.set(Calendar.MONTH, i1);
                myCalendar.set(Calendar.DAY_OF_MONTH, i2);

                String myFormat = "yyyy-MM-dd"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                four.setText(sdf.format(myCalendar.getTime()));
            }
        };

        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(MainActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final TimePickerDialog.OnTimeSetListener stime = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                myCalendar.set(Calendar.HOUR, i);
                myCalendar.set(Calendar.MINUTE, i1);

                String hour = Integer.toString(i);
                if (i < 10)
                    hour = "0".concat(hour);

                String minute = Integer.toString(i1);
                if (i1 < 10)
                    minute = "0".concat(minute);

                String time = hour + ":" + minute;

                five.setText(time);
            }
        };

        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(MainActivity.this, stime, myCalendar.get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE), false).show();
            }
        });

        final DatePickerDialog.OnDateSetListener date1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                myCalendar.set(Calendar.YEAR, i);
                myCalendar.set(Calendar.MONTH, i1);
                myCalendar.set(Calendar.DAY_OF_MONTH, i2);

                String myFormat = "yyyy-MM-dd"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                six.setText(sdf.format(myCalendar.getTime()));
            }
        };

        six.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(MainActivity.this, date1, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });



        final TimePickerDialog.OnTimeSetListener etime = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                myCalendar.set(Calendar.HOUR, i);
                myCalendar.set(Calendar.MINUTE, i1);

                String hour = Integer.toString(i);
                if (i < 10)
                    hour = "0".concat(hour);

                String minute = Integer.toString(i1);
                if (i1 < 10)
                    minute = "0".concat(minute);

                String time = hour + ":" + minute;
                seven.setText(time);
            }
        };

        seven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(MainActivity.this, etime, myCalendar.get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE), false).show();
            }
        });

//        activityLayout.addView(mCallApiButton);
//
        mOutputText = new TextView(this);
        //mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mOutputText.setText(
                "Click the \'" + BUTTON_TEXT +"\' button to save to Google Calendar");
        activityLayout.addView(mOutputText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Saving ...");
//
//        setContentView(activityLayout);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    private String dateFromDateTime(String datetime){
        String [] split = datetime.split("T");
        return split[0];
    }

    private String timeFromDateTime(String datetime){
        String [] split = datetime.split("T");
        String [] split2 = split[1].split("-");
        return split2[0].substring(0,5);
    }

    private String dateTimeFromDateTime(String date, String time){
        return date+"T"+time+"-4:00";
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                makeEvent(summary, location, description, starttime, endtime, timezone);
                return Collections.emptyList();//getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return Collections.emptyList();
            }
        }



        private void makeEvent(String summary, String location, String description, String starttime, String endtime, String timezone) throws IOException{
            Event event = new Event()
                    .setSummary(summary)
                    .setLocation(location)
                    .setDescription(description);

            DateTime startDateTime = new DateTime(starttime);
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone(timezone);
            event.setStart(start);

            DateTime endDateTime = new DateTime(endtime);
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone(timezone);
            event.setEnd(end);

            String[] recurrence = new String[] {"RRULE:FREQ="+freq+";"};
            if(rec)
                event.setRecurrence(Arrays.asList(recurrence));

//            EventAttendee[] attendees = new EventAttendee[] {
//                    new EventAttendee().setEmail("lpage@example.com"),
//                    new EventAttendee().setEmail("sbrin@example.com"),
//            };
//            event.setAttendees(Arrays.asList(attendees));
//
            EventReminder[] reminderOverrides = new EventReminder[] {
                    new EventReminder().setMethod("email").setMinutes(24 * 60),
                    new EventReminder().setMethod("popup").setMinutes(10),
            };
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminderOverrides));
            event.setReminders(reminders);

            String calendarId = "primary";
            event = mService.events().insert(calendarId, event).execute();
            System.out.printf("Event created: %s\n", event.getHtmlLink());
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<String>();
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = event.getStart().getDate();
                }
                eventStrings.add(
                        String.format("%s (%s)", event.getSummary(), start));
            }
            return eventStrings;
        }


        @Override
        protected void onPreExecute() {
//            mOutputText.setText("");
//            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("");
            } else {
                output.add(0, "Data retrieved using the Google Calendar API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }
}
