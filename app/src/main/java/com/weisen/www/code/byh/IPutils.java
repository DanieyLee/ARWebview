package com.weisen.www.code.byh;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IPutils {

    public static void GetNetIp(){
        HttpURLConnection connection = null;
        try {
            String address = "http://ip.taobao.com/service/getIpInfo2.php?ip=myip";
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.7 Safari/537.36"); //设置浏览器ua 保证不出现503
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String tmpString = "";
                StringBuilder retJSON = new StringBuilder();
                while ((tmpString = reader.readLine()) != null) {
                    retJSON.append(tmpString + "\n");
                }
                JSONObject jsonObject = new JSONObject(retJSON.toString());
                String code = jsonObject.getString("code");
                if (code.equals("0")) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    MainActivity.ip = data.getString("ip");
                    MainActivity.address = data.getString("area")
                            + data.getString("region") + "省"
                            + data.getString("city") + "市";
                    MainActivity.network = data.getString("country") + data.getString("isp");
                } else {
                    MainActivity.ip = "IP接口异常！";
                }
            } else {
                MainActivity.ip = "网络连接异常！";
            }
        } catch (Exception e) {
            MainActivity.ip = "获取IP地址错误";
        }finally{
            connection.disconnect();
            MainActivity.ipSwitch = true;
        }
    }
}