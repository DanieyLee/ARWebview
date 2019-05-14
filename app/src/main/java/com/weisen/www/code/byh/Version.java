package com.weisen.www.code.byh;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Version {
    public static void getVersion(String address){
        HttpURLConnection conn = null;
        try {
            URL url = new URL(address);
            conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.7 Safari/537.36"); //设置浏览器ua 保证不出现503
            if(HttpURLConnection.HTTP_OK==conn.getResponseCode()){
                InputStream in=conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String tmpString = "";
                StringBuilder retJSON = new StringBuilder();
                while ((tmpString = reader.readLine()) != null) {
                    retJSON.append(tmpString.trim() + "\n");
                }
                JSONObject jsonObject = new JSONObject(retJSON.toString());
                String code = jsonObject.getString("code");
                if (code.equals("1")) {
                    String data = jsonObject.getString("data");
                    data = data.substring(1,data.length()-1);
                    JSONObject jsonData = new JSONObject(data);
                    MainActivity.ServiceVersion = jsonData.getString("clientVersion");
                    MainActivity.verText = jsonData.getString("updateLog");
                    MainActivity.verAddress = jsonData.getString("downLoad");
                    MainActivity.update = jsonData.getString("choiceUpdate");
                }
            }
        } catch (Exception e) {
        }
        finally{
            conn.disconnect();
            MainActivity.verSwitch = true;
        }
    }
}
