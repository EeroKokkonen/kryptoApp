package com.example.kryptoapp;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.math.BigDecimal;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class addCryptoActivity extends AppCompatActivity {


    private RequestQueue requestQueue;
    private List<String> favouriteCryptoNames = new ArrayList<String>();
    private List<String> favouriteCryptoPrices = new ArrayList<String>();
    private List<String> favouriteCryptoChanges = new ArrayList<String>();

    private ArrayAdapter<String> commonCryptoAdapter;
    private ArrayList<String> commonCryptos = new ArrayList<String>(Arrays.asList("Bitcoin","Ethereum",
            "Binancecoin", "Dogecoin", "Cardano", "Solana", "Shiba-Inu", "Polkadot"));

    private ListView commonCryptoList;

    private BigDecimal bd;

    private EditText userInput;
    Bundle args = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_crypto);

        commonCryptoList = findViewById(R.id.commonCryptoList);
        setCommonCryptoList();

        requestQueue = Volley.newRequestQueue(this);

        if(getIntent().getBundleExtra("BUNDLE") != null) {
            args = getIntent().getBundleExtra("BUNDLE");
            favouriteCryptoNames = (ArrayList<String>) args.getSerializable("nameList");
            favouriteCryptoPrices  = (ArrayList<String>) args.getSerializable("priceList");
            favouriteCryptoChanges = (ArrayList<String>) args.getSerializable("changeList");
        }
    }

    public void fetchCryptoData(View view) throws JSONException {
        // Ottaa käyttäjän kirjoittaman tekstin
        EditText userInput = (EditText)findViewById(R.id.cryptoInput);
        String crypto = userInput.getText().toString().toLowerCase();

        String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + crypto + "&vs_currencies=eur&include_24hr_change=true";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Parsii datan ja pyöristää numerot
                            if (response.length() != 0) {
                                float price = Float.parseFloat(response.getJSONObject(crypto).getString("eur"));

                                float change = Float.parseFloat(response.getJSONObject(crypto).getString("eur_24h_change"));
                                favouriteCryptoNames.add(crypto);
                                bd = new BigDecimal(price);
                                bd = bd.round(new MathContext(4));
                                favouriteCryptoPrices.add(bd.floatValue()+ "€");

                                bd = new BigDecimal(change);
                                bd = bd.round(new MathContext(2));
                                favouriteCryptoChanges.add(bd.floatValue() + "%");
                                userInput.setText("");
                                Toast.makeText(getApplicationContext(), crypto + " " + getResources().getString(R.string.newCryptoText), Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalidInputText), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.internetErrorText), Toast.LENGTH_LONG).show();

                    }
                });
        requestQueue.add(jsonObjectRequest);
        }


        public void backToMenu(View view)
        {
            Intent intent = new Intent(this, MainActivity.class);

            // Lisää kryptojen nimet bundleen
            args.putSerializable("nameList", (Serializable)favouriteCryptoNames);

            // Lisää kryptojen arvot bundleen
            args.putSerializable("priceList", (Serializable)favouriteCryptoPrices);

            // Lisää kryptojen muutokset bundleen
            args.putSerializable("changeList", (Serializable)favouriteCryptoChanges);

            // Lisää bundlen intenttiin
            intent.putExtra("BUNDLE", args);
            startActivity(intent);
        }

    public void setCommonCryptoList() {
        commonCryptoAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_white_text, R.id.list_content, commonCryptos);
        commonCryptoList.setAdapter(commonCryptoAdapter);
        userInput = findViewById(R.id.cryptoInput);
        commonCryptoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                userInput.setText(parent.getItemAtPosition(position).toString());

            }
        });
    }
}