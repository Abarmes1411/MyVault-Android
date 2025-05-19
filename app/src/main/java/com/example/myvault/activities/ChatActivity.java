package com.example.myvault.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myvault.R;
import com.example.myvault.adapters.ChatAdapter;
import com.example.myvault.models.CustomList;
import com.example.myvault.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String chatKey;
    private ImageButton btnSendCustomList, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();
    }

    private void loadComponents() {
        // Recoger el ID del usuario con el que se quiere chatear y el autenticado.
        String chatUserId = getIntent().getStringExtra("chatUserId");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("ChatActivity", "ID del usuario con el que se chateará: " + chatUserId);
        Log.d("ChatActivity", "ID del usuario autenticado: " + currentUserId);

        btnBack = findViewById(R.id.btnBack);
        btnSendCustomList = findViewById(R.id.btnSendCustomList);

        // Generar una clave única para el chat.
        chatKey = generateChatKey(currentUserId, chatUserId);
        Log.d("ChatActivity", "Clave de chat generada: " + chatKey);

        // Referenciar el nodo chats con la clave generada.
        DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(chatKey);

        // Verificar si ya existe el nodo del chat.
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Si no existe, crear el nodo del chat.
                    Map<String, Object> chatData = new HashMap<>();
                    // Almacenar los UIDs de los participantes en una lista.
                    Map<String, Boolean> chatUsersMap = new HashMap<>();
                    chatUsersMap.put(currentUserId, true);
                    chatUsersMap.put(chatUserId, true);
                    chatData.put("users", chatUsersMap);

                    // Crear un nodo vacío para los mensajes.
                    chatData.put("messages", new HashMap<String, Object>());

                    chatRef.setValue(chatData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("ChatActivity", "Chat creado exitosamente");
                                initChatInterface();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ChatActivity", "Error al crear el chat", e);
                            });
                } else {
                    Log.d("ChatActivity", "El chat ya existe");
                    initChatInterface();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatActivity", "Error en la consulta del chat", error.toException());
            }
        });

        // Botón para regresar.
        btnBack.setOnClickListener(view -> {
            finish();
        });

        // Botón para enviar listas personalizadas.
        btnSendCustomList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference listsRef = FirebaseDatabase.getInstance()
                        .getReference("users").child(userID).child("customLists");

                listsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(ChatActivity.this, "No tienes listas creadas", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<String> listNames = new ArrayList<>();
                        for (DataSnapshot listSnapshot : snapshot.getChildren()) {
                            listNames.add(listSnapshot.getKey());
                        }

                        String[] listArray = listNames.toArray(new String[0]);
                        final int[] selectedIndex = {-1};

                        new AlertDialog.Builder(ChatActivity.this)
                                .setTitle("Selecciona una lista para compartir")
                                .setSingleChoiceItems(listArray, -1, (dialog, which) -> {
                                    selectedIndex[0] = which;
                                })
                                .setPositiveButton("Confirmar", (dialog, which) -> {
                                    if (selectedIndex[0] != -1) {
                                        String selectedListKey = listNames.get(selectedIndex[0]);
                                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                        DatabaseReference selectedListRef = FirebaseDatabase.getInstance()
                                                .getReference("users")
                                                .child(currentUserId)
                                                .child("customLists")
                                                .child(selectedListKey);

                                        selectedListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                CustomList selectedList = snapshot.getValue(CustomList.class);
                                                if (selectedList != null) {
                                                    selectedList.setId(selectedListKey);

                                                    String timestamp = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                                                    Message listMessage = new Message("custom_list", chatKey, currentUserId, "", timestamp);
                                                    listMessage.setCustomList(selectedList);

                                                    DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                                                            .getReference("chats")
                                                            .child(chatKey)
                                                            .child("messages");

                                                    messagesRef.push().setValue(listMessage)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(ChatActivity.this, "Lista enviada", Toast.LENGTH_SHORT).show();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(ChatActivity.this, "Error al enviar la lista", Toast.LENGTH_SHORT).show();
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(ChatActivity.this, "Error al obtener lista", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } else {
                                        Toast.makeText(ChatActivity.this, "No seleccionaste ninguna lista", Toast.LENGTH_SHORT).show();
                                    }
                                })

                                .setNegativeButton("Cancelar", null)
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, "Error al cargar listas", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    // Función auxiliar para generar la clave única del chat usando los UIDs de los usuarios.
    private String generateChatKey(String uid1, String uid2) {
        if (uid1.compareTo(uid2) < 0) {
            return "chat_" + uid1 + "_" + uid2;
        } else {
            return "chat_" + uid2 + "_" + uid1;
        }
    }

    private void initChatInterface() {
        RecyclerView rvChatMessages = findViewById(R.id.rvChatMessages);
        EditText etMessageInput = findViewById(R.id.etMessageInput);
        ImageButton btnSend = findViewById(R.id.btnSend);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvChatMessages.setLayoutManager(layoutManager);

        final List<Message> messagesList = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(this, messagesList);
        rvChatMessages.setAdapter(chatAdapter);

        // Referencia al nodo de mensajes del chat.
        DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(chatKey)
                .child("messages");

        // Listener en tiempo real para los mensajes del chat.
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesList.clear();
                // Recorrer todos los mensajes.
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Message message = ds.getValue(Message.class);
                    if (message != null) {
                        messagesList.add(message);
                    }
                }
                chatAdapter.notifyDataSetChanged();
                // Desplazar automáticamente al último mensaje.
                if (!messagesList.isEmpty()) {
                    rvChatMessages.scrollToPosition(messagesList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatActivity", "Error al recuperar mensajes: " + error.getMessage());
            }
        });

        // Listener para el botón de enviar mensaje.
        btnSend.setOnClickListener(view -> {
            String messageText = etMessageInput.getText().toString().trim();
            Log.d("ChatActivity", "Mensaje enviado: " + messageText);
            if (messageText.isEmpty()) {
                return;
            }
            // Obtener timestamp formateado.
            String timestamp = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date());
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Crear un objeto Message. Se utiliza "text" en el campo type.
            Message newMessage = new Message("text", chatKey, currentUserId, messageText, timestamp);
            Log.d("ChatActivity", "Mensaje creado: " + newMessage);
            DatabaseReference newMessageRef = messagesRef.push();
            newMessageRef.setValue(newMessage)
                    .addOnSuccessListener(aVoid -> {
                        etMessageInput.setText("");
                        rvChatMessages.scrollToPosition(messagesList.size() - 1);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ChatActivity", "Error al enviar mensaje: ", e);
                    });
        });
    }
}