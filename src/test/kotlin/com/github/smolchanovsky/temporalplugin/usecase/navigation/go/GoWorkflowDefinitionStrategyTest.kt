package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

class GoWorkflowDefinitionStrategyTest : GoWorkflowStrategyTestBase() {

    private val strategy = GoWorkflowDefinitionStrategy()

    private val testCode = """
        package workflows

        import (
            "go.temporal.io/sdk/workflow"
        )

        func FirstWorkflow(ctx workflow.Context, input string) error {
            return nil
        }

        type Workflows struct{}

        func (w *Workflows) SecondWorkflow(ctx workflow.Context, params Params) error {
            return nil
        }

        func HelperFunction(input string) error {
            return nil
        }

        type Params struct {
            Value string
        }
    """.trimIndent()

    fun `test finds workflow function by name`() = withGoFile(testCode) { goFile ->
        val matches = strategy.findMatches(goFile, "FirstWorkflow")

        assertEquals(1, matches.size)
        assertEquals("FirstWorkflow", matches[0].workflowType)
        assertEquals("function", matches[0].definitionType)
    }

    fun `test finds workflow method by name`() = withGoFile(testCode) { goFile ->
        val matches = strategy.findMatches(goFile, "SecondWorkflow")

        assertEquals(1, matches.size)
        assertEquals("SecondWorkflow", matches[0].workflowType)
        assertEquals("method", matches[0].definitionType)
    }

    fun `test does not find non-workflow function`() = withGoFile(testCode) { goFile ->
        val matches = strategy.findMatches(goFile, "HelperFunction")

        assertTrue(matches.isEmpty())
    }

    fun `test finds all workflows when workflowType is null`() = withGoFile(testCode) { goFile ->
        val matches = strategy.findMatches(goFile, null)

        assertEquals(2, matches.size)
        val types = matches.map { it.workflowType }.toSet()
        assertTrue(types.contains("FirstWorkflow"))
        assertTrue(types.contains("SecondWorkflow"))
    }

    fun `test returns empty for non-existent workflow`() = withGoFile(testCode) { goFile ->
        val matches = strategy.findMatches(goFile, "NonExistent")

        assertTrue(matches.isEmpty())
    }
}
