package com.example.onlinequizapp.domain

import com.example.onlinequizapp.util.CertificateGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CertificateGeneratorTest {

    @Test
    fun testRule2EligibilityThreshold() {
        // Rule 2: Generate and issue a certificate only when quiz_score_percentage > 80
        assertFalse("80% must not be eligible", CertificateGenerator.isEligible(80.0))
        assertFalse("79.9% must not be eligible", CertificateGenerator.isEligible(79.9))
        assertFalse("0% must not be eligible", CertificateGenerator.isEligible(0.0))

        assertTrue("80.01% must be eligible", CertificateGenerator.isEligible(80.01))
        assertTrue("92% must be eligible", CertificateGenerator.isEligible(92.0))
        assertTrue("100% must be eligible", CertificateGenerator.isEligible(100.0))
    }

    @Test
    fun testVerificationUrlFormat() {
        val certId = "QM-JS92-20261007-001"
        val expectedUrl = "https://quizmaster-dd2f8.web.app/verify/QM-JS92-20261007-001"
        assertEquals(expectedUrl, "${CertificateGenerator.VERIFICATION_BASE_URL}$certId")
    }
}
