package com.andrerinas.openheadunit.main

import com.andrerinas.openheadunit.main.AutoStartOfferPolicy.Action
import com.andrerinas.openheadunit.main.AutoStartOfferPolicy.Trigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoStartOfferPolicyTest {

    private val phone1 = "AA:BB:CC:DD:EE:FF"
    private val phone2 = "11:22:33:44:55:66"

    @Test
    fun `a new connected phone that has never been asked about is offered`() {
        assertEquals(
            Action.ASK,
            AutoStartOfferPolicy.decide(
                connectedMac = phone1,
                answeredMacs = emptySet(),
                configuredMacs = emptySet()
            )
        )
    }

    @Test
    fun `a phone already answered for is not asked again`() {
        assertEquals(
            Action.NOTHING,
            AutoStartOfferPolicy.decide(
                connectedMac = phone1,
                answeredMacs = setOf(phone1),
                configuredMacs = emptySet()
            )
        )
    }

    @Test
    fun `a phone already configured for auto-start is not asked again`() {
        assertEquals(
            Action.NOTHING,
            AutoStartOfferPolicy.decide(
                connectedMac = phone1,
                answeredMacs = emptySet(),
                configuredMacs = setOf(phone1)
            )
        )
    }

    @Test
    fun `a second new phone is offered even if a first phone is already configured`() {
        assertEquals(
            Action.ASK,
            AutoStartOfferPolicy.decide(
                connectedMac = phone2,
                answeredMacs = setOf(phone1),
                configuredMacs = setOf(phone1)
            )
        )
    }

    @Test
    fun `a phone that cannot be named is never offered`() {
        assertEquals(
            Action.NOTHING,
            AutoStartOfferPolicy.decide(
                connectedMac = "",
                answeredMacs = emptySet(),
                configuredMacs = emptySet()
            )
        )
    }

    @Test
    fun `the question is asked over the picture, never on the home screen`() {
        assertTrue(AutoStartOfferPolicy.actsNow(Action.ASK, Trigger.PROJECTION_START))
        assertFalse(AutoStartOfferPolicy.actsNow(Action.ASK, Trigger.HOME_SCREEN))
    }

    @Test
    fun `nothing acts at either moment`() {
        assertFalse(AutoStartOfferPolicy.actsNow(Action.NOTHING, Trigger.HOME_SCREEN))
        assertFalse(AutoStartOfferPolicy.actsNow(Action.NOTHING, Trigger.PROJECTION_START))
    }

    @Test
    fun `every action has exactly one moment it belongs to, or none`() {
        for (action in Action.values()) {
            val moments = Trigger.values().count { AutoStartOfferPolicy.actsNow(action, it) }
            assertTrue("$action acts at $moments moments", moments <= 1)
        }
    }

    @Test
    fun `the offer answers itself after twenty seconds`() {
        assertEquals(20_000L, AutoStartOfferPolicy.OFFER_TIMEOUT_MS)
    }
}
