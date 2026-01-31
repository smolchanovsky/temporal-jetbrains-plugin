package com.github.smolchanovsky.temporalplugin.usecase.navigation.java

class JavaSpringWorkflowImplStrategyTest : JavaWorkflowStrategyTestBase() {

    private val strategy = JavaSpringWorkflowImplStrategy()

    override fun setUp() {
        super.setUp()

        addClass("""
            package com.example;
            
            import io.temporal.workflow.*;
            
            @WorkflowInterface
            public interface FirstWorkflow {
                @WorkflowMethod
                void execute(String input);
            }
        """)

        addClass("""
            package com.example;
            
            import io.temporal.spring.boot.WorkflowImpl;
            
            @WorkflowImpl(workers = "worker")
            public class FirstWorkflowImpl implements FirstWorkflow {
                public void execute(String input) {}
            }
        """)

        // Implementation WITHOUT @WorkflowImpl - should not be found
        addClass("""
            package com.example;
            
            public class FirstWorkflowNoAnnotation implements FirstWorkflow {
                public void execute(String input) {}
            }
        """)

        addClass("""
            package com.example;
            
            import io.temporal.workflow.*;
            
            @WorkflowInterface
            public interface SecondWorkflow {
                @WorkflowMethod(name = "CustomName")
                void run(String input);
            }
        """)

        addClass("""
            package com.example;

            import io.temporal.spring.boot.WorkflowImpl;

            @WorkflowImpl(workers = "worker")
            public class SecondWorkflowImpl implements SecondWorkflow {
                public void run(String input) {}
            }
        """)

        // Interface and implementation in different packages
        addClass("""
            package com.example.api;

            import io.temporal.workflow.*;

            @WorkflowInterface
            public interface ThirdWorkflow {
                @WorkflowMethod
                void process();
            }
        """)

        addClass("""
            package com.example.impl;

            import com.example.api.ThirdWorkflow;
            import io.temporal.spring.boot.WorkflowImpl;

            @WorkflowImpl(workers = "worker")
            public class ThirdWorkflowImpl implements ThirdWorkflow {
                public void process() {}
            }
        """)
    }

    fun `test finds workflow by interface name`() {
        val matches = strategy.findMatches(project, scope, "FirstWorkflow")

        assertEquals(1, matches.size)
        assertEquals("FirstWorkflow", matches[0].workflowType)
        assertEquals("implementation", matches[0].definitionType)
    }

    fun `test finds workflow by WorkflowMethod name`() {
        val matches = strategy.findMatches(project, scope, "CustomName")

        assertEquals(1, matches.size)
        assertEquals("CustomName", matches[0].workflowType)
    }

    fun `test does not find implementation without WorkflowImpl`() {
        val matches = strategy.findMatches(project, scope, "FirstWorkflow")

        assertEquals(1, matches.size)
        assertTrue(matches[0].fileName.contains("FirstWorkflowImpl"))
    }

    fun `test finds workflow when interface and impl in different packages`() {
        val matches = strategy.findMatches(project, scope, "ThirdWorkflow")

        assertEquals(1, matches.size)
        assertEquals("ThirdWorkflow", matches[0].workflowType)
        assertEquals("com.example.impl", matches[0].namespace)
    }

    fun `test finds all workflows when workflowType is null`() {
        val matches = strategy.findMatches(project, scope, null)

        assertEquals(3, matches.size)
        val types = matches.map { it.workflowType }.toSet()
        assertEquals(setOf("FirstWorkflow", "CustomName", "ThirdWorkflow"), types)
    }

    fun `test returns empty for non-existent workflow`() {
        val matches = strategy.findMatches(project, scope, "NonExistent")

        assertTrue(matches.isEmpty())
    }
}
