package com.example.spaceinvaders.videogame_basic;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.spaceinvaders.R;

import java.util.*;

public class Juego extends View {
    private int ancho, alto;
    private int posX, posY;
    private Bitmap naveJugador, enemigoBola, fondo, disparoEnemigoImg;

    private final ArrayList<RectF> disparos = new ArrayList<>();
    private final ArrayList<EnemigoCircular> enemigos = new ArrayList<>();
    private final Random random = new Random();
    private final Paint paint = new Paint();

    private boolean juegoTerminado = false;

    private static final int RADIO_ENEMIGO_CPR = 200;
    private static final int VELOCIDAD_ENEMIGO_CPR = 4;
    private static final int VELOCIDAD_DISPARO_ENEMIGO_CPR = 18;
    private static final int VELOCIDAD_DISPARO_JUGADOR_CPR = 15;

    private static final int MAX_DISPAROS_ENEMIGO = 3;  // El número máximo de disparos antes de destruir al enemigo

    public Juego(Context context, AttributeSet attrs) {
        super(context, attrs);
        naveJugador = BitmapFactory.decodeResource(getResources(), R.drawable.btc);
        enemigoBola = BitmapFactory.decodeResource(getResources(), R.drawable.hacker);
        fondo = BitmapFactory.decodeResource(getResources(), R.drawable.hack);
        disparoEnemigoImg = BitmapFactory.decodeResource(getResources(), R.drawable.fireball);

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (juegoTerminado) {
            paint.setColor(Color.WHITE);
            paint.setTextSize(100);
            canvas.drawText("GAME OVER", ancho / 2 - 200, alto / 2, paint);
            return;
        }

        canvas.drawBitmap(fondo, 0, 0, null);
        RectF hitboxJugador = new RectF(posX, posY, posX + 75, posY + 75);
        canvas.drawBitmap(naveJugador, posX, posY, null);

        // Dibujar disparos del jugador
        for (RectF disparo : disparos) {
            canvas.drawRect(disparo, paint);
        }

        // Dibujar enemigos y sus disparos
        for (EnemigoCircular enemigo : enemigos) {
            canvas.drawBitmap(enemigoBola, enemigo.x - 200, enemigo.y - 200, null);
            for (RectF disparo : enemigo.disparos) {
                canvas.drawBitmap(disparoEnemigoImg, disparo.left, disparo.top, null);
            }
        }
    }

    public void actualizarJuego() {
        if (juegoTerminado) return;

        // Mover disparos del jugador
        Iterator<RectF> iterDisparos = disparos.iterator();
        while (iterDisparos.hasNext()) {
            RectF disparo = iterDisparos.next();
            disparo.left += VELOCIDAD_DISPARO_JUGADOR_CPR;
            disparo.right += VELOCIDAD_DISPARO_JUGADOR_CPR;
            if (disparo.left > ancho) iterDisparos.remove();
        }

        RectF hitboxJugador = new RectF(posX, posY, posX + 75, posY + 75);
        Iterator<EnemigoCircular> iterEnemigos = enemigos.iterator();
        while (iterEnemigos.hasNext()) {
            EnemigoCircular enemigo = iterEnemigos.next();
            enemigo.x -= VELOCIDAD_ENEMIGO_CPR;

            if (enemigo.x + RADIO_ENEMIGO_CPR < 0) {
                iterEnemigos.remove();
                continue;
            }

            // Colisión con el jugador
            if (distancia(enemigo.x, enemigo.y, posX + 37, posY + 37) < RADIO_ENEMIGO_CPR + 37) {
                terminarJuego();
                return;
            }

            // Mover y comprobar disparos del enemigo
            Iterator<RectF> iterDisparosEnemigo = enemigo.disparos.iterator();
            while (iterDisparosEnemigo.hasNext()) {
                RectF disparo = iterDisparosEnemigo.next();
                disparo.left -= VELOCIDAD_DISPARO_ENEMIGO_CPR;
                disparo.right -= VELOCIDAD_DISPARO_ENEMIGO_CPR;

                if (disparo.right < 0) {
                    iterDisparosEnemigo.remove();
                } else if (RectF.intersects(hitboxJugador, disparo)) {
                    terminarJuego();
                    return;
                }
            }

            // Verificar si fue impactado por un disparo del jugador
            Iterator<RectF> iterDisparosJugador = disparos.iterator();
            while (iterDisparosJugador.hasNext()) {
                RectF disparo = iterDisparosJugador.next();
                float cx = disparo.left + 10;
                float cy = disparo.top + 7;
                if (distancia(enemigo.x, enemigo.y, cx, cy) < RADIO_ENEMIGO_CPR) {
                    iterDisparosJugador.remove();
                    enemigo.vida--;  // Reducir la vida del enemigo por cada impacto
                    if (enemigo.vida <= 0) {
                        iterEnemigos.remove(); // Eliminar el enemigo
                    }
                    break;
                }
            }
        }

        invalidate(); // Redibuja la pantalla
    }

    public void disparar() {
        disparos.add(new RectF(posX + 75, posY + 35, posX + 95, posY + 50));
        invalidate();
    }

    public void generarEnemigo() {
        if (enemigos.size() >= 5) return;

        int intentos = 0;
        while (intentos++ < 10) {
            int y = random.nextInt(alto / 2 - 200) + 100;
            EnemigoCircular nuevo = new EnemigoCircular(ancho - RADIO_ENEMIGO_CPR, y + RADIO_ENEMIGO_CPR);

            boolean solapado = false;
            for (EnemigoCircular e : enemigos) {
                if (distancia(nuevo.x, nuevo.y, e.x, e.y) < RADIO_ENEMIGO_CPR * 2) {
                    solapado = true;
                    break;
                }
            }

            if (!solapado) {
                // Inicializamos con 3 disparos, pero de manera secuencial.
                nuevo.vida = 3; // El enemigo puede recibir 3 disparos.
                enemigos.add(nuevo);
                dispararEnemigo(nuevo);
                break;
            }
        }
    }

    private void dispararEnemigo(EnemigoCircular enemigo) {
        // Disparo inicial del enemigo
        Handler handler = new Handler();
        Runnable disparoRunnable = new Runnable() {
            int contadorDisparos = 0;
            @Override
            public void run() {
                if (contadorDisparos < MAX_DISPAROS_ENEMIGO) {
                    float offsetY = (contadorDisparos - 1) * 30; // Disparos arriba, centro, abajo
                    RectF disparo = new RectF(
                            enemigo.x - 50, enemigo.y + offsetY - 10,
                            enemigo.x - 30, enemigo.y + offsetY + 10
                    );
                    enemigo.disparos.add(disparo);
                    contadorDisparos++;
                    handler.postDelayed(this, 500); // Disparo cada 500ms (1 segundo entre disparos)
                }
            }
        };

        handler.post(disparoRunnable); // Inicia el disparo secuencial
    }

    public void setDimensiones(int width, int height) {
        this.ancho = width;
        this.alto = height;
        this.posX = 100;
        this.posY = alto / 2;
    }

    public void moverArriba() {
        if (posY > 20) {
            posY -= 20;
            invalidate();
        }
    }

    public void moverAbajo() {
        if (posY < alto - 95) {
            posY += 20;
            invalidate();
        }
    }

    public void terminarJuego() {
        juegoTerminado = true;
        invalidate();

        new Handler().postDelayed(this::reset, 2000); // Reinicia en 2s
    }

    public void reset() {
        enemigos.clear();
        disparos.clear();
        juegoTerminado = false;
        posX = 100;
        posY = alto / 2;
        invalidate();
    }

    public boolean isJuegoTerminado() {
        return juegoTerminado;
    }

    private float distancia(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x2 - x1, y2 - y1);
    }

    private static class EnemigoCircular {
        float x, y;
        ArrayList<RectF> disparos = new ArrayList<>();
        int vida;  // Vida del enemigo (cuántos disparos puede recibir)

        EnemigoCircular(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (juegoTerminado) return true;

        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                posY = (int) y - 37;
                if (posY < 0) posY = 0;
                if (posY > alto - 75) posY = alto - 75;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                disparar();
                break;
        }
        return true;
    }
}
