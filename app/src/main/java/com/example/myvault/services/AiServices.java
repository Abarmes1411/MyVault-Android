package com.example.myvault.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myvault.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class AiServices {

    private static final String OPENAI_API_KEY = "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";
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private String DEEPL_KEY;

    private  Context context;
    private  DatabaseReference contentRef;
    private TextView tvAIReview;
    private final OkHttpClient client = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    public AiServices(Context context) {
        this.context = context;
        this.DEEPL_KEY = context.getString(R.string.deepl_api_key);
    }

    // Constructor extendido
    public AiServices(Context context, String contentId, TextView tvAIReview) {
        this(context);
        this.contentRef = FirebaseDatabase.getInstance().getReference("content").child(contentId);
        this.tvAIReview = tvAIReview;
    }

    // Método para cargar las reseñas y generar el resumen si es necesario
    public void loadAndGenerateSummary() {
        contentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    updateTextView("Contenido no encontrado");
                    return;
                }

                // Obtener reseñas de usuarios
                ArrayList<String> reviews = new ArrayList<>();
                DataSnapshot userReviewsSnap = snapshot.child("userReviews");
                for (DataSnapshot reviewSnap : userReviewsSnap.getChildren()) {
                    String comment = reviewSnap.child("comment").getValue(String.class);
                    if (comment != null) reviews.add(comment);
                }

                // Comprobar si hay al menos 3 reseñas
                if (reviews.size() < 3) {
                    updateTextView("Se necesitan al menos 3 reseñas para generar resumen.");
                    return;
                }

                // Obtener cuántas reseñas se usaron en el último resumen
                Long summaryReviewCount = snapshot.child("summaryReviewCount").getValue(Long.class);
                if (summaryReviewCount == null) summaryReviewCount = 0L;

                // Si no hay suficientes reseñas nuevas, se muestra el resumen actual (si existe)
                if (reviews.size() < summaryReviewCount + 3) {
                    String existingSummary = snapshot.child("summaryAI").getValue(String.class);
                    if (existingSummary != null && !existingSummary.isEmpty()) {
                        updateTextView(existingSummary);
                    } else {
                        updateTextView("No hay resumen disponible.");
                    }
                    return;
                }

                // Si hay suficientes reseñas nuevas, se genera un nuevo resumen con OpenAI
                generateSummaryWithOpenAI(reviews);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                updateTextView("Error al leer datos: " + error.getMessage());
            }
        });
    }

    // Método para generar un resumen usando OpenAI a partir de las reseñas
    private void generateSummaryWithOpenAI(ArrayList<String> reviews) {
        // Construir el texto con todas las reseñas enumeradas
        StringBuilder reviewsText = new StringBuilder();
        for (int i = 0; i < reviews.size(); i++) {
            reviewsText.append(i + 1).append(". ").append(reviews.get(i)).append("\n");
        }

        // Crear el prompt que se enviará a la IA
        String prompt = "Eres un crítico profesional de cine, series, anime, manga, videojuegos y novelas ligeras.\n" +
                "Resume de forma muy concisa y objetiva lo que piensan los usuarios sobre esta obra, en un párrafo de máximo 2 líneas.\n" +
                "Usa un estilo neutral, sin opiniones propias.\n\n" +
                "Estas son algunas reseñas:\n\n" +
                reviewsText.toString() +
                "\nDevuelve solo el resumen en español, en un único párrafo corto de 2 líneas como máximo.";

        try {
            // Crear cuerpo JSON para la petición
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();

            // Mensaje del sistema para definir el rol de la IA
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Eres un crítico profesional de entretenimiento");
            messages.put(systemMessage);

            // Mensaje del usuario con el prompt completo
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);

            jsonBody.put("messages", messages);
            jsonBody.put("temperature", 0.7);
            jsonBody.put("max_tokens", 100);

            // Configurar la petición HTTP
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(OPENAI_URL)
                    .header("Authorization", "Bearer " + OPENAI_API_KEY)
                    .post(body)
                    .build();

            // Enviar la petición de forma asíncrona
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    updateTextView("Error generando resumen: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        updateTextView("Error en respuesta OpenAI: " + response.message());
                        return;
                    }

                    String jsonResponse = response.body().string();
                    try {
                        // Parsear respuesta JSON para extraer el resumen
                        JSONObject json = new JSONObject(jsonResponse);
                        String summary = json
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                                .trim();

                        // Guardar resumen y cantidad de reseñas usadas en Firebase
                        contentRef.child("summaryAI").setValue(summary);
                        contentRef.child("summaryReviewCount").setValue(reviews.size());

                        // Mostrar resumen en el TextView
                        updateTextView(summary);

                    } catch (JSONException e) {
                        updateTextView("Error procesando respuesta OpenAI");
                    }
                }
            });

        } catch (JSONException e) {
            updateTextView("Error creando petición JSON");
        }
    }

    // Método para actualizar el contenido del TextView desde el hilo principal
    private void updateTextView(String text) {
        mainHandler.post(() -> tvAIReview.setText(text));
    }

    // Método para traducir texto al español usando la API de DeepL
    public void traducirConDeepL(String textoOriginal, Consumer<String> callback, Context context) {
        String url = "https://api-free.deepl.com/v2/translate";

        // Crear cola de peticiones con Volley
        RequestQueue queue = Volley.newRequestQueue(context);

        // Crear petición POST a la API de DeepL
        StringRequest request = new StringRequest(com.android.volley.Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray translations = jsonResponse.getJSONArray("translations");
                        String textoTraducido = translations.getJSONObject(0).getString("text");
                        callback.accept(textoTraducido);
                    } catch (JSONException e) {
                        Log.e("DeepL", "Error al parsear traducción: " + e.getMessage());
                        callback.accept(textoOriginal);
                    }
                },
                error -> {
                    Log.e("DeepL", "Error al traducir: " + error.toString());
                    callback.accept(textoOriginal);
                }) {

            // Parámetros que se envían en la petición POST
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("auth_key", DEEPL_KEY);
                params.put("text", textoOriginal);
                params.put("target_lang", "ES");
                return params;
            }

            // Procesar la respuesta de red (parsear con UTF-8)
            @Override
            protected com.android.volley.Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, StandardCharsets.UTF_8);
                    return com.android.volley.Response.success(jsonString, HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return com.android.volley.Response.error(new ParseError(e));
                }
            }
        };

        // Añadir la petición a la cola
        queue.add(request);
    }
}
