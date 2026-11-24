package com.example.cliniclearai.ui.common

/**
 * Modèle immuable représentant un message d'erreur pour la couche UI.
 *
 * Points clés du cours pour le jury :
 * - Gestion des erreurs pilotée par l'état (State-driven error handling) :
 *   L'erreur ne transite pas par un canal ou un événement volatil non garanti,
 *   mais fait partie intégrante de l'état UI immuable (liste `errorMessages`).
 * - Identifiant unique [id] : Permet au ViewModel et au Composable de dépiler
 *   précisément l'erreur traitée (action "Réessayer" ou fermeture du Snackbar)
 *   sans risquer de supprimer une nouvelle erreur survenue entre-temps.
 *
 * @property id Horodatage ou identifiant unique de l'erreur.
 * @property message Message explicatif destiné à être affiché dans le Snackbar.
 */
data class UiErrorMessage(
    val id: Long = System.nanoTime(),
    val message: String
)
