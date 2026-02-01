package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

class GoWorkflowDefinitionStrategyTest : GoWorkflowStrategyTestBase() {

    private val strategy = GoWorkflowDefinitionStrategy()

    override fun setUp() {
        super.setUp()

        addGoFile("workflows/first.go", """
            package workflows

            import "go.temporal.io/sdk/workflow"

            func FirstWorkflow(ctx workflow.Context, input string) error {
                return nil
            }
        """)

        addGoFile("workflows/second.go", """
            package workflows

            import "go.temporal.io/sdk/workflow"

            type Workflows struct{}

            func (w *Workflows) SecondWorkflow(ctx workflow.Context) error {
                return nil
            }
        """)

        // Workflow in different package
        addGoFile("other/third.go", """
            package other

            import "go.temporal.io/sdk/workflow"

            func ThirdWorkflow(ctx workflow.Context) error {
                return nil
            }
        """)

        // Non-workflow function
        addGoFile("utils/helper.go", """
            package utils

            func HelperFunction(input string) error {
                return nil
            }
        """)
    }

    fun `test finds workflow function by name`() {
        val matches = strategy.findMatches(project, scope, "FirstWorkflow")

        assertEquals(1, matches.size)
        assertEquals("FirstWorkflow", matches[0].workflowType)
        assertEquals("function", matches[0].definitionType)
    }

    fun `test finds workflow method by name`() {
        val matches = strategy.findMatches(project, scope, "SecondWorkflow")

        assertEquals(1, matches.size)
        assertEquals("SecondWorkflow", matches[0].workflowType)
        assertEquals("method", matches[0].definitionType)
    }

    fun `test finds workflow in different package`() {
        val matches = strategy.findMatches(project, scope, "ThirdWorkflow")

        assertEquals(1, matches.size)
        assertEquals("ThirdWorkflow", matches[0].workflowType)
        assertEquals("other", matches[0].namespace)
    }

    fun `test does not find non-workflow function`() {
        val matches = strategy.findMatches(project, scope, "HelperFunction")

        assertTrue(matches.isEmpty())
    }

    fun `test finds all workflows when workflowType is null`() {
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
