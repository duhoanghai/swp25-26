package com.example.schlaftagebuch_vers_1.api

/**
 * In-Memory-Session.
 *
 * Zweck:
 * - JWT wird hier zur Laufzeit gespeichert, damit ApiClient es automatisch
 *   als Bearer-Token in HTTP-Requests einfügen kann.
 *
 * Hinweis:
 * - Nach App-Neustart ist dieser Wert weg -> SessionStorage speichert persistent.
 */

object Session {
    var jwt: String? = null

}