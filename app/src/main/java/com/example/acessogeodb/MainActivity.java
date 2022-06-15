package com.example.acessogeodb;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button mBotaoBuscaId;
    private Button mBotaoBuscaDados;
    private TextView mTextViewDados;
    private EditText mEditText;
    Response response;
    String wikiId = null;
    Editable nomeCidade = null;

    private class TarefaBuscaWikiId extends AsyncTask<Void, Void, String> {
        @Override
        public String doInBackground(Void... params) {
            DadosCidade dadosGeoDB = null;
            // chama endpoint para busca do "wikiDataId" da cidade
            if (nomeCidade != null)
                wikiId = getWikiDataID(nomeCidade);
            //wikiId = getWikiDataID("Poços de Caldas");
            return wikiId;
        }

        @Override
        public void onPostExecute(String wikiid) {
            Log.i(TAG, "onPostExecute: TarefaBuscaWikiId executada");
            mTextViewDados = (TextView) findViewById(R.id.view_texto_dos_dados);
            mTextViewDados.setText(wikiid);
        }
    }

    private class TarefaBuscaDadosCidades extends AsyncTask<Void, Void, DadosCidade> {
        @Override
        protected DadosCidade doInBackground(Void... params) {
            DadosCidade dadosGeoDB = null;
            if (wikiId != null)
                dadosGeoDB = getDadosCidade(wikiId);
            return dadosGeoDB;
        }

        @Override
        public void onPostExecute(DadosCidade resultado) {
            Log.i(TAG, "onPostExecute: metodo executado");
            if (resultado != null) {
                Log.i(TAG, "onPostExecute: dados recebidos: " + resultado.getCidade());
                String texto = resultado.getCidade() + "\n" +
                        resultado.getEstado() + "\n" +
                        resultado.getPais() + "/" + resultado.getCod_pais() + "\n" +
                        resultado.getPopulacao() + " habitantes" + "\n" +
                        resultado.getElevacao() + " m" + "\n" +
                        resultado.getLatitude() + " latitude" + "\n" +
                        resultado.getLongitude() + " longitude" + "\n";

                mTextViewDados = (TextView) findViewById(R.id.view_texto_dos_dados);
                mTextViewDados.setText(texto);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mTextViewDados = findViewById(R.id.view_texto_dos_dados);
        mBotaoBuscaId = (Button) findViewById(R.id.botaoBuscaId);
        mBotaoBuscaId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditText = (EditText) findViewById(R.id.textoNomeCidade);
                nomeCidade = mEditText.getText();
                AsyncTask<Void, Void, String> tar = new TarefaBuscaWikiId();
                tar.execute();
            }
        });

        mBotaoBuscaDados = (Button) findViewById(R.id.botaoBuscaDados);
        mBotaoBuscaDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask<Void, Void, DadosCidade> tar = new TarefaBuscaDadosCidades();
                tar.execute();
            }
        });
    }

    String getWikiDataID(Editable cidade) {
        String wikidataid = null;
        OkHttpClient client = new OkHttpClient();
        String url = "https://wft-geo-db.p.rapidapi.com/v1/geo/cities?namePrefix=" + cidade;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-RapidAPI-Host", "wft-geo-db.p.rapidapi.com")
                .addHeader("X-RapidAPI-Key", "8cfb61b3f0msh679aa8dea496f98p10325fjsne48e9885f0a9")
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            String responseBody = response.body().string();

            // parse do item recebido
            JSONObject corpoJson = new JSONObject(responseBody);
            JSONArray dataJsonArray = corpoJson.getJSONArray("data");
            JSONObject dataJasonObject = dataJsonArray.getJSONObject(0);
            wikidataid = dataJasonObject.getString("wikiDataId");
            Log.i(TAG, "doInBackground: " + responseBody);
            Log.i(TAG, "doInBackground/wikiDataId: " + wikidataid);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return wikidataid;
    }

    DadosCidade getDadosCidade(String wikiId) {
        DadosCidade dadosGeoDB = null;
        OkHttpClient client = new OkHttpClient();
        String url = "https://wft-geo-db.p.rapidapi.com/v1/geo/cities/" + wikiId;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-RapidAPI-Host", "wft-geo-db.p.rapidapi.com")
                .addHeader("X-RapidAPI-Key", "8cfb61b3f0msh679aa8dea496f98p10325fjsne48e9885f0a9")
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            String responseBody = response.body().string();

            // parse do item recebido
            JSONObject corpoJson = new JSONObject(responseBody);
            JSONObject dataJsonObject = corpoJson.getJSONObject("data");
            dadosGeoDB = new DadosCidade(
                    dataJsonObject.getString("id"),
                    dataJsonObject.getString("name"),
                    dataJsonObject.getString("region"),
                    dataJsonObject.getString("country"),
                    dataJsonObject.getString("countryCode"),
                    dataJsonObject.getString("elevationMeters"),
                    dataJsonObject.getString("latitude"),
                    dataJsonObject.getString("longitude"),
                    (int) new Integer(dataJsonObject.getString("population"))
            );
            Log.i(TAG, "doInBackground: " + responseBody);
            Log.i(TAG, "doInBackground/cidade: " + dataJsonObject.getString("name"));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return dadosGeoDB;
    }
}