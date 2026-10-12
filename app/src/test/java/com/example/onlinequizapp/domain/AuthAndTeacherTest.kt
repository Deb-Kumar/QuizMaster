package com.example.onlinequizapp.domain

import com.example.onlinequizapp.data.repository.AuthRepositoryImpl
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.data.repository.toQuizResult
import com.example.onlinequizapp.domain.model.Difficulty
import com.example.onlinequizapp.domain.model.FeedbackMode
import com.example.onlinequizapp.domain.model.Option
import com.example.onlinequizapp.domain.model.Question
import com.example.onlinequizapp.domain.model.QuestionResultEntry
import com.example.onlinequizapp.domain.model.QuestionType
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizConfig
import com.example.onlinequizapp.domain.model.QuizResult
import com.example.onlinequizapp.domain.model.QuizSessionState
import com.example.onlinequizapp.domain.model.SessionStatus
import com.example.onlinequizapp.domain.model.TimerMode
import com.example.onlinequizapp.domain.model.UserQuestionAnswer
import com.example.onlinequizapp.domain.model.UserRole
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthAndTeacherTest {

    private val authRepo = AuthRepositoryImpl.instance
    private val quizRepo = QuizRepositoryImpl.instance

    private fun createTestQuiz(id: String = "test_quiz_1", title: String = "Test Quiz"): Quiz {
        val questions = (1..3).map { i ->
            Question(
                id = "q$i",
                type = QuestionType.SINGLE,
                prompt = "Question $i prompt",
                options = listOf(Option("A", "Option A"), Option("B", "Option B")),
                correctAnswers = setOf("A"),
                explanation = "Explanation $i"
            )
        }
        return Quiz(
            id = id,
            title = title,
            description = "Test description",
            category = "Kotlin",
            icon = "KT",
            questions = questions,
            config = QuizConfig(feedbackMode = FeedbackMode.ON_SUBMIT)
        )
    }

    @Before
    fun setup() = runBlocking {
        authRepo.logout()
        quizRepo.resetToDefaultQuizzes()
        quizRepo.createQuiz(createTestQuiz("js_basics", "JavaScript Basics"))
        quizRepo.createQuiz(createTestQuiz("test_quiz_1", "Test Quiz"))
        // Register test accounts for test suite
        authRepo.register("Alex Johnson", "student@quizmaster.com", "student123", UserRole.STUDENT)
        authRepo.register("Prof. Eleanor Vance", "teacher@quizmaster.com", "teacher123", UserRole.TEACHER)
        authRepo.logout()
    }

    @Test
    fun testStudentLoginSuccess() = runBlocking {
        val result = authRepo.login("student@quizmaster.com", "student123", UserRole.STUDENT)
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(UserRole.STUDENT, user?.role)
        assertEquals("Alex Johnson", user?.name)
        assertEquals(user, authRepo.getCurrentUser())
    }

    @Test
    fun testTeacherLoginSuccess() = runBlocking {
        val result = authRepo.login("teacher@quizmaster.com", "teacher123", UserRole.TEACHER)
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(UserRole.TEACHER, user?.role)
        assertEquals("Prof. Eleanor Vance", user?.name)
        assertEquals(user, authRepo.getCurrentUser())
    }

    @Test
    fun testLoginRoleMismatchFails() = runBlocking {
        // Trying to log in with student email while selecting TEACHER role should fail
        val result = authRepo.login("student@quizmaster.com", "student123", UserRole.TEACHER)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Teacher") == true)
    }

    @Test
    fun testLoginInvalidPasswordFails() = runBlocking {
        val result = authRepo.login("student@quizmaster.com", "wrongpassword", UserRole.STUDENT)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("password") == true)
    }

    @Test
    fun testRegisterNewStudentAndTeacher() = runBlocking {
        val studentResult = authRepo.register(
            name = "Sarah Connor",
            email = "sarah@cyberdyne.org",
            password = "securePassword1",
            role = UserRole.STUDENT
        )
        assertTrue(studentResult.isSuccess)
        assertEquals("Sarah Connor", studentResult.getOrNull()?.name)
        assertEquals(UserRole.STUDENT, studentResult.getOrNull()?.role)

        val teacherResult = authRepo.register(
            name = "Prof. John Connor",
            email = "john@resistance.org",
            password = "securePassword2",
            role = UserRole.TEACHER
        )
        assertTrue(teacherResult.isSuccess)
        assertEquals("Prof. John Connor", teacherResult.getOrNull()?.name)
        assertEquals(UserRole.TEACHER, teacherResult.getOrNull()?.role)
    }

    @Test
    fun testPasswordLengthValidationOnRegister() = runBlocking {
        // Password longer than 16 characters must fail
        val tooLongResult = authRepo.register(
            name = "Long Pass User",
            email = "longpass@quizmaster.com",
            password = "a".repeat(17),
            role = UserRole.STUDENT
        )
        assertTrue(tooLongResult.isFailure)
        assertTrue(tooLongResult.exceptionOrNull()?.message?.contains("16") == true)

        // Password shorter than 6 characters must fail
        val tooShortResult = authRepo.register(
            name = "Short Pass User",
            email = "shortpass@quizmaster.com",
            password = "short",
            role = UserRole.STUDENT
        )
        assertTrue(tooShortResult.isFailure)
        assertTrue(tooShortResult.exceptionOrNull()?.message?.contains("6") == true)

        // Password with exactly 16 characters must succeed
        val exact16Result = authRepo.register(
            name = "Exact 16 User",
            email = "exact16@quizmaster.com",
            password = "1234567890123456",
            role = UserRole.STUDENT
        )
        assertTrue(exact16Result.isSuccess)
    }

    @Test
    fun testPasswordLengthValidationOnLogin() = runBlocking {
        // Attempting to log in with a password longer than 16 characters must fail immediately
        val result = authRepo.login("student@quizmaster.com", "a".repeat(17), UserRole.STUDENT)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("16") == true)
    }

    @Test
    fun testGoogleAuthForStudentAndTeacher() = runBlocking {
        val studentGoogle = authRepo.loginWithGoogle(UserRole.STUDENT)
        assertTrue(studentGoogle.isSuccess)
        val studentUser = studentGoogle.getOrNull()
        assertNotNull(studentUser)
        assertEquals(UserRole.STUDENT, studentUser?.role)
        assertEquals("google", studentUser?.authProvider)
        assertEquals(studentUser, authRepo.getCurrentUser())

        authRepo.logout()

        val teacherGoogle = authRepo.loginWithGoogle(UserRole.TEACHER)
        assertTrue(teacherGoogle.isSuccess)
        val teacherUser = teacherGoogle.getOrNull()
        assertNotNull(teacherUser)
        assertEquals(UserRole.TEACHER, teacherUser?.role)
        assertEquals("google", teacherUser?.authProvider)
        assertEquals(teacherUser, authRepo.getCurrentUser())
    }

    @Test
    fun testGithubAuthForStudentAndTeacher() = runBlocking {
        val studentGithub = authRepo.loginWithGithub(UserRole.STUDENT)
        assertTrue(studentGithub.isSuccess)
        val studentUser = studentGithub.getOrNull()
        assertNotNull(studentUser)
        assertEquals(UserRole.STUDENT, studentUser?.role)
        assertEquals("github", studentUser?.authProvider)

        authRepo.logout()

        val teacherGithub = authRepo.loginWithGithub(UserRole.TEACHER)
        assertTrue(teacherGithub.isSuccess)
        val teacherUser = teacherGithub.getOrNull()
        assertNotNull(teacherUser)
        assertEquals(UserRole.TEACHER, teacherUser?.role)
        assertEquals("github", teacherUser?.authProvider)
    }

    @Test
    fun testSocialAuthWithExplicitAccountDetails() = runBlocking {
        // When real Google sign-in provides name and email
        val googleResult = authRepo.loginWithGoogle(
            role = UserRole.STUDENT,
            name = "Aria Stark",
            email = "aria.stark@winterfell.edu",
            idToken = null
        )
        assertTrue(googleResult.isSuccess)
        val user = googleResult.getOrNull()
        assertNotNull(user)
        assertEquals("Aria Stark", user?.name)
        assertEquals("aria.stark@winterfell.edu", user?.email)
        assertEquals(UserRole.STUDENT, user?.role)
        assertEquals("google", user?.authProvider)

        // When real GitHub sign-in provides name and email
        val githubResult = authRepo.loginWithGithub(
            role = UserRole.TEACHER,
            name = "Jon Snow",
            email = "jon.snow@thewall.org"
        )
        assertTrue(githubResult.isSuccess)
        val githubUser = githubResult.getOrNull()
        assertNotNull(githubUser)
        assertEquals("Jon Snow", githubUser?.name)
        assertEquals("jon.snow@thewall.org", githubUser?.email)
        assertEquals(UserRole.TEACHER, githubUser?.role)
        assertEquals("github", githubUser?.authProvider)
    }

    @Test
    fun testLogoutClearsCurrentUser() = runBlocking {
        authRepo.login("student@quizmaster.com", "student123", UserRole.STUDENT)
        assertNotNull(authRepo.getCurrentUser())
        authRepo.logout()
        assertNull(authRepo.getCurrentUser())
    }

    @Test
    fun testTeacherCreateQuiz() = runBlocking {
        val initialCount = quizRepo.getAvailableQuizzes().size

        val customQuiz = Quiz(
            id = "custom_kotlin_coroutines",
            title = "Kotlin Coroutines Mastery",
            description = "Explore dispatchers, scopes, and flow collectors.",
            category = "Kotlin",
            icon = "KT",
            config = QuizConfig(
                feedbackMode = FeedbackMode.ON_SUBMIT,
                timerMode = TimerMode.COUNTDOWN,
                timeLimitSeconds = 300,
                allowHints = true
            ),
            questions = listOf(
                Question(
                    id = "cq_1",
                    type = QuestionType.SINGLE,
                    prompt = "Which dispatcher is optimized for disk and network IO operations?",
                    options = listOf(
                        Option("A", "Dispatchers.Main"),
                        Option("B", "Dispatchers.IO"),
                        Option("C", "Dispatchers.Default"),
                        Option("D", "Dispatchers.Unconfined")
                    ),
                    correctAnswers = setOf("B"),
                    explanation = "Dispatchers.IO is designed for blocking IO tasks.",
                    difficulty = Difficulty.MEDIUM
                )
            )
        )

        val created = quizRepo.createQuiz(customQuiz)
        assertTrue(created)

        val quizzesAfter = quizRepo.getAvailableQuizzes()
        assertEquals(initialCount + 1, quizzesAfter.size)
        assertEquals("custom_kotlin_coroutines", quizzesAfter.first().id)
        assertEquals("Kotlin Coroutines Mastery", quizzesAfter.first().title)
    }

    @Test
    fun testTeacherDeleteQuiz() = runBlocking {
        val initialQuizzes = quizRepo.getAvailableQuizzes()
        val targetId = initialQuizzes.first().id

        val deleted = quizRepo.deleteQuiz(targetId)
        assertTrue(deleted)

        val remainingQuizzes = quizRepo.getAvailableQuizzes()
        assertEquals(initialQuizzes.size - 1, remainingQuizzes.size)
        assertNull(quizRepo.getQuizById(targetId))
    }

    @Test
    fun testTeacherResetToDefaultQuizzes() = runBlocking {
        assertTrue(quizRepo.getAvailableQuizzes().any { it.id == "js_basics" })
        quizRepo.deleteQuiz("js_basics")
        val afterDelete = quizRepo.getAvailableQuizzes().any { it.id == "js_basics" }
        assertFalse(afterDelete)

        quizRepo.resetToDefaultQuizzes()
        // With sample data removed and Firestore unavailable in unit tests, reset clears local cache
        assertTrue(quizRepo.getAvailableQuizzes().isEmpty())
    }

    @Test
    fun testSaveAndRetrieveQuizSubmissions() = runBlocking {
        val sampleQuiz = quizRepo.getAvailableQuizzes().first()
        val mockSession = QuizSessionState(
            quiz = sampleQuiz,
            currentIndex = 0,
            answers = mapOf(
                sampleQuiz.questions.first().id to UserQuestionAnswer(
                    questionId = sampleQuiz.questions.first().id,
                    selectedOptionIds = setOf("A"),
                    isAnswered = true,
                    isCorrect = true
                )
            ),
            remainingTimeSeconds = 120,
            elapsedTimeSeconds = 180,
            status = SessionStatus.COMPLETED
        )

        quizRepo.saveCompletedQuizResult(mockSession)
        val allSubmissions = quizRepo.getAllCompletedResults()
        assertTrue(allSubmissions.any { it.quiz.id == sampleQuiz.id })
    }

    @Test
    fun testUpdateTeacherProfileSuccess() = runBlocking {
        val regResult = authRepo.register("Prof. Turing", "turing@cambridge.edu", "securePass1", UserRole.TEACHER)
        assertTrue(regResult.isSuccess)
        val teacherUser = regResult.getOrThrow()

        val profile = com.example.onlinequizapp.domain.model.TeacherProfile(
            organization = "University of Cambridge",
            department = "Computer Science",
            designation = "Professor",
            referralSource = "YouTube"
        )

        val updateResult = authRepo.updateTeacherProfile(teacherUser.id, profile)
        assertTrue(updateResult.isSuccess)
        val updated = updateResult.getOrThrow()
        assertEquals("University of Cambridge", updated.teacherProfile?.organization)
        assertEquals("Computer Science", updated.teacherProfile?.department)
        assertEquals("Professor", updated.teacherProfile?.designation)
        assertEquals("YouTube", updated.teacherProfile?.referralSource)
        assertEquals(updated, authRepo.getCurrentUser())
    }

    @Test
    fun testUpdateStudentProfileSuccess() = runBlocking {
        val regResult = authRepo.register("John Doe", "john.doe@college.edu", "studentPass123", UserRole.STUDENT)
        assertTrue(regResult.isSuccess)
        val studentUser = regResult.getOrThrow()

        val profile = com.example.onlinequizapp.domain.model.StudentProfile(
            university = "Delhi University",
            department = "Computer Science",
            course = "BCA",
            referralSource = "Instagram"
        )

        val updateResult = authRepo.updateStudentProfile(studentUser.id, profile)
        assertTrue(updateResult.isSuccess)
        val updated = updateResult.getOrThrow()
        assertEquals("Delhi University", updated.studentProfile?.university)
        assertEquals("Computer Science", updated.studentProfile?.department)
        assertEquals("BCA", updated.studentProfile?.course)
        assertEquals("Instagram", updated.studentProfile?.referralSource)
        assertEquals(updated, authRepo.getCurrentUser())
    }

    @Test
    fun testUpdateUserProfileWithContactNumberSuccess() = runBlocking {
        val regResult = authRepo.register("Rohan Sen", "rohan.sen@college.edu", "studentPass123", UserRole.STUDENT)
        assertTrue(regResult.isSuccess)
        val studentUser = regResult.getOrThrow()

        val updateResult = authRepo.updateUserProfile(
            userId = studentUser.id,
            name = "Rohan Sen",
            university = "Brainware University",
            department = "Computational Sciences",
            course = "MCA",
            contactNumber = "+91 9876543210"
        )
        assertTrue(updateResult.isSuccess)
        val updated = updateResult.getOrThrow()
        assertEquals("+91 9876543210", updated.contactNumber)
        assertEquals("Brainware University", updated.displayUniversity)
        assertEquals("Computational Sciences", updated.displayDepartment)
        assertEquals("MCA", updated.displayCourse)
        assertEquals("+91 9876543210", authRepo.getCurrentUser()?.contactNumber)
    }

    @Test
    fun testSocialUserPasswordSetupAndSubsequentChange() = runBlocking {
        // 1. Social user logs in via Google
        val googleLogin = authRepo.loginWithGoogle(
            role = UserRole.STUDENT,
            name = "Priya Sharma",
            email = "priya.sharma.test@gmail.com"
        )
        assertTrue(googleLogin.isSuccess)
        val user = googleLogin.getOrThrow()
        assertTrue(user.isSocialAccount)
        assertFalse(user.hasPasswordSet)
        assertTrue(user.needsPasswordSetup) // Red badge will show on profile icon and settings

        // 2. User sets their initial password (no current password needed)
        val setupResult = authRepo.setOrChangePassword(user.id, null, "pass1234")
        assertTrue(setupResult.isSuccess)
        val userWithPassword = setupResult.getOrThrow()
        assertTrue(userWithPassword.hasPasswordSet)
        assertFalse(userWithPassword.needsPasswordSetup) // Red badge vanishes

        // 3. User tries to change password without current password - should fail
        val noCurrentResult = authRepo.setOrChangePassword(userWithPassword.id, null, "newpass123")
        assertTrue(noCurrentResult.isFailure)

        // 4. User tries to change password with wrong current password - should fail
        val wrongCurrentResult = authRepo.setOrChangePassword(userWithPassword.id, "wrongpass", "newpass123")
        assertTrue(wrongCurrentResult.isFailure)

        // 5. User changes password with correct current password - should succeed
        val changeSuccess = authRepo.setOrChangePassword(userWithPassword.id, "pass1234", "newpass123")
        assertTrue(changeSuccess.isSuccess)
        val finalUser = changeSuccess.getOrThrow()
        assertTrue(finalUser.hasPasswordSet)
        assertFalse(finalUser.needsPasswordSetup)
    }

    @Test
    fun testDeleteAccount() = runBlocking {
        val reg = authRepo.register("Delete Me", "deleteme@quizmaster.com", "pass123", UserRole.STUDENT)
        assertTrue(reg.isSuccess)
        val user = reg.getOrThrow()
        assertNotNull(authRepo.getCurrentUser())

        val deleteResult = authRepo.deleteAccount(user.id)
        assertTrue(deleteResult.isSuccess)

        assertNull(authRepo.getCurrentUser())

        val loginAttempt = authRepo.login("deleteme@quizmaster.com", "pass123", UserRole.STUDENT)
        assertTrue(loginAttempt.isFailure)
    }

    // ── Quiz Result Persistence Tests ──

    @Test
    fun testQuizSessionStateToQuizResultConversion() = runBlocking {
        val sampleQuiz = quizRepo.getAvailableQuizzes().first()
        val question = sampleQuiz.questions.first()
        val answer = UserQuestionAnswer(
            questionId = question.id,
            selectedOptionIds = setOf(question.options.first().id),
            isAnswered = true,
            isCorrect = true,
            timeSpentSeconds = 12
        )

        val sessionState = QuizSessionState(
            quiz = sampleQuiz,
            currentIndex = 0,
            answers = mapOf(question.id to answer),
            status = SessionStatus.COMPLETED,
            elapsedTimeSeconds = 45,
            remainingTimeSeconds = 15
        )

        val result = sessionState.toQuizResult(userId = "user_test_123", userName = "Test Student")

        assertEquals("user_test_123", result.userId)
        assertEquals("Test Student", result.userName)
        assertEquals(sampleQuiz.id, result.quizId)
        assertEquals(sampleQuiz.title, result.quizTitle)
        assertEquals(sampleQuiz.category, result.quizCategory)
        assertEquals(sampleQuiz.questions.size, result.totalQuestions)
        assertEquals(1, result.correctCount)
        assertEquals(45, result.elapsedTimeSeconds)
        assertEquals(1, result.questionResults.size)
        assertEquals(question.id, result.questionResults.first().questionId)
        assertTrue(result.questionResults.first().isCorrect)
    }

    @Test
    fun testQuizResultModelFieldsValidation() {
        val entry = QuestionResultEntry(
            questionId = "q1",
            selectedOptionIds = listOf("opt_a"),
            isAnswered = true,
            isCorrect = true,
            timeSpentSeconds = 10
        )
        val result = QuizResult(
            resultId = "res_001",
            userId = "usr_001",
            userName = "Alice",
            quizId = "qz_001",
            quizTitle = "Kotlin Basics",
            quizCategory = "Programming",
            totalQuestions = 10,
            correctCount = 8,
            incorrectCount = 1,
            unansweredCount = 1,
            scorePercentage = 80,
            elapsedTimeSeconds = 120,
            submittedAtMillis = 1700000000000L,
            status = "COMPLETED",
            questionResults = listOf(entry)
        )

        assertEquals("res_001", result.resultId)
        assertEquals(80, result.scorePercentage)
        assertEquals(1, result.questionResults.size)
        assertTrue(result.isPassed)
    }

    @Test
    fun testSaveAndRetrieveQuizResultInMemory() = runBlocking {
        val sampleQuiz = quizRepo.getAvailableQuizzes().first()
        val sessionState = QuizSessionState(
            quiz = sampleQuiz,
            currentIndex = 0,
            answers = emptyMap(),
            status = SessionStatus.COMPLETED,
            elapsedTimeSeconds = 60,
            remainingTimeSeconds = 0
        )

        quizRepo.saveCompletedQuizResult(sessionState)

        val latest = quizRepo.getLatestQuizSession(sampleQuiz.id)
        assertNotNull(latest)
        assertEquals(sampleQuiz.id, latest?.quiz?.id)

        val allResults = quizRepo.getAllCompletedResults()
        assertTrue(allResults.any { it.quiz.id == sampleQuiz.id })
    }
}
