package com.pulseira.nfc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Tela de ajuda: explica o uso e leva às configurações de acessibilidade. */
class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tela = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val explicacao = TextView(this).apply {
            textSize = 18f
            text = "1. Toque no botão abaixo e ative \"Pulseira NFC\" em Acessibilidade " +
                "(apps instalados).\n\n" +
                "2. Abra o formulário e toque no campo onde o texto deve entrar.\n\n" +
                "3. Encoste a pulseira no tablet. O texto é colado no campo.\n\n" +
                "Para RDP: ative o teclado \"Pulseira NFC\" (botão abaixo). Dentro da sessão, " +
                "abra o teclado do cliente RDP, escolha \"Pulseira NFC\" em \"Trocar teclado\", " +
                "clique no campo do Windows e encoste a pulseira."
        }
        status = TextView(this).apply {
            textSize = 18f
            setPadding(0, 32, 0, 32)
        }
        val botao = Button(this).apply {
            text = "Abrir configurações de acessibilidade"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        val botaoTeclado = Button(this).apply {
            text = "Ativar teclado Pulseira NFC (para RDP)"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }

        tela.addView(explicacao)
        tela.addView(status)
        tela.addView(botao)
        tela.addView(botaoTeclado)
        setContentView(tela)
    }

    override fun onResume() {
        super.onResume()
        status.text = if (PasteService.instancia != null) {
            "Serviço: ATIVO ✔"
        } else {
            "Serviço: desativado ✖"
        }
    }
}
