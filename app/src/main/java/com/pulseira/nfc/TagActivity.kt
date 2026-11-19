package com.pulseira.nfc

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.Toast
import java.nio.charset.Charset

/**
 * Tela transparente que o Android abre quando a pulseira encosta no tablet.
 * Lê o texto, copia, avisa o serviço de acessibilidade e fecha na hora,
 * devolvendo o foco ao formulário.
 */
class TagActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tratar(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        tratar(intent)
    }

    private fun tratar(i: Intent) {
        // Prefere o texto gravado; se não houver, usa o código (UID) da pulseira
        val texto = lerTexto(i) ?: lerUid(i)
        if (texto.isNullOrEmpty()) {
            Toast.makeText(this, "Não consegui ler a pulseira", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val cm = getSystemService(ClipboardManager::class.java)
        cm.setPrimaryClip(ClipData.newPlainText("pulseira", texto))

        val servico = PasteService.instancia
        if (servico == null) {
            Toast.makeText(
                this,
                "Texto copiado. Ative o serviço de acessibilidade para colar sozinho.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            servico.colar(texto)
        }
        finish()
    }

    // UID em hexadecimal maiúsculo, sem separadores (ex.: 04A1B2C3)
    @Suppress("DEPRECATION")
    private fun lerUid(i: Intent): String? {
        val id = i.getByteArrayExtra(NfcAdapter.EXTRA_ID)
            ?: i.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.id
            ?: return null
        return id.joinToString("") { "%02X".format(it) }
    }

    @Suppress("DEPRECATION")
    private fun lerTexto(i: Intent): String? {
        val msgs = i.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES) ?: return null
        val sb = StringBuilder()
        for (m in msgs) {
            for (r in (m as NdefMessage).records) {
                if (r.tnf == NdefRecord.TNF_WELL_KNOWN &&
                    r.type.contentEquals(NdefRecord.RTD_TEXT)
                ) {
                    sb.append(decodificarTexto(r.payload))
                }
            }
        }
        return sb.toString().ifEmpty { null }
    }

    // Payload de registro de texto: [status][idioma...][texto...]
    private fun decodificarTexto(p: ByteArray): String {
        val status = p[0].toInt()
        val charset = if (status and 0x80 == 0) Charsets.UTF_8 else Charset.forName("UTF-16")
        val tamIdioma = status and 0x3F
        return String(p, 1 + tamIdioma, p.size - 1 - tamIdioma, charset)
    }
}
