package com.example.schlaftagebuch_vers_1.helper

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import java.lang.ref.WeakReference

/**
    Dient der Auswertung der String Resourcen IDs für die Übersetzung.
    Der eingebaute Standard Service (stringResource und getContext) erzeugt manchmal Fehler,
    da die Texte bereits vor der Laufzeit ausgewertet werden.
    In Knöpfen (und anderen Laufzeit-Statements wie return), hier meist onClick(),
    können die Texte aber erst während der Laufzeit ausgewertet werden.
 */

object TranslationHelper {
    //Wenn Speicher benötigt, Aktivität durch GBC beendet
    //hatte vorher Speicherleck
    private var contextRef: WeakReference<Context>? = null

    // 1. Init
    fun init(context: Context) {
        this.contextRef = WeakReference(context)
    }

    // 2. normale Texte
    operator fun invoke(@StringRes id: Int): String {
        return contextRef?.get()?.getString(id) ?: "Error: Context lost"
    }


    // 3. fertige Toasts
    fun toast(message: String) {
        contextRef?.get()?.let { ctx ->
            Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()
        }
    }

    fun toast(@StringRes id: Int) {
        contextRef?.get()?.let { ctx ->
            Toast.makeText(ctx, ctx.getString(id), Toast.LENGTH_LONG).show()
        }
    }
}