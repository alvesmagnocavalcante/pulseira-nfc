package com.pulseira.nfc

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

/**
 * Cola o texto no campo que estiver em foco na janela ativa.
 * Não observa eventos da tela: só age quando a TagActivity pede.
 */
class PasteService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        instancia = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instancia = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun colar(texto: String) {
        tentar(texto, 0)
    }

    // A janela transparente leva um instante para fechar; tenta algumas vezes.
    private fun tentar(texto: String, tentativa: Int) {
        handler.postDelayed({
            val campo = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            if (campo != null && campo.packageName != packageName) {
                if (!campo.performAction(AccessibilityNodeInfo.ACTION_PASTE)) {
                    val args = Bundle().apply {
                        putCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                            (campo.text?.toString() ?: "") + texto
                        )
                    }
                    campo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                }
            } else if (tentativa < 8) {
                tentar(texto, tentativa + 1)
            } else {
                Toast.makeText(
                    this,
                    "Nenhum campo selecionado. Texto copiado, é só colar.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, 200)
    }

    companion object {
        var instancia: PasteService? = null
    }
}
