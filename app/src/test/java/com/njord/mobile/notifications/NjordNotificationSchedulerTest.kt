package com.njord.mobile.notifications

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class NjordNotificationSchedulerTest {
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test
    fun millisUntilNextActivityRun_usesTodayWhenBeforeNineThirtyPm() {
        val now = ZonedDateTime.of(2026, 6, 19, 21, 0, 0, 0, zone)

        val delay = NjordNotificationScheduler.millisUntilNextActivityRun(now)

        assertEquals(30 * 60 * 1000L, delay)
    }

    @Test
    fun millisUntilNextActivityRun_usesTomorrowWhenAfterNineThirtyPm() {
        val now = ZonedDateTime.of(2026, 6, 19, 22, 0, 0, 0, zone)

        val delay = NjordNotificationScheduler.millisUntilNextActivityRun(now)

        assertEquals(23L * 60L * 60L * 1000L + 30L * 60L * 1000L, delay)
    }
}
