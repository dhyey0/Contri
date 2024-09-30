
package com.example.contri;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {
    Button bt,b ;
    EditText Email,Pass ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        b=findViewById(R.id.button);
        Pass=findViewById(R.id.editTextTextPassword);
        Email=findViewById(R.id.editTextText4);

        FirebaseAuth auth ;
        auth = FirebaseAuth.getInstance();



        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email=Email.getText().toString() ;
                String pass=Pass.getText().toString() ;
                String patt="[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(login.this, "enter an valid email", Toast.LENGTH_SHORT).show();
                }
                else if (!(email.matches(patt)))
                {
                    Toast.makeText(login.this, "enter an valid email", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(pass))
                {
                    Toast.makeText(login.this, "enter your password", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {

                                Intent intent=new Intent(login.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            }
                            else
                            {
                                Toast.makeText(login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
































        bt = findViewById(R.id.button2);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(login.this,signup.class);
                startActivity(intent);
            }
        });

    }
}

