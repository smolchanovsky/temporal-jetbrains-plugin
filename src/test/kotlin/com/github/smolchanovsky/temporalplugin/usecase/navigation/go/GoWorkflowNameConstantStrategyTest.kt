package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

class GoWorkflowNameConstantStrategyTest : GoWorkflowStrategyTestBase() {

    private val strategy = GoWorkflowNameConstantStrategy()

    private val testCode = """
        package myworkflow

        import (
            "go.temporal.io/sdk/workflow"
        )

        const (
            workflowName = "FifthWorkflow"
        )

        type Workflow struct{}

        func (w *Workflow) Execute(ctx workflow.Context) error {
            return nil
        }
    """.trimIndent()

    private val testCodeNoConstant = """
        package workflows

        import (
            "go.temporal.io/sdk/workflow"
        )

        func SixthWorkflow(ctx workflow.Context, input string) error {
            return nil
        }
    """.trimIndent()

    fun `test finds workflow by constant workflowName`() = withGoFile(testCode) { goFile ->
        val matches = strategy.findMatches(goFile, "FifthWorkflow")

        assertEquals(1, matches.size)
        assertEquals("FifthWorkflow", matches[0].workflowType)
        assertEquals("workflow method", matches[0].definitionType)
    }

    fun `test finds all workflows when workflowType is null`() = withGoFile(testCode) { goFile ->
        val matches = strategy.findMatches(goFile, null)

        assertEquals(1, matches.size)
        assertEquals("FifthWorkflow", matches[0].workflowType)
    }

    fun `test returns empty for non-existent workflow`() = withGoFile(testCode) { goFile ->
        val matches = strategy.findMatches(goFile, "NonExistent")

        assertTrue(matches.isEmpty())
    }

    fun `test returns empty when no workflowName constant`() = withGoFile(testCodeNoConstant) { goFile ->
        val matches = strategy.findMatches(goFile, null)

        assertTrue(matches.isEmpty())
    }
}
