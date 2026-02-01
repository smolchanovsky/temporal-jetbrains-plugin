package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

class GoWorkflowNameConstantStrategyTest : GoWorkflowStrategyTestBase() {

    private val strategy = GoWorkflowNameConstantStrategy()

    override fun setUp() {
        super.setUp()

        // Constant and method in same file
        addGoFile("workflows/first.go", """
            package workflows

            import "go.temporal.io/sdk/workflow"

            const workflowName = "FirstWorkflow"

            type FirstHandler struct{}

            func (h *FirstHandler) Execute(ctx workflow.Context) error {
                return nil
            }
        """)

        // Constant in one file, method in another (same package)
        addGoFile("workflows/constants.go", """
            package workflows

            const workflow_type = "SecondWorkflow"
        """)

        addGoFile("workflows/second.go", """
            package workflows

            import "go.temporal.io/sdk/workflow"

            type SecondHandler struct{}

            func (h *SecondHandler) Run(ctx workflow.Context) error {
                return nil
            }
        """)

        // Different package
        addGoFile("other/workflow.go", """
            package other

            import "go.temporal.io/sdk/workflow"

            const WorkflowName = "ThirdWorkflow"

            type ThirdHandler struct{}

            func (h *ThirdHandler) Process(ctx workflow.Context) error {
                return nil
            }
        """)

        // File without workflowName constant - should not match
        addGoFile("utils/helper.go", """
            package utils

            import "go.temporal.io/sdk/workflow"

            func HelperWorkflow(ctx workflow.Context) error {
                return nil
            }
        """)
    }

    fun `test finds workflow by constant workflowName`() {
        val matches = strategy.findMatches(project, scope, "FirstWorkflow")

        assertEquals(1, matches.size)
        assertEquals("FirstWorkflow", matches[0].workflowType)
        assertEquals("workflow method", matches[0].definitionType)
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

    fun `test returns empty when no workflowName constant`() {
        val matches = strategy.findMatches(project, scope, "HelperWorkflow")

        assertTrue(matches.isEmpty())
    }
}
