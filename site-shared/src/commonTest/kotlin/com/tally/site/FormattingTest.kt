package com.tally.site

import kotlin.test.Test
import kotlin.test.assertEquals

class FormattingTest {

    @Test
    fun `When grouping thousands then commas go every three digits`() {
        assertEquals(expected = "0", actual = 0.withThousands())
        assertEquals(expected = "999", actual = 999.withThousands())
        assertEquals(expected = "18,240", actual = 18_240.withThousands())
        assertEquals(expected = "1,234,567", actual = 1_234_567.withThousands())
        assertEquals(expected = "-4,000", actual = (-4_000).withThousands())
    }

    @Test
    fun `When signing a change then positives get a plus and zero stays bare`() {
        assertEquals(expected = "+12", actual = 12.signed())
        assertEquals(expected = "-3", actual = (-3).signed())
        assertEquals(expected = "0", actual = 0.signed())
    }

    @Test
    fun `Given the content when linking store buttons then they fall back to the download section`() {
        val stores = TallySite.content.download.stores

        assertEquals(expected = listOf("Android", "iOS"), actual = stores.map { it.platform })
        assertEquals(expected = SectionId.Download.target, actual = stores.first().target)
    }
}
