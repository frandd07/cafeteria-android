package com.example.cafeteria_android;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private static final int SPLASH_TIMEOUT = 2000; // 2 segundos
    private TextView loadingText;
    private Handler dotsHandler;
    private Runnable dotsRunnable;
    private int dotsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        View outerRing = findViewById(R.id.outer_ring);
        View middleRing = findViewById(R.id.middle_ring);
        View innerRing = findViewById(R.id.inner_ring);
        View pingEffect = findViewById(R.id.ping_effect);
        loadingText = findViewById(R.id.loading_text);

        // Animación de rotación para el anillo exterior (sentido horario)
        ObjectAnimator outerRotation = ObjectAnimator.ofFloat(outerRing, "rotation", 0f, 360f);
        outerRotation.setDuration(3000);
        outerRotation.setRepeatCount(ObjectAnimator.INFINITE);
        outerRotation.start();

        // Animación de rotación para el anillo medio (sentido contrario)
        ObjectAnimator middleRotation = ObjectAnimator.ofFloat(middleRing, "rotation", 0f, -360f);
        middleRotation.setDuration(1500);
        middleRotation.setRepeatCount(ObjectAnimator.INFINITE);
        middleRotation.start();

        // Animación de pulso para el anillo interior
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(innerRing, "scaleX", 1f, 1.2f, 1f);
        scaleX.setDuration(1000);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.start();

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(innerRing, "scaleY", 1f, 1.2f, 1f);
        scaleY.setDuration(1000);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.start();

        // Efecto "ping": fade in y fade out en la vista superpuesta
        ObjectAnimator pingAlpha = ObjectAnimator.ofFloat(pingEffect, "alpha", 0f, 1f, 0f);
        pingAlpha.setDuration(1000);
        pingAlpha.setRepeatCount(ObjectAnimator.INFINITE);
        pingAlpha.start();

        // Animar texto "Cargando" con puntos suspensivos
        dotsHandler = new Handler();
        dotsRunnable = new Runnable() {
            @Override
            public void run() {
                dotsCount = (dotsCount + 1) % 4; // Ciclo de 0 a 3
                StringBuilder dots = new StringBuilder();
                for (int i = 0; i < dotsCount; i++) {
                    dots.append(".");
                }
                loadingText.setText("Cargando" + dots.toString());
                dotsHandler.postDelayed(this, 400);
            }
        };
        dotsHandler.post(dotsRunnable);

        // Tras SPLASH_TIMEOUT, pasar a LoginActivity (o a la pantalla siguiente)
        new Handler().postDelayed(() -> {
            dotsHandler.removeCallbacks(dotsRunnable); // detener animación de puntos
            startActivity(new Intent(MainActivity.this, MainActivity.class)); //TODO: Crear Login activity
            finish();
        }, SPLASH_TIMEOUT);
    }
}