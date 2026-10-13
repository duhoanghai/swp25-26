package com.example.schlaftagebuch_vers_1.uiTools


//Enthält alle Möglichen Bildschirme:

enum class Screen {
    PrivacyPolicy,          //Datenschutzerklärung
    FirstLogin,             //Einstieg in die App / Erster Login
    CredentialsInfo,        //Generierte Username + Hinweis das zu speichern
    Menu,                   //Hauptmenü
    Questions_Protocol,     //Schlafprotokoll
    Questions_Personal,     //Personenbezogene Fragen
    Summary,                //Zusammenfassung
    Settings,               //Einstellungen
    SleepControl,           //Uhr Steuerung
    SyncEvaluation,         //Auswahl der Uhrdaten
    SyncEvaluationMenu,     //Auswahl der Uhrdaten aber mit anderer Navigation
    AnalysisResult,         //Visualisierung Uhrdaten
    AnalysisResultMenu,     //Visualisierung Uhrdaten aber mit anderer Navigation
    AuthChoice,             //First-Login oder Login
    Login,                  //Anmelden
    Language                //Erste Sprachauswahl beim ersten öffnen
}