package com.example.mycurrency;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner spinnerFirst,spinnerSecond;
    EditText editText;
    Button button;
    TextView textView2;
    String[] mCurrencise;
    public static final String FOR="FOR_CURRENCY";
    public static final String HOM="HOM_CURRENCY";

    private String mKey;
    public static final String RATES ="rates";
    public static final String URL_BASE="https://openexchangerates.org/api/latest.json?app_id=";
    public static final DecimalFormat DECIMAL_FORMAT=new DecimalFormat("#,##0.00000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("test", "onCreate: MainActivity  开始");

        spinnerFirst=findViewById(R.id.spinnerFirst);
        spinnerSecond=findViewById(R.id.spinnerSecond);
        editText=findViewById(R.id.editText);
        button=findViewById(R.id.button);
        textView2=findViewById(R.id.textView2);

        ArrayList<String> arrayList=((ArrayList<String>)getIntent().getSerializableExtra(SplashActivity.KEY_ARRAYLIST));
        Collections.sort(arrayList);
        mCurrencise=arrayList.toArray(new String[arrayList.size()]);

        ArrayAdapter<String> arrayAdapter =new ArrayAdapter<>(this,R.layout.spinner_closed,mCurrencise);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFirst.setAdapter(arrayAdapter);
        spinnerSecond.setAdapter(arrayAdapter);

        spinnerFirst.setOnItemSelectedListener(this);
        spinnerSecond.setOnItemSelectedListener(this);

        //保存信息
        if(savedInstanceState==null && (PrefsMgr.getString(this,FOR)==null && PrefsMgr.getString(this,HOM)==null)){
            spinnerFirst.setSelection(findPositionGivenCode("USD",mCurrencise));
            spinnerSecond.setSelection(findPositionGivenCode("CNY",mCurrencise));

            PrefsMgr.setString(this,FOR,"USD");
            PrefsMgr.setString(this,HOM,"CNY");
        }else{
            spinnerFirst.setSelection(findPositionGivenCode(PrefsMgr.getString(this,FOR),mCurrencise));
            spinnerSecond.setSelection(findPositionGivenCode(PrefsMgr.getString(this,HOM),mCurrencise));
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CurrencyConverterTask().execute(URL_BASE+mKey);
            }
        });

        mKey=getKey("open_key");
    }

    /**
     * 重写activity中创建菜单的选项
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //通过inflater对象将自己写的资源文件转为menu对象
        //参数1代表需要创建的菜单，参数2代表将菜单设置到对应的menu上
        getMenuInflater().inflate(R.menu.menu_main,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch(id){
            case R.id.mnu_codes:
                launchBrowser(SplashActivity.URL_CODES);
                Log.d("test", "onOptionsItemSelected: mnu_codes    查看钱币代码");
                break;
            case  R.id.mnu_exit:
                Log.d("test", "onOptionsItemSelected: mnu_exit     退出");
                break;
            case R.id.mnu_invert:
                invertCurrencies();
                Log.d("test", "onOptionsItemSelected: mnu_invert   钱币转换");
                break;
            case R.id.mnu_record:
                Log.d("test", "onOptionsItemSelected: mnu_record   查看记录");
                break;
        }
        return true;
    }

    public boolean isOnline(){
        ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnectedOrConnecting()){
            Log.d("test", "isOnline: 以联网");
            return true;
        }
        return false;
    }

    private void launchBrowser(String strUri){
        if(isOnline()){
            Uri uri=Uri.parse(strUri);
            Intent intent=new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);
        }
    }

    private void invertCurrencies(){
        int nFir=spinnerFirst.getSelectedItemPosition();
        int nSec=spinnerSecond.getSelectedItemPosition();
        spinnerFirst.setSelection(nSec);
        spinnerSecond.setSelection(nFir);
        textView2.setText("");
        PrefsMgr.setString(this,FOR,extractCodeFromCurrency((String)spinnerFirst.getSelectedItem()));
        PrefsMgr.setString(this,HOM,extractCodeFromCurrency((String)spinnerSecond.getSelectedItem()));
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        switch (adapterView.getId()){
            case R.id.spinnerFirst:
                PrefsMgr.setString(this,FOR,extractCodeFromCurrency((String)spinnerFirst.getSelectedItem()));
                break;
            case R.id.spinnerSecond:
                PrefsMgr.setString(this,HOM,extractCodeFromCurrency((String)spinnerSecond.getSelectedItem()));
                break;
        }
        textView2.setText("");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private int findPositionGivenCode(String code,String[] currencise){
        for(int i=0;i<currencise.length;i++){
            if(extractCodeFromCurrency(mCurrencise[i]).equalsIgnoreCase(code)){
                return i;
            }
        }
        return 0;
    }

    private String extractCodeFromCurrency(String s) {
        return (s).substring(0,3);
    }

    private String getKey(String keyName){
        AssetManager assetManager=this.getResources().getAssets();
        Properties properties=new Properties();
        try {
            InputStream inputStream=assetManager.open("keys.properties");
            properties.load(inputStream);
        }catch (IOException e){
            e.printStackTrace();
        }
        return properties.getProperty(keyName);
    }

    private class CurrencyConverterTask extends AsyncTask<String,Void, JSONObject>{
        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            progressDialog=new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("计算结果...");
            progressDialog.setMessage("计算中...");
            progressDialog.setCancelable(true);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CurrencyConverterTask.this.cancel(true);
                    progressDialog.dismiss();
                }
            });
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            //super.onPostExecute(jsonObject);
            double dCalculated =0.0;
            //strForCode外币币种代码
            String strForCode=extractCodeFromCurrency(mCurrencise[spinnerFirst.getSelectedItemPosition()]);
            //strHomCode本地币种代码
            String strHomCode=extractCodeFromCurrency(mCurrencise[spinnerSecond.getSelectedItemPosition()]);
            String strAmount=editText.getText().toString();
            try{
                if(jsonObject==null){
                    throw new JSONException("no data available.");
                }
                JSONObject jsonRates=jsonObject.getJSONObject(RATES);
                if(strForCode.equalsIgnoreCase("USD")){
                    dCalculated=Double.parseDouble(strAmount)*jsonRates.getDouble(strHomCode);
                }else if(strHomCode.equalsIgnoreCase("USD")){
                    dCalculated=Double.parseDouble(strAmount)/jsonRates.getDouble(strForCode);
                }else{
                    dCalculated=Double.parseDouble(strAmount)*jsonRates.getDouble(strHomCode)/jsonRates.getDouble(strForCode);
                }
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this,"计算出现错误："+e.getMessage(),Toast.LENGTH_LONG).show();
                textView2.setText("");
                e.printStackTrace();
            }
            textView2.setText(DECIMAL_FORMAT.format(dCalculated)+" "+strHomCode);
            progressDialog.dismiss();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            //获取JSONFromUrl中的信息
            return new JSONParser().getJSONFromUrl(params[0]);
        }
    }
}
