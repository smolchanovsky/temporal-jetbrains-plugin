package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

import com.goide.GoFileType
import com.goide.psi.GoFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import junit.framework.TestCase

abstract class GoWorkflowStrategyTestBase : LightPlatformCodeInsightTestCase() {

    protected fun createGoFile(content: String): GoFile? {
        val psiFileFactory = PsiFileFactory.getInstance(project)
        val file: PsiFile = psiFileFactory.createFileFromText(
            "test.go",
            GoFileType.INSTANCE,
            content
        )

        return file as? GoFile
    }

    protected fun skipIfGoNotAvailable(goFile: GoFile?) {
        if (goFile == null) {
            println("SKIPPED: Go plugin not available")
            return
        }
    }

    protected inline fun withGoFile(content: String, test: (GoFile) -> Unit) {
        val goFile = createGoFile(content)
        if (goFile == null) {
            println("SKIPPED: Go plugin not available")
            return
        }
        test(goFile)
    }
}
