package com.example.andre.cameraworks;

import android.os.AsyncTask;
import android.os.Debug;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public class Vision extends AsyncTask<String,Void,String> {
    String requestUrl = "https://vision.googleapis.com/v1/images:annotate?key=AIzaSyBN4_SF1LOBK8DkavvqkEgFjLo5Gg1CuJU";
    @Override
    protected String doInBackground(String... strings) {

        String requestUrl="https://vision.googleapis.com/v1/images:annotate?key=AIzaSyBN4_SF1LOBK8DkavvqkEgFjLo5Gg1CuJU";
        String response = sendPostRequest(requestUrl, strings[0]);

        JSONObject obj = null;
        try {
            obj = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String obj6 = null;
        try {
            obj6 = obj.getJSONArray("responses").getJSONObject(0).getJSONArray("textAnnotations").getJSONObject(0).getString("description");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //new

        String result = obj6.replaceAll("[\\t\\n\\r]+"," "); //remove space

        String next_payload= "{\"document\":{\"type\":\"PLAIN_TEXT\",\"language\": \"EN\",\"content\":\" " + result + "\"}, \"encodingType\":\"UTF8\"}";
//		System.out.println(next_payload);

        String next_requestUrl="https://language.googleapis.com/v1/documents:analyzeEntities?key=AIzaSyBN4_SF1LOBK8DkavvqkEgFjLo5Gg1CuJU";
        String next_response = sendPostRequest(next_requestUrl, next_payload);
//		System.out.println(next_response);

        String[] output = {"","","","",""};
        JSONObject obj10 = null;
        try {
            obj10 = new JSONObject(next_response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray coll = null;
        try {
            coll = obj10.getJSONArray("entities");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int len = coll.length();
        for (int i=0;i<len;i++){

            try {
                JSONObject cur = coll.getJSONObject(i);
                String cur_type = cur.getString("type");
                if (cur_type.equals("EVENT")) {
                    output[0] += cur.getString("name");
                    output[0] += " ";
                } else if (cur_type.equals("LOCATION")) {
                    output[1] += cur.getString("name");
                    output[1] += " ";
                } else if (cur_type.equals("TIME")) {
                    output[3] += cur.getString("name");
                    output[3] += " ";
                } else {
                    output[2] += cur.getString("name");
                    output[2] += " ";
                }
            }
            catch (Exception e){}
        }
        ArrayList<String> dates = new ArrayList<String>();
        ArrayList<String> times = new ArrayList<String>();
        String[] months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        String[] resultsArray = result.split(" ");
        for(int i = 0; i < resultsArray.length; i++){
            for(int j = 0; j < months.length; j++){
                if(months[j].equals(resultsArray[i])){
                    try{
                        if(resultsArray[i+1].contains("-")){
                            String[] days = resultsArray[i+1].split("-");
                            dates.add(months[j]+"-"+days[0]);
                            dates.add(months[j]+"-"+days[1]);
                        }
                        else{
                            dates.add(months[j]+"-"+resultsArray[i+1]);

                        }
                        resultsArray[i+1] = "";
                    }
                    catch(Exception e){}
                }
            }
        }
        for(int i = 0; i < resultsArray.length; i++){
            if(resultsArray[i].contains("am") || resultsArray[i].contains("pm")){
                try {
                    if (resultsArray[i - 1].contains("-") || resultsArray[i].contains("-")) {
                        String[] splitTimes = resultsArray[i + 1].split("-");
                        times.add(splitTimes[0]);
                        times.add(splitTimes[1]);
                    }
                    else if(isNumber(resultsArray[i-1])){
                        times.add(resultsArray[i-1]);
                    }
                }
                catch(Exception e){}
            }

        }
        String timeString = "";
        for(String time : times){
            timeString += time;
        }
        output[3] = timeString;

        String dateString = "";
        for(String date : dates){
            dateString += date;
        }
        output[4] = dateString;
        return Arrays.toString(output);

    }

    public static String sendPostRequest(String requestUrl, String payload) {
        StringBuffer jsonString = new StringBuffer();
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            connection.disconnect();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return jsonString.toString();
    }

    public boolean isNumber(String test){
        try{
            Integer.parseInt(test);
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

}
