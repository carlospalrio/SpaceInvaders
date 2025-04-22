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

            new Handler().postDelayed(() -> {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(() -> juego.generarEnemigo());
                    }
                }, 0, 1000); // 🔥 Enemigos más frecuentes
            }, 100);
        });
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            // Ahora verificamos si el juego terminó correctamente
            if (juego.isJuegoTerminado()) {
                reiniciarJuego();
                return true;
            }

            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_W:
                    juego.moverArriba();
                    return true;
                case KeyEvent.KEYCODE_S:
                    juego.moverAbajo();
                    return true;
                case KeyEvent.KEYCODE_SPACE:
                    juego.disparar();
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


    private void reiniciarJuego() {
        juego = new Juego(this, null);
        setContentView(R.layout.activity_main);  // Asegurar que cargamos el layout correcto
        juego = findViewById(R.id.Pantalla);

        ViewTreeObserver obs = juego.getViewTreeObserver();
        obs.addOnGlobalLayoutListener(() -> juego.setDimensiones(juego.getWidth(), juego.getHeight()));
    }


}
