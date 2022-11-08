package com.example.kryptoapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

            private RequestQueue requestQueue;
            TextView textView;
            LinearLayout cryptoDataContainer;
            private ListView cryptoNameList;
            private ListView cryptoPriceList;
            private ListView cryptoChangeList;
            private ArrayList<String> favouriteCryptoNames;
            private ArrayList<String> favouriteCryptoPrices;
            private ArrayList<String> favouriteCryptoChanges;

            private ArrayAdapter<String> nameAdapter;
            private ArrayAdapter<String> priceAdapter;
            private ArrayAdapter<String> changeAdapter;
            private BigDecimal bd;
            private Bundle args;
            private long lastUpdateTime;
            private long minUpdateTime = 1000*20;   // Asettaa ajan, kuinka usein numeroita voi päivittää (20s)
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                requestQueue = Volley.newRequestQueue(this);

                if(getIntent().getBundleExtra("BUNDLE") != null) {
                    args = getIntent().getBundleExtra("BUNDLE");
                    favouriteCryptoNames = (ArrayList<String>) args.getSerializable("nameList");
                    favouriteCryptoPrices  = (ArrayList<String>) args.getSerializable("priceList");
                    favouriteCryptoChanges = (ArrayList<String>) args.getSerializable("changeList");
                    updateUi();
                }

            }


            public void changePage(View view) /*throws JSONException*/ {
                Intent intent = new Intent(this, addCryptoActivity.class);
                intent.putExtra("BUNDLE", args);
                startActivity(intent);
            }

            public void fetchCryptoData(String _crypto, int _index) throws JSONException {
                String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + _crypto + "&vs_currencies=eur&include_24hr_change=true";

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.length() != 0) {
                                // Parsii datan ja pyöristää numerot
                                float price = Float.parseFloat(response.getJSONObject(_crypto).getString("eur"));
                                float change = Float.parseFloat(response.getJSONObject(_crypto).getString("eur_24h_change"));
                                favouriteCryptoNames.set(_index, _crypto);
                                bd = new BigDecimal(price);
                                bd = bd.round(new MathContext(4));
                                favouriteCryptoPrices.set(_index,bd.floatValue()+ "€");

                                bd = new BigDecimal(change);
                                bd = bd.round(new MathContext(2));
                                favouriteCryptoChanges.set(_index,bd.floatValue() + "%");
                            } else
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.noCryptosText), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.internetErrorText), Toast.LENGTH_SHORT).show();

                    }
                });
                requestQueue.add(jsonObjectRequest);
            }

            public void updateNumbers(View view){
                if (System.currentTimeMillis() - lastUpdateTime > minUpdateTime)
                {
                    try {
                        for (int i = 0; i < favouriteCryptoNames.size(); i++) {
                            fetchCryptoData(favouriteCryptoNames.get(i), i);
                        }
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.updateNumbersText), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.noCryptosText), Toast.LENGTH_SHORT).show();

                    }
                    lastUpdateTime = System.currentTimeMillis();
                }

            }

            public void updateUi()
            {
                cryptoNameList = findViewById(R.id.nameList);
                cryptoPriceList = findViewById(R.id.priceList);
                cryptoChangeList = findViewById(R.id.changeList);

                // Lisää otsikot
                textView = new TextView(getApplicationContext());
                textView.setTextColor(Color.WHITE);
                textView.setText((String)"Krypto");
                cryptoNameList.addHeaderView(textView);
                textView = new TextView(getApplicationContext());
                textView.setTextColor(Color.WHITE);
                textView.setText((String)"Hinta");
                cryptoPriceList.addHeaderView(textView);
                textView = new TextView(getApplicationContext());
                textView.setTextColor(Color.WHITE);
                textView.setText((String)"Muutos");
                cryptoChangeList.addHeaderView(textView);

                // Lisää adapterit listvieweille
                nameAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_white_text, R.id.list_content, favouriteCryptoNames);
                cryptoNameList.setAdapter(nameAdapter);

                priceAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_white_text, R.id.list_content, favouriteCryptoPrices);
                cryptoPriceList.setAdapter(priceAdapter);

                changeAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_white_text, R.id.list_content, favouriteCryptoChanges);
                cryptoChangeList.setAdapter(changeAdapter);
            }
}