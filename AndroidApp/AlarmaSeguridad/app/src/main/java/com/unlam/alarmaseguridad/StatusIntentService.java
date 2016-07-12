package com.unlam.alarmaseguridad;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

public class StatusIntentService extends IntentService {
    public static final String CHECK_ALARM_INFO = "com.unlam.alarmaseguridad.action.FOO";

    public StatusIntentService() {
        super("StatusIntentService");
    }

    public static void startActionFoo(Context context) {
        Intent intent = new Intent(context, StatusIntentService.class);
        intent.setAction(CHECK_ALARM_INFO);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (CHECK_ALARM_INFO.equals(action)) {
                checkAlarmInfo();
            }
        }
    }

    private String completeReadUrl(String webServiceName) {
        return "https://dweet.io/get/latest/dweet/for/" + webServiceName;
    }

    private Boolean getBooleanWebService(String wsName, String wsProperty) throws JSONException {
        return Boolean.valueOf(getValue(requestWebService(completeReadUrl(wsName)), wsProperty));
    }

    private String getStringWebService(String wsName, String wsProperty) throws JSONException {
        return getValue(requestWebService(completeReadUrl(wsName)), wsProperty);
    }

    private void checkAlarmInfo() {
        while(true) {
            try {
                Thread.sleep(5000);
                if(Alarm.getInstance().isSendMessage()) {
                    sendAlarmInfo();
                }
                else{
                    Alarm alarm = Alarm.getInstance();
                    Boolean isActivated = getBooleanWebService("soa_alarma_activated", "activated");
                    Boolean isRinging = getBooleanWebService("soa_alarma_ringing", "ringing");
                    String gas = getStringWebService("soa_alarma_sensor_values", "gas");
                    String logs = getStringWebService("soa_alarma_logs", "logs");

                    alarm.setActivated(isActivated);
                    alarm.setRinging(isRinging);
                    alarm.setGas(gas);
                    alarm.setLogs(logs);

                    Intent intent = new Intent(CHECK_ALARM_INFO);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                    System.out.println("TASK RUNNED");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String getValue(JSONObject responseActivated, String paraName) throws JSONException {
        try{
            return responseActivated.getJSONArray("with").getJSONObject(0).getJSONObject("content").getString(paraName);
        }
        catch(JSONException e) {
            System.out.println("no existe parametro");
        }
        return "";
    }

    private void sendAlarmInfo() {
        try {

            Boolean panic = Alarm.getInstance().isPanic();
            Boolean change = Alarm.getInstance().isChange();

            if(panic) {
                JsonObject panicJson = new JsonObject();
                panicJson.addProperty("panic", true);
                DweetIO.publish("soa_alarma_panic", panicJson);
            }
            else{
                if(change) {
                    JsonObject changeJson = new JsonObject();
                    changeJson.addProperty("change", true);
                    DweetIO.publish("soa_alarma_change_state", changeJson);
                }
            }

            System.out.println("MESSAGE SENT");
            Alarm.getInstance().setSendMessage(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject requestWebService(String serviceUrl) {
        disableConnectionReuseIfNecessary();

        HttpURLConnection urlConnection = null;
        try {
            // create connection
            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection)
                    urlToRequest.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);

            // handle issues
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // handle unauthorized (if service requires user login)
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
                // handle any other errors, like 404, 500,..
            }

            // create JSON object from content
            InputStream in = new BufferedInputStream(
                    urlConnection.getInputStream());
            return new JSONObject(getResponseText(in));

        } catch (MalformedURLException e) {
            // URL is invalid
        } catch (SocketTimeoutException e) {
            // data retrieval or connection timed out
        } catch (IOException e) {
            // could not read response body
            // (could not create input stream)
        } catch (JSONException e) {
            // response body is no valid JSON string
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    /**
     * required in order to prevent issues in earlier Android version.
     */
    private static void disableConnectionReuseIfNecessary() {
        // see HttpURLConnection API doc
        if (Integer.parseInt(Build.VERSION.SDK)
                < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private static String getResponseText(InputStream inStream) {
        // very nice trick from
        // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
        return new Scanner(inStream).useDelimiter("\\A").next();
    }
}
