package com.example.onlinequizapp.data.session

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

/**
 * Manages standard and custom quiz categories.
 * Custom categories created by teachers are persistently stored in SharedPreferences
 * so they remain available across quiz creation sessions and app restarts.
 */
object CategoryManager {

    private const val PREFS_NAME = "quiz_master_custom_categories_prefs"
    private const val KEY_CUSTOM_CATEGORIES_JSON = "key_custom_categories_json"

    val DEFAULT_CATEGORIES: List<String> = listOf(
        // Core Programming & Languages
        "Kotlin",
        "Java",
        "Python",
        "JavaScript",
        "TypeScript",
        "C++",
        "HTML",
        "CSS",
        "C#",
        "Rust",
        "Go (Golang)",
        "Swift",
        "PHP",
        "Ruby",
        "Dart",
        "R Programming",
        "Shell Scripting",

        // Computer Science Foundations
        "Data Structures",
        "Algorithms & AI",
        "Computer Networks",
        "Database & SQL",
        "Operating Systems",
        "Object-Oriented Programming",
        "Functional Programming",
        "System Design",
        "Distributed Systems",
        "Compiler Design",
        "Computer Architecture",
        "Theory of Computation",
        "Cryptography & Encryption",
        "Discrete Mathematics",

        // Web Development & Design
        "Web Development",
        "Frontend Development",
        "Backend Development",
        "Full-Stack Development",
        "UI/UX Design",
        "Responsive Web Design",
        "Progressive Web Apps (PWA)",
        "RESTful API Design",
        "GraphQL",
        "Web Security (OWASP)",
        "Web Performance Optimization",
        "Content Management Systems (CMS)",

        // Mobile & Desktop Development
        "Android Development",
        "iOS Development",
        "Cross-Platform Mobile Dev",
        "Flutter",
        "React Native",
        "Desktop Application Dev",
        "Embedded Systems",
        "IoT (Internet of Things)",

        // Data Science & Advanced Analytics
        "Machine Learning",
        "Deep Learning",
        "Natural Language Processing (NLP)",
        "Computer Vision",
        "Data Science",
        "Data Engineering",
        "Big Data (Hadoop/Spark)",
        "Data Visualization",
        "Business Intelligence (BI)",
        "Predictive Analytics",

        // Cloud, DevOps & Infrastructure
        "Cloud Computing",
        "AWS (Amazon Web Services)",
        "Google Cloud Platform (GCP)",
        "Microsoft Azure",
        "DevOps Engineering",
        "CI/CD Pipelines",
        "Docker & Containerization",
        "Kubernetes & Orchestration",
        "Infrastructure as Code (IaC)",
        "Serverless Architecture",
        "Site Reliability Engineering (SRE)",
        "Linux Administration",

        // Databases & Storage
        "NoSQL Databases",
        "NewSQL Databases",
        "Graph Databases",
        "Vector Databases",
        "Data Warehousing",
        "Cache Management (Redis/Memcached)",

        // Cybersecurity & Networking
        "Cybersecurity",
        "Ethical Hacking & Pentesting",
        "Network Security",
        "Cloud Security",
        "Identity & Access Management (IAM)",
        "Digital Forensics",

        // Software Engineering Practices
        "Software Testing & QA",
        "Test-Driven Development (TDD)",
        "Agile & Scrum Methodologies",
        "Git & Version Control",
        "Clean Code & Refactoring",
        "Microservices Architecture",
        "API Testing & Automation",

        // Game Dev & Immersive Tech
        "Game Development",
        "Unity 3D",
        "Unreal Engine",
        "Augmented Reality (AR)",
        "Virtual Reality (VR)",
        "Computer Graphics",

        // Emerging Technologies & Domains
        "Generative AI & LLMs",
        "Blockchain & Web3",
        "Smart Contracts & Solidity",
        "Quantum Computing",
        "Edge Computing",
        "Robotics & Automation",
        "Bioinformatics",
        "Geographic Information Systems (GIS)"
    )

    private var context: Context? = null
    private var prefs: SharedPreferences? = null
    private val inMemoryCustomCategories = mutableListOf<String>()

    fun init(context: Context) {
        this.context = context.applicationContext
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadFromPrefs()
    }

    private fun getPrefs(): SharedPreferences? {
        if (prefs == null && context != null) {
            prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        return prefs
    }

    private fun loadFromPrefs() {
        val jsonString = getPrefs()?.getString(KEY_CUSTOM_CATEGORIES_JSON, null)
        if (!jsonString.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonString)
                inMemoryCustomCategories.clear()
                for (i in 0 until array.length()) {
                    val item = array.optString(i)?.trim()
                    if (!item.isNullOrBlank() && !inMemoryCustomCategories.contains(item)) {
                        inMemoryCustomCategories.add(item)
                    }
                }
            } catch (_: Exception) {
                // Ignore parse errors, keep current in-memory list
            }
        }
    }

    private fun saveToPrefs() {
        val array = JSONArray()
        inMemoryCustomCategories.forEach { array.put(it) }
        getPrefs()?.edit()?.putString(KEY_CUSTOM_CATEGORIES_JSON, array.toString())?.apply()
    }

    /**
     * Returns the list of custom categories added by the user.
     */
    fun getCustomCategories(): List<String> {
        if (inMemoryCustomCategories.isEmpty()) {
            loadFromPrefs()
        }
        return inMemoryCustomCategories.toList()
    }

    /**
     * Adds a new custom category and saves it to persistent storage.
     * Returns true if successfully added, false if it already exists or is invalid.
     */
    fun addCustomCategory(category: String): Boolean {
        val clean = category.trim()
        if (clean.isBlank()) return false
        
        // Check if already in default or custom list (case-insensitive)
        val existsInDefault = DEFAULT_CATEGORIES.any { it.equals(clean, ignoreCase = true) }
        val existsInCustom = inMemoryCustomCategories.any { it.equals(clean, ignoreCase = true) }

        if (!existsInDefault && !existsInCustom) {
            inMemoryCustomCategories.add(clean)
            saveToPrefs()
            return true
        }
        return false
    }

    /**
     * Returns all available categories: default built-ins followed by saved custom categories.
     */
    fun getAllCategories(): List<String> {
        val custom = getCustomCategories()
        val all = LinkedHashSet<String>()
        all.addAll(DEFAULT_CATEGORIES)
        all.addAll(custom)
        return all.toList()
    }
}
