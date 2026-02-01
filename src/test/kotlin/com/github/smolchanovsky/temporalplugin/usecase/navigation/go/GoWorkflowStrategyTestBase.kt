package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

import com.goide.GoCodeInsightFixtureTestCase
import com.goide.psi.GoFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope

abstract class GoWorkflowStrategyTestBase : GoCodeInsightFixtureTestCase() {

    protected val scope: GlobalSearchScope
        get() = GlobalSearchScope.allScope(project)

    override fun getTestDataPath(): String = "src/test/testData"

    protected fun addGoFile(name: String, content: String): GoFile {
        val virtualFile = myFixture.addFileToProject(name, content.trimIndent()).virtualFile
        return PsiManager.getInstance(project).findFile(virtualFile) as GoFile
    }
}
