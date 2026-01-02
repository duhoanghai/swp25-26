package com.example.schlaftagebuch_vers_1

//Enthält alle Möglichen Bildschirme:
//Fragen, Menü, Einstellungen etc.

enum class Screen {
    Menu,                   //Hauptmenü
    Questions_Protocol,     //Schlafprotokoll
    Questions_Personal,     //Personenbezogene Fragen
    Summary,                //Zusammenfassung
    Settings,               //Einstellungen
    PrivacyPolicy,          //Datenschutz
    Registration,           //Anmeldung
}