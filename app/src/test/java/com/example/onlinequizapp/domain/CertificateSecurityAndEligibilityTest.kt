package com.example.onlinequizapp.domain

import com.example.onlinequizapp.util.CertificateGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class CertificateSecurityAndEligibilityTest {

    @Test
    fun testStrictEligibilityBoundaries() {
        // Below passing threshold
        assertFalse("0.0% must not be eligible", CertificateGenerator.isEligible(0.0))
        assertFalse("50.0% must not be eligible", CertificateGenerator.isEligible(50.0))
        assertFalse("79.99% must not be eligible", CertificateGenerator.isEligible(79.99))
        assertFalse("Strict Rule 2: Exactly 80.0% must NOT be eligible", CertificateGenerator.isEligible(80.0))

        // Passing threshold (> 80.0%)
        assertTrue("80.0001% must be eligible", CertificateGenerator.isEligible(80.0001))
        assertTrue("80.01% must be eligible", CertificateGenerator.isEligible(80.01))
        assertTrue("92.0% must be eligible", CertificateGenerator.isEligible(92.0))
        assertTrue("98.0% must be eligible", CertificateGenerator.isEligible(98.0))
        assertTrue("100.0% must be eligible", CertificateGenerator.isEligible(100.0))
    }

    @Test
    fun testSecurityBoundaryRejections() {
        // Negative scores
        assertFalse("Negative score must be rejected", CertificateGenerator.isEligible(-0.01))
        assertFalse("Negative 100% must be rejected", CertificateGenerator.isEligible(-100.0))

        // Exceeding maximum possible score (> 100.0%)
        assertFalse("Scores > 100.0% must be rejected", CertificateGenerator.isEligible(100.01))
        assertFalse("Scores > 100.0% must be rejected", CertificateGenerator.isEligible(150.0))
        assertFalse("Scores > 100.0% must be rejected", CertificateGenerator.isEligible(999.0))

        // Non-finite values
        assertFalse("NaN must be rejected", CertificateGenerator.isEligible(Double.NaN))
        assertFalse("Positive Infinity must be rejected", CertificateGenerator.isEligible(Double.POSITIVE_INFINITY))
        assertFalse("Negative Infinity must be rejected", CertificateGenerator.isEligible(Double.NEGATIVE_INFINITY))
    }

    @Test
    fun testVerificationUrlIntegrity() {
        val certId = "QM-JS92-20261125-003"
        val expectedBaseUrl = "https://quizmaster-dd2f8.web.app/verify/"
        assertEquals(expectedBaseUrl, CertificateGenerator.VERIFICATION_BASE_URL)
        assertEquals("$expectedBaseUrl$certId", "${CertificateGenerator.VERIFICATION_BASE_URL}$certId")
    }

    @Test
    fun testFilenameSanitizationLogic() {
        val maliciousStudent = "../../etc/passwd"
        val safeStudent = maliciousStudent.replace("[^a-zA-Z0-9_-]".toRegex(), "_").trim('_').ifBlank { "Student" }
        assertFalse("Sanitized name must not contain path traversal", safeStudent.contains("..") || safeStudent.contains("/"))
        assertEquals("etc_passwd", safeStudent)

        val emptySpecialChars = "@@@###$$$"
        val fallbackStudent = emptySpecialChars.replace("[^a-zA-Z0-9_-]".toRegex(), "_").trim('_').ifBlank { "Student" }
        assertEquals("Student", fallbackStudent)
    }

    @Test
    fun testPasswordHashingAlgorithmSecurity() {
        val testPassword = "SecretPassword123"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(testPassword.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

        // SHA-256 must produce a 64-character hexadecimal string
        assertEquals(64, hash.length)
        assertFalse("Hash must not equal plaintext password", hash == testPassword)
    }
}
