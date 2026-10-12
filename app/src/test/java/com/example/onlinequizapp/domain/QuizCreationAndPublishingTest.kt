package com.example.onlinequizapp.domain

import com.example.onlinequizapp.data.ai.AIQuizValidator
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.domain.model.Difficulty
import com.example.onlinequizapp.domain.model.Option
import com.example.onlinequizapp.domain.model.Question
import com.example.onlinequizapp.domain.model.QuestionType
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizCreationMethod
import com.example.onlinequizapp.domain.model.QuizStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizCreationAndPublishingTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repo = QuizRepositoryImpl.instance

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAiQuizValidatorParsesAndSanitizesJsonWithMarkdown() {
        val rawAiJson = """
            ```json
            [
              {
                "question": "What is a Stack in Data Structures?",
                "options": ["FIFO", "LIFO", "Random Access", "Tree"],
                "correctAnswer": "B",
                "explanation": "A stack is a Last-In-First-Out data structure.",
                "hint": "Think of a pile of plates."
              },
              {
                "question": "Which operation inserts an item on a Stack?",
                "options": ["pop", "push", "peek", "enqueue"],
                "correctAnswer": "B",
                "explanation": "Push adds an element to the top of the stack.",
                "hint": "Opposite of pop."
              }
            ]
            ```
        """.trimIndent()

        val parsed = AIQuizValidator.parseAndValidate(rawAiJson, QuestionType.SINGLE)
        assertEquals(2, parsed.size)
        assertEquals("What is a Stack in Data Structures?", parsed[0].prompt)
        assertEquals(listOf("FIFO", "LIFO", "Random Access", "Tree"), parsed[0].options)
        assertEquals(setOf("B"), parsed[0].correctAnswers)
        assertEquals("A stack is a Last-In-First-Out data structure.", parsed[0].explanation)
        assertEquals("Think of a pile of plates.", parsed[0].hint)
    }

    @Test
    fun testAiQuizValidatorRejectsMalformedQuestions() {
        val badJson = """
            [
              {
                "question": "Empty options question",
                "options": [],
                "correctAnswer": "A"
              },
              {
                "question": "Only 1 option question",
                "options": ["Single Option"],
                "correctAnswer": "A"
              },
              {
                "question": "",
                "options": ["A", "B"],
                "correctAnswer": "A"
              }
            ]
        """.trimIndent()

        val parsed = AIQuizValidator.parseAndValidate(badJson, QuestionType.SINGLE)
        assertEquals(0, parsed.size)
    }

    @Test
    fun testGeneratedQuizCodeIsExactly6CharactersAlphanumeric() = runTest {
        val code1 = repo.generateUniqueQuizCode()
        val code2 = repo.generateUniqueQuizCode()

        assertEquals(6, code1.length)
        assertEquals(6, code2.length)
        assertTrue(code1.matches(Regex("^[2-9A-Z]{6}$")))
        assertTrue(code2.matches(Regex("^[2-9A-Z]{6}$")))
        assertTrue("Subsequent codes should be distinct", code1 != code2)
    }

    @Test
    fun testQuizCodeAccessRequiresPublishedStatus() = runTest {
        val uniqueCode = repo.generateUniqueQuizCode()
        val draftQuiz = Quiz(
            id = "test_draft_quiz_${System.currentTimeMillis()}",
            title = "Draft Test Quiz",
            description = "Draft description",
            category = "Kotlin",
            quizCode = uniqueCode,
            status = QuizStatus.DRAFT,
            creationMethod = QuizCreationMethod.MANUAL,
            questions = listOf(
                Question(
                    id = "q1",
                    type = QuestionType.SINGLE,
                    prompt = "Sample?",
                    options = listOf(Option("A", "Yes"), Option("B", "No")),
                    correctAnswers = setOf("A"),
                    explanation = "Test"
                )
            )
        )

        repo.createQuiz(draftQuiz)

        val accessResult = repo.validateAndAccessQuiz(uniqueCode, "student_123")
        assertTrue("Draft quiz must not be accessible to students", accessResult.isFailure)
        assertTrue(
            accessResult.exceptionOrNull()?.message?.contains("draft", ignoreCase = true) == true
        )
    }

    @Test
    fun testIndividualStudentPublishingAuthorization() = runTest {
        val uniqueCode = repo.generateUniqueQuizCode()
        val privateQuiz = Quiz(
            id = "test_private_quiz_${System.currentTimeMillis()}",
            title = "Private Assigned Quiz",
            description = "For student 7 only",
            category = "Algorithms",
            quizCode = uniqueCode,
            status = QuizStatus.PUBLISHED,
            targetStudentIds = listOf("student_7"),
            targetBatchIds = emptyList(),
            questions = listOf(
                Question(
                    id = "q1",
                    type = QuestionType.SINGLE,
                    prompt = "Sample?",
                    options = listOf(Option("A", "Yes"), Option("B", "No")),
                    correctAnswers = setOf("A"),
                    explanation = "Test"
                )
            )
        )

        repo.createQuiz(privateQuiz)

        // Unauthorized student 8 attempts
        val unauthorized = repo.validateAndAccessQuiz(uniqueCode, "student_8")
        assertTrue("Unauthorized student must fail access", unauthorized.isFailure)

        // Authorized student 7 attempts
        val authorized = repo.validateAndAccessQuiz(uniqueCode, "student_7")
        assertTrue("Authorized student must succeed", authorized.isSuccess)
        assertEquals("Private Assigned Quiz", authorized.getOrNull()?.title)
    }
}
