package com.example.acessogeodb;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button mBotaoBusca;
    private TextView mTextViewDados;
    Response response;

    private class TarefaBuscaDadosCidades extends AsyncTask<Void, Void, DadosCidade> {
        @Override
        protected DadosCidade doInBackground(Void... params) {
            DadosCidade dadosGeoDB = null;
            // chama endpoint para busca do "wikiDataId" da cidade


            // chama endpoint com dados da cidade
            OkHttpClient client = new OkHttpClient();
            String url = "https://wft-geo-db.p.rapidapi.com/v1/geo/cities/Q817216";
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
        mBotaoBusca = (Button) findViewById(R.id.botaoBusca);
        mBotaoBusca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask<Void, Void, DadosCidade> tar = new TarefaBuscaDadosCidades();
                tar.execute();
            }
        });
    }
}