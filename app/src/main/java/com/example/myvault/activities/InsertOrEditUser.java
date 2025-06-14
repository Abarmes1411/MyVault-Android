package com.example.myvault.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myvault.R;
import com.example.myvault.models.User;
import com.example.myvault.services.UserService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class InsertOrEditUser extends AppCompatActivity {


    private Button btnSave, btnCancel;
    private EditText etConfirmPassword, etName, etSurname, etEmail, etPassword, etUsername;
    private MaterialSwitch swRole;
    private User userEdit;
    private User user;

    private UserService userService;

    private FirebaseAuth instanceAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_insert_or_edit_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();



        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InsertOrEditUser.this, LoginActivity.class);

                startActivity(intent);
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validar campos
                if (validateFields()) {
                    // Instancia del módulo de Authenticación
                    instanceAuth = FirebaseAuth.getInstance();

                    if(instanceAuth.getCurrentUser() == null) {
                        instanceAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                            if (currentUser != null) {
                                                createUser();
                                                Toast.makeText(InsertOrEditUser.this, "¡Bienvenido a myVault!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(InsertOrEditUser.this, "Error inesperado: usuario no autenticado tras crear cuenta.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(InsertOrEditUser.this, "Error al crear el usuario", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    } else {
                            createUser();
                            Toast.makeText(InsertOrEditUser.this, "Usuario creado2", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }

    // Función para validar los campos
    private boolean validateFields() {
        // Validar nombre
        String name = etName.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(InsertOrEditUser.this, "El nombre no puede estar vacío.", Toast.LENGTH_SHORT).show();
        }

        if (name.length() < 1) {
            Toast.makeText(InsertOrEditUser.this, "El nombre debe tener al menos 1 caracteres.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar apellido
        String surname = etSurname.getText().toString();
        if (surname.isEmpty()) {
            Toast.makeText(InsertOrEditUser.this, "El apellido no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (surname.length() < 1) {
            Toast.makeText(InsertOrEditUser.this, "El apellido debe tener al menos 1 caracteres.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar email
        String email = etEmail.getText().toString();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(InsertOrEditUser.this, "Por favor ingrese un email válido.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar contraseña
        String password = etPassword.getText().toString();
        if (password.length() < 8) {
            Toast.makeText(InsertOrEditUser.this, "La contraseña debe tener al menos 8 caracteres.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar confirmación de contraseña
        String confirmPassword = etConfirmPassword.getText().toString();
        if (!password.equals(confirmPassword)) {
            Toast.makeText(InsertOrEditUser.this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
            return false;
        }

        String username = etUsername.getText().toString();
        if (username.isEmpty()) {
            Toast.makeText(InsertOrEditUser.this, "El nombre de usuario no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (username.length() < 6) {
            Toast.makeText(InsertOrEditUser.this, "El nombre de usuario debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return false;
        }



        return true;
    }


    private void createUser(){
        user = new User();
        user.setName(etName.getText().toString());
        user.setUsername(etUsername.getText().toString());
        user.setSurname(etSurname.getText().toString());
        user.setEmail(etEmail.getText().toString());
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        user.setId(currentUser.getUid());


        if(etPassword.getText().toString().equals(etConfirmPassword.getText().toString())){
                userService.insert(user);
                Toast.makeText(InsertOrEditUser.this, "Se ha insertado al usuario1", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(InsertOrEditUser.this, MainActivity.class);
                startActivity(intent);
            }else{
                Toast.makeText(InsertOrEditUser.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            }

    }


    private void loadComponents() {
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etUsername = findViewById(R.id.etUsername);

        userService = new UserService(getApplicationContext());



    }
}