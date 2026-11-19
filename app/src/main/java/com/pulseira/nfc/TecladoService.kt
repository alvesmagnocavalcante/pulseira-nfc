package com.pulseira.nfc

import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Teclado mínimo: não tem teclas, só digita o que vem da pulseira.
 * Serve para apps que não expõem campos de texto ao Android (ex.: sessão RDP),
 * onde a acessibilidade não consegue colar.
 */
class TecladoService : InputMethodService() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        instancia = this
    }

    override fun onDestroy() {
        instancia = null
        super.onDestroy()
    }

    override fun onCreateInputView(): View {
        val dp = resources.displayMetrics.density
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((16 * dp).toInt(), (8 * dp).toInt(), (16 * dp).toInt(), (8 * dp).toInt())
            addView(
                TextView(context).apply {
                    text = "Encoste a pulseira para digitar"
                    textSize = 16f
                },
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            )
            addView(Button(context).apply {
                text = "Trocar teclado"
                setOnClickListener {
                    getSystemService(InputMethodManager::class.java).showInputMethodPicker()
                }
            })
        }
    }

    fun digitar(texto: String) {
        tentar(texto, 0)
    }

    // A tela transparente leva um instante para fechar; tenta algumas vezes.
    private fun tentar(texto: String, tentativa: Int) {
        handler.postDelayed({
            val ic = currentInputConnection
            if (ic != null) {
                ic.commitText(texto, 1)
            } else if (tentativa < 10) {
                tentar(texto, tentativa + 1)
            }
        }, 250)
    }

    companion object {
        var instancia: TecladoService? = null
    }
}
