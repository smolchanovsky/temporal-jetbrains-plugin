package com.github.smolchanovsky.temporalplugin.ui.navigation.finders

class GoWorkflowFinder : BaseTextSearchFinder() {

    override fun getSupportedFileExtensions(): Set<String> = setOf("go")

    override fun getLanguageName(): String = "Go"

    override fun getExactSearchPatterns(workflowType: String): List<Regex> {
        val escapedName = Regex.escape(workflowType)
        return listOf(
            // func WorkflowName(ctx workflow.Context, ...)
            Regex("""func\s+$escapedName\s*\([^)]*workflow\.Context"""),
            // func (r *Receiver) WorkflowName(ctx workflow.Context, ...) - method receiver
            Regex("""func\s+\([^)]+\)\s+$escapedName\s*\([^)]*workflow\.Context""")
        )
    }

    override fun getFallbackPatterns(): List<Pair<Regex, String>> {
        return listOf(
            // Match any function with workflow.Context parameter
            Regex("""func\s+(\w+)\s*\([^)]*workflow\.Context""") to "function",
            // Match method receivers with workflow.Context
            Regex("""func\s+\([^)]+\)\s+(\w+)\s*\([^)]*workflow\.Context""") to "method"
        )
    }

    override fun getDefinitionTypeFromPattern(pattern: Regex): String = "function"

    override fun extractNamespace(content: String): String? {
        val packagePattern = Regex("""^package\s+(\w+)""", RegexOption.MULTILINE)
        return packagePattern.find(content)?.groupValues?.getOrNull(1)
    }
}
