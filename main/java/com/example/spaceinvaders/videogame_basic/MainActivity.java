package com.example.spaceinvaders.videogame_basic;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.ViewTreeObserver;

import com.example.spaceinvaders.R;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private Juego juego;
    private Handler handler = new Handler();
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        juego = findViewById(R.id.Pantalla);
        if (juego == null) {
            throw new RuntimeException("Error: No se encontró el ID 'Pantalla' en activity_main.xml");
        }

        ViewTreeObserver obs = juego.getViewTreeObserver();
        obs.addOnGlobalLayoutListener(() -> {
            juego.setDimensiones(juego.getWidth(), juego.getHeight());

            iniciarTimers();
        });
    }

    private void iniciarTimers() {
        timer = new Timer();

        // Timer para actualizar el juego (60fps)
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> juego.actualizarJuego());
            }
        }, 0, 16);

        // Timer para generar enemigos cada 2 segundos
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> juego.generarEnemigo());
            }
        }, 2000, 2000); // cada 2s
    }

    // Reiniciar el juego al presionar tecla ENTER (opcional)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && juego.isJuegoTerminado()) {
            juego.reset();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel(); // limpiar timers para evitar fugas de memoria
        }
    }
}
