package com.carlosarancibia.playfit.ui.components.design

/**
 * Named alpha levels for `Color.copy(alpha = ...)` calls. Consolidates the ~20 ad-hoc alpha
 * values scattered across the codebase (several were near-duplicates, e.g. 0.3f/0.30f/0.35f or
 * 0.86f/0.88f) into a single scale so re-tuning happens in one place.
 */
object PlayfitOpacities {
    const val zero = 0.0f
    const val faint = 0.05f
    const val subtle = 0.08f
    const val low = 0.1f
    const val soft = 0.12f
    const val light = 0.15f
    const val muted = 0.2f
    const val mild = 0.25f
    const val medium = 0.3f
    const val moderate = 0.4f
    const val half = 0.5f
    const val strong = 0.6f
    const val heavy = 0.7f
    const val prominent = 0.8f
    const val nearOpaque = 0.87f
    const val opaque = 0.9f
}
