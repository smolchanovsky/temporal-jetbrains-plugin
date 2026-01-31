package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

class GoWorkflowRegistrationStrategyTest : GoWorkflowStrategyTestBase() {

    private val strategy = GoWorkflowRegistrationStrategy()

    private val testCode = """
        package workflows

        import (
            "go.temporal.io/sdk/worker"
            "go.temporal.io/sdk/workflow"
        )

        const ThirdWorkflowName = "ThirdWorkflow"

        func thirdWorkflowImpl(ctx workflow.Context, input string) error {
            return nil
        }

        func RegisterWorkflows(w worker.Worker) {
            w.RegisterWorkflowWithOptions(thirdWorkflowImpl, workflow.RegisterOptions{
                Name: "FourthWorkflow",
            })

            w.RegisterWorkflowWithOptions(thirdWorkflowImpl, workflow.RegisterOptions{
                Name: ThirdWorkflowName,
            })
        }
    """.trimIndent()

    fun `test finds workflow by string literal name`() = withGoFile(testCode) { goFile ->
        val matches = strategy.findMatches(goFile, "FourthWorkflow")

        assertEquals(1, matches.size)
        assertEquals("FourthWorkflow", matches[0].workflowType)
        assertEquals("registered workflow", matches[0].definitionType)
    }

    fun `test finds workflow by constant name`() = withGoFile(testCode) { goFile ->
        val matches = strategy.findMatches(goFile, "ThirdWorkflow")

        assertEquals(1, matches.size)
        assertEquals("ThirdWorkflow", matches[0].workflowType)
        assertEquals("registered workflow", matches[0].definitionType)
    }

    fun `test finds all registered workflows when workflowType is null`() = withGoFile(testCode) { goFile ->
        val matches = strategy.findMatches(goFile, null)

        assertEquals(2, matches.size)
        val types = matches.map { it.workflowType }.toSet()
        assertTrue(types.contains("FourthWorkflow"))
        assertTrue(types.contains("ThirdWorkflow"))
    }

    fun `test returns empty for non-existent workflow`() = withGoFile(testCode) { goFile ->
        val matches = strategy.findMatches(goFile, "NonExistent")

        assertTrue(matches.isEmpty())
    }
}
