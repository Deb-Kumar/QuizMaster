package com.example.onlinequizapp.ui.components

import androidx.annotation.DrawableRes
import com.example.onlinequizapp.R

data class SubjectLogoItem(
    val id: String,
    val name: String,
    val category: String,
    @DrawableRes val drawableRes: Int
)

object SubjectLogoRegistry {
    val logos: List<SubjectLogoItem> = listOf(
        SubjectLogoItem("default", "Default", "General", R.drawable.default_logo),
        SubjectLogoItem("azure", "Azure", "Cloud", R.drawable.azure),
        SubjectLogoItem("aws", "AWS", "Cloud", R.drawable.aws),
        SubjectLogoItem("google_cloud", "Google Cloud", "Cloud", R.drawable.google_cloud),
        SubjectLogoItem("python", "Python", "Programming", R.drawable.python),
        SubjectLogoItem("java", "Java", "Programming", R.drawable.java),
        SubjectLogoItem("javascript", "JavaScript", "Web Development", R.drawable.javascript),
        SubjectLogoItem("typescript", "TypeScript", "Web Development", R.drawable.typescript),
        SubjectLogoItem("cpp", "C++", "Programming", R.drawable.cpp),
        SubjectLogoItem("c", "C Language", "Programming", R.drawable.c),
        SubjectLogoItem("csharp", "C#", "Programming", R.drawable.csharp),
        SubjectLogoItem("dotnet", ".NET", "Backend", R.drawable.dotnet),
        SubjectLogoItem("php", "PHP", "Backend", R.drawable.php),
        SubjectLogoItem("nodejs", "Node.js", "Backend", R.drawable.nodejs),
        SubjectLogoItem("react", "React", "Frontend", R.drawable.react),
        SubjectLogoItem("angular", "Angular", "Frontend", R.drawable.angular),
        SubjectLogoItem("vuejs", "Vue.js", "Frontend", R.drawable.vuejs),
        SubjectLogoItem("html", "HTML 5", "Web Development", R.drawable.html),
        SubjectLogoItem("css", "CSS 3", "Web Development", R.drawable.css),
        SubjectLogoItem("sql", "SQL", "Database", R.drawable.sql),
        SubjectLogoItem("mysql", "MySQL", "Database", R.drawable.mysql),
        SubjectLogoItem("postgresql", "PostgreSQL", "Database", R.drawable.postgresql),
        SubjectLogoItem("mongodb", "MongoDB", "Database", R.drawable.mongodb),
        SubjectLogoItem("redis", "Redis", "Database", R.drawable.redis),
        SubjectLogoItem("docker", "Docker", "DevOps", R.drawable.docker),
        SubjectLogoItem("linux", "Linux", "DevOps / OS", R.drawable.linux),
        SubjectLogoItem("nginx", "Nginx", "DevOps", R.drawable.nginx),
        SubjectLogoItem("github", "GitHub", "Tools", R.drawable.github),
        SubjectLogoItem("postman", "Postman", "Tools", R.drawable.postman),
        SubjectLogoItem("vscode", "VS Code", "Tools", R.drawable.vscode),
        SubjectLogoItem("api", "REST API", "Architecture", R.drawable.api),
        SubjectLogoItem("code", "Code", "Programming", R.drawable.code),
        SubjectLogoItem("microsoft_365", "Microsoft 365", "Office", R.drawable.microsoft_365),
        SubjectLogoItem("excel", "Excel", "Office", R.drawable.excel),
        SubjectLogoItem("word", "Word", "Office", R.drawable.word),
        SubjectLogoItem("powerpoint", "PowerPoint", "Office", R.drawable.powerpoint),
        SubjectLogoItem("android", "Android", "Mobile", R.drawable.android),
        SubjectLogoItem("django", "Django", "Backend", R.drawable.django),
        SubjectLogoItem("kotlin", "Kotlin", "Programming", R.drawable.kotlin),
        SubjectLogoItem("swift", "Swift", "Programming", R.drawable.swift),
        SubjectLogoItem("kubernetes", "Kubernetes", "DevOps", R.drawable.kubernetes),
        SubjectLogoItem("flutter", "Flutter", "Mobile", R.drawable.flutter),
        SubjectLogoItem("ruby", "Ruby", "Programming", R.drawable.ruby),
        SubjectLogoItem("math", "Math", "Mathematics", R.drawable.math),
     )

    fun getLogoById(id: String?): SubjectLogoItem? {
        if (id.isNullOrBlank()) return null
        return logos.find { it.id.equals(id.trim(), ignoreCase = true) }
    }

    fun getDrawableRes(id: String?): Int? {
        return getLogoById(id)?.drawableRes
    }
}
