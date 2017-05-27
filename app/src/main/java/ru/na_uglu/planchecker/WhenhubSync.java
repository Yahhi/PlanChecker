package ru.na_uglu.planchecker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimeZone;

public class WhenhubSync extends Service {

    IBinder mBinder = new LocalBinder();

    public WhenhubSync() {

    }

    private JSONObject createScheduleObject(String name, String description, String fieldName) {
        JSONObject calendar = new JSONObject();
        try {
            calendar.put("calendarType", "absolute");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject customField = new JSONObject();
        try {
            JSONObject config = new JSONObject();
            config.put("fieldName", fieldName);
            customField.put("id", "d2c6eb7ec14a496ce60ea2a0b5bf");
            customField.put("customField", "chart-series");
            customField.put("name", "Chart Series");
            customField.put("config", config);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray customFields = new JSONArray();
        customFields.put(customField);

        JSONObject params = new JSONObject();
        try {
            params.put("scope", "public");
            params.put("calendar", calendar);
            params.put("sync", false);
            params.put("name", name);
            params.put("description", description);
            params.put("customFields", customFields);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.i("JSON", params.toString());
        return params;
    }

    public void createSchedules() {
        RequestQueue queue = Volley.newRequestQueue(this);
        Response.ErrorListener commonErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.i("VOLLEY", "didn't work");
            }
        };

        String url = formatURLWithToken("https://api.whenhub.com/api/users/me/schedules");

        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.POST, url,
                createScheduleObject("Planning Accuracy", "Graph is showing how accurate you are at planning. 100 is for excellent accuracy, the lesser rate the lesser is accuracy.", "Chart Series"),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String scheduleId = "";
                        try {
                            scheduleId = response.getString("id");
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("accuracyScheduleId", scheduleId);
                            editor.apply();

                            LocalData data = new LocalData(getBaseContext(), false);
                            WhenhubEvent[] accuracyIntervals = data.getAllAccuracyRates();
                            for (WhenhubEvent event: accuracyIntervals) {
                                createEvent(event, scheduleId);
                            }
                            data.closeDataConnection();

                            createScheduleViewType(scheduleId, "line");

                            saveViewCode(scheduleId, "workingViewCode");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Log.i("VOLLEY", "scheduleID = " + scheduleId);
                    }
                },
                commonErrorListener);
        queue.add(stringRequest);

        JsonObjectRequest stringRequest2 = new JsonObjectRequest(
                Request.Method.POST, url,
                createScheduleObject("Working resume", "Graph is showing how much you work every day", "Working time"),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String scheduleId = "";
                        try {
                            scheduleId = response.getString("id");
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("workingScheduleId", scheduleId);
                            editor.apply();

                            LocalData data = new LocalData(getBaseContext(), false);
                            WhenhubEvent[] timeIntervals = data.getAllTimeIntervals();
                            for (WhenhubEvent event: timeIntervals) {
                                createEvent(event, scheduleId);
                            }
                            data.closeDataConnection();

                            createScheduleViewType(scheduleId, "stacked-column");

                            saveViewCode(scheduleId, "workingViewCode");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Log.i("VOLLEY", "scheduleID = " + scheduleId);
                    }
                },
                commonErrorListener);
        queue.add(stringRequest2);
    }

    private void createScheduleViewType(String scheduleId, String viewName) {
        RequestQueue queue = Volley.newRequestQueue(this);
        Response.ErrorListener commonErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.i("VOLLEY", "didn't work");
            }
        };

        String whencastId = scheduleId.substring(0, scheduleId.length() - 1);
        int charCode = scheduleId.charAt(scheduleId.length() - 1);
        whencastId += (char) (charCode+1);
        String url = formatURLWithToken("https://api.whenhub.com/api/schedules/" + scheduleId + "/whencasts/" + whencastId);
        //Log.i("VOLLEY", scheduleId + " and " + whencastId);

        JSONObject whencast = new JSONObject();
        try {
            JSONObject publisher = new JSONObject();
            publisher.put("name", "whenhub");

            JSONObject settings = new JSONObject();
            settings.put("title", "Data from Plan Checker App");
            settings.put("chartSubTitle", "");
            settings.put("legendLayout", "horizontal");
            settings.put("horizontalAlign", "left");
            settings.put("verticalAlign", "bottom");

            JSONObject generalSettings = new JSONObject();
            generalSettings.put("theme", "red");
            generalSettings.put("eventlist", "nearest");

            whencast.put("id", whencastId);
            whencast.put("name", "charts");
            whencast.put("publisher", publisher);
            whencast.put("viewName", viewName);
            whencast.put("settings", settings);
            whencast.put("theme", "red");
            whencast.put("generalSettings", generalSettings);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.PUT, url,
                whencast,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.i("VOLLEY", response.toString());
                    }
                },
                commonErrorListener);
        queue.add(stringRequest);
    }

    void saveViewCode(String scheduleId, final String preferencesTitle) {
        RequestQueue queue = Volley.newRequestQueue(this);
        Response.ErrorListener commonErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.i("VOLLEY", "didn't work");
            }
        };
        String url = formatURLWithToken("https://api.whenhub.com/api/schedules/" + scheduleId);
        JsonObjectRequest stringRequest2 = new JsonObjectRequest(
                Request.Method.GET, url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String viewCode = "";
                        try {
                            viewCode = response.getString("viewCode");
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(preferencesTitle, viewCode);
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Log.i("VOLLEY", response.toString());
                    }
                },
                commonErrorListener);
        queue.add(stringRequest2);
    }


    public void createEventForTimeIntervals(WhenhubEvent event) {
        createEvent(event, getWorkingScheduleId());
    }

    private void createEvent(WhenhubEvent event, String scheduleId){
        RequestQueue queue = Volley.newRequestQueue(this);
        Response.ErrorListener commonErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.i("VOLLEY", "didn't work");
            }
        };

        String url = formatURLWithToken("https://api.whenhub.com/api/schedules/" + scheduleId + "/events");

        JSONObject when = new JSONObject();
        try {
            when.put("period", "minute");
            when.put("startDate", event.dateTime);
            when.put("startTimezone", TimeZone.getDefault().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject value = new JSONObject();
        try {
            value.put("value", event.customField);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject customFieldData = new JSONObject();
        try {
            customFieldData.put("d2c6eb7ec14a496ce60ea2a0b5bf", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject params = new JSONObject();
        try {
            params.put("when", when);
            params.put("name", event.title);
            params.put("customFieldData", customFieldData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.i("VOLLEY", params.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //Log.i("VOLLEY", response.toString());
            }
        }, commonErrorListener);
        queue.add(jsonObjectRequest);
    }

    public void createEventForAccuracy(WhenhubEvent event) {
        createEvent(event, getAccuracyScheduleId());
    }

    String formatURLWithToken(String s) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String accessToken = preferences.getString("access_token", "");
        String url = s + "?access_token=" + accessToken;
        return url;
    }

    String getWorkingScheduleId() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return preferences.getString("workingScheduleId", "");
    }
    String getAccuracyScheduleId() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return preferences.getString("accuracyScheduleId", "");
    }

    static boolean isSyncAvailable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String accessToken = preferences.getString("access_token", "");
        return !accessToken.equals("");
    }

    public static int getAccuracyRate(int realTime, int estimatedTime) {
        int customField;
        //Log.i("MATH", "numbers: " + realTime + " " + estimatedTime);
        if (realTime > estimatedTime) {
            if (realTime == 0) {
                customField = 0;
            } else {
                customField = (int) Math.ceil(estimatedTime * 100 / realTime);
                //Log.i("MATH", "inside: " + (estimatedTime * 100 / realTime));
            }
        } else {
            customField = (int) Math.ceil(realTime * 100 / estimatedTime);
            //Log.i("MATH", "inside: " + (realTime * 100 / estimatedTime));
        }
        //Log.i("MATH", "final: " + customField);
        return customField;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Log.i(TAG, "bindng service...");
        return mBinder;
    }

    class LocalBinder extends Binder {
        WhenhubSync getService() {
            // Return this instance of LocalService so clients can call public methods
            return WhenhubSync.this;
        }
    }
}
