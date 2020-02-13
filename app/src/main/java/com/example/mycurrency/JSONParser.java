package com.example.mycurrency;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class JSONParser {
    static InputStream sInputStream=null;
    static JSONObject sReturnJsonObject=null;
    static String sRawJsonString="";
    public JSONParser(){}
    public JSONObject getJSONFromUrl(String url){
        try{
            URL urlObj=new URL(url);
            //得到connection对象。
            HttpURLConnection connection=(HttpURLConnection)urlObj.openConnection();
            //设置请求方式
            connection.setRequestMethod("GET");
            //连接
            connection.connect();
            //得到响应码
            int responseCode=connection.getResponseCode();
            if(responseCode==HttpURLConnection.HTTP_OK){
                //得到响应流
                sInputStream=connection.getInputStream();
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            //BufferedReader reader=new BufferedReader(new InputStreamReader(sInputStream,"utf-8"),8);
            BufferedReader reader=new BufferedReader(new InputStreamReader(sInputStream,"iso-8859-1"),8);
            StringBuilder stringBuilder=new StringBuilder();
            String line=null;
            while((line=reader.readLine())!=null){
                stringBuilder.append(line+"\n");
            }
            sInputStream.close();
            sRawJsonString=stringBuilder.toString();
        }catch (Exception e){
            Log.e(this.getClass().getSimpleName(),"Error reading fron Buffer:"+e.toString());
        }
        try {
            sReturnJsonObject=new JSONObject(sRawJsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sReturnJsonObject;
    }

}
