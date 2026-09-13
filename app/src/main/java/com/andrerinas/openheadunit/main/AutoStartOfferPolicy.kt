package com.andrerinas.openheadunit.main

/**
 * Whether to offer Bluetooth auto-start for a phone connecting to this unit.
 *
 * Prompts for any newly connected phone that has not been configured or previously declined.
 * When the user accepts ("Yes"), the device is added to the set of configured auto-start devices.
 */
object AutoStartOfferPolicy {

    enum class Action {
        /** Offer auto-start for the connected phone. */
        ASK,

        /** Legacy reset action (no-op). */
        RESET,

        NOTHING,
    }

    /** Where the question is being asked from. */
    enum class Trigger {
        /** The home screen, on a resume. */
        HOME_SCREEN,

        /** The first frame of a session, over the picture. */
        PROJECTION_START,
    }

    /** The offer answers itself after this, so it never holds a driver's screen for longer. */
    const val OFFER_TIMEOUT_MS = 20_000L

    /**
     * @param connectedMac the phone this session was with, empty when it cannot be named.
     * @param answeredMacs phones already asked about, so a "no" is not asked again.
     * @param configuredMacs phones already configured for auto-start.
     */
    fun decide(
        connectedMac: String,
        answeredMacs: Set<String>,
        configuredMacs: Set<String>,
    ): Action = when {
        connectedMac.isEmpty() -> Action.NOTHING
        configuredMacs.any { it.equals(connectedMac, ignoreCase = true) } -> Action.NOTHING
        answeredMacs.any { it.equals(connectedMac, ignoreCase = true) } -> Action.NOTHING
        else -> Action.ASK
    }

    /**
     * Backward-compatible overload for legacy call sites.
     */
    fun decide(
        phonesPaired: Int,
        connectedMac: String,
        answeredMacs: Set<String>,
        autoStartConfigured: Boolean,
    ): Action = decide(
        connectedMac = connectedMac,
        answeredMacs = answeredMacs,
        configuredMacs = if (autoStartConfigured) setOf(connectedMac) else emptySet(),
    )

    /**
     * Whether [trigger] is the moment [action] belongs to.
     */
    fun actsNow(action: Action, trigger: Trigger): Boolean = when (action) {
        Action.NOTHING -> false
        Action.RESET -> false
        Action.ASK -> trigger == Trigger.PROJECTION_START
    }
}
