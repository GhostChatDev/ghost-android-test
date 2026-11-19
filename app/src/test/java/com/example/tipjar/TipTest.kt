package com.example.tipjar

import org.junit.Assert.assertEquals
import org.junit.Test

class TipTest {
    @Test fun splitsTheTotalIncludingTip() = assertEquals(2875, Tip.perPersonCents(5000, 15, 2, false))
    @Test fun roundsUpToTheNextDollar() = assertEquals(2900, Tip.perPersonCents(5000, 15, 2, true))
    @Test fun formatsCents() = assertEquals("$28.75", Tip.money(2875))
}
