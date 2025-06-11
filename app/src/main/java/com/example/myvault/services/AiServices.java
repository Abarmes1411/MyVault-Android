package com.example.myvault.services;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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

    private final DatabaseReference contentRef;
    private final TextView tvAIReview;
    private final OkHttpClient client = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AiServices(String contentId, TextView tvAIReview) {
        this.contentRef = FirebaseDatabase.getInstance().getReference("content").child(contentId);
        this.tvAIReview = tvAIReview;
    }

    public void loadAndGenerateSummary() {
        contentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    updateTextView("Contenido no encontrado");
                    return;
                }


                ArrayList<String> reviews = new ArrayList<>();
                DataSnapshot userReviewsSnap = snapshot.child("userReviews");
                for (DataSnapshot reviewSnap : userReviewsSnap.getChildren()) {
                    String comment = reviewSnap.child("comment").getValue(String.class);
                    if (comment != null) reviews.add(comment);
                }

                if (reviews.size() < 3) {
                    updateTextView("Se necesitan al menos 3 reseñas para generar resumen.");
                    return;
                }

                Long summaryReviewCount = snapshot.child("summaryReviewCount").getValue(Long.class);
                if (summaryReviewCount == null) summaryReviewCount = 0L;

                if (reviews.size() < summaryReviewCount + 3) {
                    // Mostrar resumen actual si existe
                    String existingSummary = snapshot.child("summaryAI").getValue(String.class);
                    if (existingSummary != null && !existingSummary.isEmpty()) {
                        updateTextView(existingSummary);
                    } else {
                        updateTextView("No hay resumen disponible.");
                    }
                    return;
                }

                // Generar resumen llamando a OpenAI
                generateSummaryWithOpenAI(reviews);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                updateTextView("Error al leer datos: " + error.getMessage());
            }
        });
    }

    private void generateSummaryWithOpenAI(ArrayList<String> reviews) {
        // Construir prompt
        StringBuilder reviewsText = new StringBuilder();
        for (int i = 0; i < reviews.size(); i++) {
            reviewsText.append(i + 1).append(". ").append(reviews.get(i)).append("\n");
        }

        String prompt = "Eres un crítico profesional de cine, series, anime, manga, videojuegos y novelas ligeras.\n" +
                "Resume de forma muy concisa y objetiva lo que piensan los usuarios sobre esta obra, en un párrafo de máximo 2 líneas.\n" +
                "Usa un estilo neutral, sin opiniones propias.\n\n" +
                "Estas son algunas reseñas:\n\n" +
                reviewsText.toString() +
                "\nDevuelve solo el resumen en español, en un único párrafo corto de 2 líneas como máximo.";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Eres un crítico profesional de entretenimiento");
            messages.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);

            jsonBody.put("messages", messages);
            jsonBody.put("temperature", 0.7);
            jsonBody.put("max_tokens", 100);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(OPENAI_URL)
                    .header("Authorization", "Bearer " + OPENAI_API_KEY)
                    .post(body)
                    .build();

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
                        JSONObject json = new JSONObject(jsonResponse);
                        String summary = json
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                                .trim();

                        // Guardar resumen y contador en Firebase
                        contentRef.child("summaryAI").setValue(summary);
                        contentRef.child("summaryReviewCount").setValue(reviews.size());

                        // Mostrar resumen en UI
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

    private void updateTextView(String text) {
        mainHandler.post(() -> tvAIReview.setText(text));
    }
}
