package com.carlosarancibia.playfit.model

sealed interface ProductOnboardingValidation {
    data object Valid : ProductOnboardingValidation
    data class Invalid(val message: String) : ProductOnboardingValidation
}

object ProductOnboardingRules {
    const val REQUIRED_LIKED_GAMES = 3
    const val REQUIRED_DISLIKED_GAMES = 1

    fun validate(draft: ProductOnboardingDraft): ProductOnboardingValidation {
        val platforms = draft.platforms.map { it.platformId }
        val liked = draft.likedGameIds
        val disliked = draft.dislikedGameIds

        return when {
            platforms.isEmpty() -> ProductOnboardingValidation.Invalid("Select at least one platform.")
            platforms.distinct().size != platforms.size ->
                ProductOnboardingValidation.Invalid("Platforms must be unique.")
            liked.size != REQUIRED_LIKED_GAMES ->
                ProductOnboardingValidation.Invalid("Select exactly 3 games you loved.")
            liked.distinct().size != liked.size ->
                ProductOnboardingValidation.Invalid("Loved games must be unique.")
            disliked.size != REQUIRED_DISLIKED_GAMES ->
                ProductOnboardingValidation.Invalid("Select exactly 1 game that was not for you.")
            disliked.distinct().size != disliked.size ->
                ProductOnboardingValidation.Invalid("The missed game must be unique.")
            liked.any { it in disliked } ->
                ProductOnboardingValidation.Invalid("A game cannot be both loved and avoided.")
            else -> ProductOnboardingValidation.Valid
        }
    }

    fun canComplete(draft: ProductOnboardingDraft): Boolean =
        validate(draft) is ProductOnboardingValidation.Valid
}
