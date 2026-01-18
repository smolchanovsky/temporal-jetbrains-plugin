package com.github.smolchanovsky.temporalplugin.ui.navigation.finders

import com.github.smolchanovsky.temporalplugin.ui.navigation.WorkflowDefinitionFinder
import com.github.smolchanovsky.temporalplugin.ui.navigation.WorkflowNavigationItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

abstract class BaseTextSearchFinder : WorkflowDefinitionFinder {

    protected data class PatternMatch(
        val workflowType: String,
        val offset: Int,
        val lineNumber: Int,
        val definitionType: String
    )

    protected abstract fun getExactSearchPatterns(workflowType: String): List<Regex>
    protected abstract fun getFallbackPatterns(): List<Pair<Regex, String>>

    override fun findNavigationItems(
        project: Project,
        workflowType: String,
        scope: GlobalSearchScope
    ): List<WorkflowNavigationItem> {
        val results = mutableListOf<WorkflowNavigationItem>()
        val patterns = getExactSearchPatterns(workflowType)
        val psiManager = PsiManager.getInstance(project)

        for (extension in getSupportedFileExtensions()) {
            val files = FilenameIndex.getAllFilesByExt(project, extension, scope)
            for (file in files) {
                val content = readFileContent(file) ?: continue
                val matches = findMatchesWithPatterns(content, patterns, workflowType)
                val namespace = extractNamespace(content)

                val psiFile = psiManager.findFile(file) ?: continue

                for (match in matches) {
                    val element = psiFile.findElementAt(match.offset) ?: continue
                    results.add(
                        WorkflowNavigationItem(
                            element = element,
                            workflowType = match.workflowType,
                            definitionType = match.definitionType,
                            language = getLanguageName(),
                            namespace = namespace
                        )
                    )
                }
            }
        }

        return results
    }

    override fun findAllNavigationItems(
        project: Project,
        scope: GlobalSearchScope
    ): List<WorkflowNavigationItem> {
        val results = mutableListOf<WorkflowNavigationItem>()
        val fallbackPatterns = getFallbackPatterns()
        val psiManager = PsiManager.getInstance(project)

        for (extension in getSupportedFileExtensions()) {
            val files = FilenameIndex.getAllFilesByExt(project, extension, scope)
            for (file in files) {
                val content = readFileContent(file) ?: continue
                val matches = findAllMatchesWithFallback(content, fallbackPatterns)
                val namespace = extractNamespace(content)

                val psiFile = psiManager.findFile(file) ?: continue

                for (match in matches) {
                    val element = psiFile.findElementAt(match.offset) ?: continue
                    results.add(
                        WorkflowNavigationItem(
                            element = element,
                            workflowType = match.workflowType,
                            definitionType = match.definitionType,
                            language = getLanguageName(),
                            namespace = namespace
                        )
                    )
                }
            }
        }

        return results
    }

    protected abstract fun extractNamespace(content: String): String?

    private fun findMatchesWithPatterns(
        content: String,
        patterns: List<Regex>,
        workflowType: String
    ): List<PatternMatch> {
        val matches = mutableListOf<PatternMatch>()

        for (pattern in patterns) {
            val matchResult = pattern.find(content)
            if (matchResult != null) {
                val lineNumber = content.substring(0, matchResult.range.first).count { it == '\n' } + 1
                matches.add(
                    PatternMatch(
                        workflowType = workflowType,
                        offset = matchResult.range.first,
                        lineNumber = lineNumber,
                        definitionType = getDefinitionTypeFromPattern(pattern)
                    )
                )
            }
        }

        return matches
    }

    private fun findAllMatchesWithFallback(
        content: String,
        patterns: List<Pair<Regex, String>>
    ): List<PatternMatch> {
        val matches = mutableListOf<PatternMatch>()

        for ((pattern, definitionType) in patterns) {
            val allMatches = pattern.findAll(content)
            for (matchResult in allMatches) {
                val workflowName = extractWorkflowName(matchResult)
                if (workflowName != null) {
                    val lineNumber = content.substring(0, matchResult.range.first).count { it == '\n' } + 1
                    matches.add(
                        PatternMatch(
                            workflowType = workflowName,
                            offset = matchResult.range.first,
                            lineNumber = lineNumber,
                            definitionType = definitionType
                        )
                    )
                }
            }
        }

        return matches
    }

    protected open fun extractWorkflowName(matchResult: MatchResult): String? {
        return matchResult.groupValues.getOrNull(1)?.takeIf { it.isNotEmpty() }
    }

    protected open fun getDefinitionTypeFromPattern(pattern: Regex): String = "definition"

    private fun readFileContent(file: VirtualFile): String? {
        return try {
            String(file.contentsToByteArray(), file.charset)
        } catch (e: Exception) {
            null
        }
    }
}
