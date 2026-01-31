package com.github.smolchanovsky.temporalplugin.usecase.navigation.java

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

abstract class JavaWorkflowStrategyTestBase : LightJavaCodeInsightFixtureTestCase() {

    protected val scope: GlobalSearchScope
        get() = GlobalSearchScope.allScope(project)

    override fun setUp() {
        super.setUp()
        addTemporalAnnotations()
    }

    private fun addTemporalAnnotations() {
        myFixture.addClass("""
            package io.temporal.workflow;
            
            import java.lang.annotation.*;
            
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.TYPE)
            public @interface WorkflowInterface {}
        """.trimIndent())

        myFixture.addClass("""
            package io.temporal.workflow;
            
            import java.lang.annotation.*;
            
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            public @interface WorkflowMethod {
                String name() default "";
            }
        """.trimIndent())

        myFixture.addClass("""
            package io.temporal.spring.boot;
            
            import java.lang.annotation.*;
            
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.TYPE)
            public @interface WorkflowImpl {
                String[] workers() default {};
            }
        """.trimIndent())
    }

    protected fun addClass(content: String) {
        myFixture.addClass(content.trimIndent())
    }
}
