package com.example.spaceinvaders.videogame_basic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.spaceinvaders.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Juego extends View {
    private int ancho, alto;
    private int posX, posY;
    private Bitmap naveJugador, enemigoBola, fondo, disparoEnemigoImg;
    private ArrayList<RectF> disparos = new ArrayList<>();
    private ArrayList<EnemigoCircular> enemigos = new ArrayList<>();
    private ArrayList<RectF> disparosEnemigos = new ArrayList<>();
    private Random random = new Random();
    private Paint paint = new Paint();
    private boolean juegoTerminado = false;

    private static final int RADIO_ENEMIGO = 200;
    private static final int VELOCIDAD_ENEMIGO = 4; // 🔥 Velocidad de las bolas del mundo
    private static final int VELOCIDAD_DISPARO_ENEMIGO = 6; // 🔥 Ahora el fuego se mueve más rápido

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
        Iterator<RectF> iterDisparos = disparos.iterator();
        while (iterDisparos.hasNext()) {
            RectF disparo = iterDisparos.next();
            disparo.left += 15;
            disparo.right += 15;

            if (disparo.left > ancho) {
                iterDisparos.remove();
            } else {
                canvas.drawRect(disparo, paint);
            }
        }

        // Dibujar y mover enemigos
        Iterator<EnemigoCircular> iterEnemigos = enemigos.iterator();
        while (iterEnemigos.hasNext()) {
            EnemigoCircular enemigo = iterEnemigos.next();
            enemigo.x -= VELOCIDAD_ENEMIGO;

            if (enemigo.x + RADIO_ENEMIGO < 0) {
                iterEnemigos.remove();
                continue;
            }

            canvas.drawBitmap(enemigoBola, enemigo.x - 200, enemigo.y - 200, null);

            float distanciaJugador = distancia(enemigo.x, enemigo.y, posX + 37, posY + 37);
            if (distanciaJugador < RADIO_ENEMIGO + 37) {
                terminarJuego();
                return;
            }

            // Generar disparo enemigo (🔥 Solo 1 vez por enemigo)
            if (!enemigo.haDisparado) {
                disparosEnemigos.add(new RectF(enemigo.x - 50, enemigo.y - 10, enemigo.x - 30, enemigo.y + 10));
                enemigo.haDisparado = true; // ⚠️ Ahora no disparará más veces
            }

            // Verificar colisión con disparos del jugador
            Iterator<RectF> iterDisparosCheck = disparos.iterator();
            while (iterDisparosCheck.hasNext()) {
                RectF disparo = iterDisparosCheck.next();
                float centroDisparoX = disparo.left + 10;
                float centroDisparoY = disparo.top + 7;

                if (distancia(enemigo.x, enemigo.y, centroDisparoX, centroDisparoY) < RADIO_ENEMIGO) {
                    iterDisparosCheck.remove();
                    iterEnemigos.remove();
                    break;
                }
            }
        }

        // Dibujar y mover disparos enemigos (🔥 más lentos)
        Iterator<RectF> iterDisparosEnemigos = disparosEnemigos.iterator();
        while (iterDisparosEnemigos.hasNext()) {
            RectF disparoEnemigo = iterDisparosEnemigos.next();
            disparoEnemigo.left -= VELOCIDAD_DISPARO_ENEMIGO;
            disparoEnemigo.right -= VELOCIDAD_DISPARO_ENEMIGO;

            if (disparoEnemigo.right < 0) {
                iterDisparosEnemigos.remove();
                continue;
            }

            canvas.drawBitmap(disparoEnemigoImg, disparoEnemigo.left, disparoEnemigo.top, null);

            // Si el disparo enemigo toca la Bitcoin, termina el juego
            if (RectF.intersects(hitboxJugador, disparoEnemigo)) {
                terminarJuego();
                return;
            }
        }

        invalidate();
    }


    public void disparar() {
        RectF nuevoDisparo = new RectF(posX + 75, posY + 35, posX + 95, posY + 50);
        disparos.add(nuevoDisparo);
        invalidate();
    }

    public void generarEnemigo() {
        if (enemigos.size() >= 5) return;

        int posEnemigoY;
        boolean solapado;
        int intentos = 0;

        do {
            solapado = false;
            posEnemigoY = random.nextInt(alto / 2 - 200) + 100;

            EnemigoCircular nuevoEnemigo = new EnemigoCircular(ancho - RADIO_ENEMIGO, posEnemigoY + RADIO_ENEMIGO);

            for (EnemigoCircular enemigoExistente : enemigos) {
                if (distancia(nuevoEnemigo.x, nuevoEnemigo.y, enemigoExistente.x, enemigoExistente.y) < RADIO_ENEMIGO * 2) {
                    solapado = true;
                    break;
                }
            }

            intentos++;

        } while (solapado && intentos < 10);

        if (!solapado) {
            enemigos.add(new EnemigoCircular(ancho - RADIO_ENEMIGO, posEnemigoY + RADIO_ENEMIGO));
        }
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
        if (posY < alto - 75 - 20) {
            posY += 20;
            invalidate();
        }
    }

    public void terminarJuego() {
        juegoTerminado = true;
        invalidate();
    }

    public boolean isJuegoTerminado() {
        return juegoTerminado;
    }
    public void reset() {
        enemigos.clear();
        disparos.clear();
        disparosEnemigos.clear();
        juegoTerminado = false;
        invalidate();
    }

    private float distancia(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private static class EnemigoCircular {
        float x, y;
        boolean haDisparado = false;
        EnemigoCircular(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
