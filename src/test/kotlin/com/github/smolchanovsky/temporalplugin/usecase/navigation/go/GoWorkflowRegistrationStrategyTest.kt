package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

class GoWorkflowRegistrationStrategyTest : GoWorkflowStrategyTestBase() {

    private val strategy = GoWorkflowRegistrationStrategy()

    override fun setUp() {
        super.setUp()

        addGoFile("workflows/impl.go", """
            package workflows

            import "go.temporal.io/sdk/workflow"

            func workflowImpl(ctx workflow.Context, input string) error {
                return nil
            }
        """)

        addGoFile("workflows/register.go", """
            package workflows

            import (
                "go.temporal.io/sdk/worker"
                "go.temporal.io/sdk/workflow"
            )

            func RegisterWorkflows(w worker.Worker) {
                w.RegisterWorkflowWithOptions(workflowImpl, workflow.RegisterOptions{
                    Name: "FirstWorkflow",
                })
            }
        """)

        // Constant in separate file, used in registration
        addGoFile("workflows/constants.go", """
            package workflows

            const SecondWorkflowName = "SecondWorkflow"
        """)

        addGoFile("workflows/register2.go", """
            package workflows

            import (
                "go.temporal.io/sdk/worker"
                "go.temporal.io/sdk/workflow"
            )

            func RegisterMore(w worker.Worker) {
                w.RegisterWorkflowWithOptions(workflowImpl, workflow.RegisterOptions{
                    Name: SecondWorkflowName,
                })
            }
        """)

        // Registration in different package
        addGoFile("other/setup.go", """
            package other

            import (
                "go.temporal.io/sdk/worker"
                "go.temporal.io/sdk/workflow"
            )

            func otherImpl(ctx workflow.Context) error {
                return nil
            }

            func Setup(w worker.Worker) {
                w.RegisterWorkflowWithOptions(otherImpl, workflow.RegisterOptions{
                    Name: "ThirdWorkflow",
                })
            }
        """)
    }

    fun `test finds workflow by string literal name`() {
        val matches = strategy.findMatches(project, scope, "FirstWorkflow")

        assertEquals(1, matches.size)
        assertEquals("FirstWorkflow", matches[0].workflowType)
        assertEquals("registered workflow", matches[0].definitionType)
    }

    fun `test finds workflow when constant in different file`() {
        val matches = strategy.findMatches(project, scope, "SecondWorkflow")

        assertEquals(1, matches.size)
        assertEquals("SecondWorkflow", matches[0].workflowType)
    }

    fun `test finds workflow in different package`() {
        val matches = strategy.findMatches(project, scope, "ThirdWorkflow")

        assertEquals(1, matches.size)
        assertEquals("ThirdWorkflow", matches[0].workflowType)
        assertEquals("other", matches[0].namespace)
    }

    fun `test finds all registered workflows when workflowType is null`() {
        val matches = strategy.findMatches(project, scope, null)

        assertEquals(3, matches.size)
        val types = matches.map { it.workflowType }.toSet()
        assertEquals(setOf("FirstWorkflow", "SecondWorkflow", "ThirdWorkflow"), types)
    }

    fun `test returns empty for non-existent workflow`() {
        val matches = strategy.findMatches(project, scope, "NonExistent")

        assertTrue(matches.isEmpty())
    }
}
