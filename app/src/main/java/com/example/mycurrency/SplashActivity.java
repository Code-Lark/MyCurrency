package com.example.mycurrency;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class SplashActivity extends AppCompatActivity {

    public static final String URL_CODES="https://openexchangerates.org/api/currencies.json";
    public static final String KEY_ARRAYLIST = "key_arraylist";
    private ArrayList<String> mCurrencies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        Log.d("test", "onCreate: 准备FetchCodesTask");
        new FetchCodesTask().execute(URL_CODES);
    }

    private class FetchCodesTask extends AsyncTask<String,Void, JSONObject>{
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            //super.onPostExecute(jsonObject);
            try {
                if(jsonObject==null){
                    throw new JSONException("no data available.");
                }
                Log.d("test", "onPostExecute: jsonObject!=null");
                Iterator iterator=jsonObject.keys();
                String key="";
                mCurrencies=new ArrayList<>();
                Log.d("test", "onPostExecute: 准备while循环");
                while(iterator.hasNext()){
                    key=(String)iterator.next();
                    mCurrencies.add(key+"|"+jsonObject.getString(key));
                }
                Log.d("test", "onPostExecute: SplashActivity->MainActivity  准备跳转");
                //跳转页面
                Intent mainIntent=new Intent(SplashActivity.this,MainActivity.class);
                mainIntent.putExtra(KEY_ARRAYLIST,mCurrencies);
                startActivity(mainIntent);
                Log.d("test", "onPostExecute: SplashActivity->MainActivity  成功跳转");

                finish();
            }catch (JSONException e){
                Toast.makeText(SplashActivity.this,"There's been a JSON exception:"+e.getMessage(),Toast.LENGTH_LONG).show();
                e.printStackTrace();
                finish();
            }
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return new JSONParser().getJSONFromUrl(params[0]);
        }
    }
}
