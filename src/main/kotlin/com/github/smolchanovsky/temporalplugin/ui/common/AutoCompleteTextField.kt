package com.github.smolchanovsky.temporalplugin.ui.common

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.project.Project
import com.intellij.util.textCompletion.TextCompletionProvider
import com.intellij.util.textCompletion.TextFieldWithCompletion

object AutoCompleteTextField {

    fun create(
        project: Project,
        items: Collection<String>,
        initialValue: String = ""
    ): TextFieldWithCompletion {
        val provider = StringsCompletionProvider(items.toList().sorted())

        return TextFieldWithCompletion(
            project,
            provider,
            initialValue,
            true,  // oneLineMode
            true,  // forceAutoPopup
            false  // showHint
        )
    }

    private class StringsCompletionProvider(
        private val items: List<String>
    ) : TextCompletionProvider {

        override fun getAdvertisement(): String? = null

        override fun getPrefix(text: String, offset: Int): String = text.substring(0, offset)

        override fun acceptChar(c: Char): com.intellij.codeInsight.lookup.CharFilter.Result? {
            return null // Accept all characters
        }

        override fun applyPrefixMatcher(
            result: com.intellij.codeInsight.completion.CompletionResultSet,
            prefix: String
        ): com.intellij.codeInsight.completion.CompletionResultSet {
            return result.withPrefixMatcher(prefix)
        }

        override fun fillCompletionVariants(
            parameters: CompletionParameters,
            prefix: String,
            result: com.intellij.codeInsight.completion.CompletionResultSet
        ) {
            val matcher = result.prefixMatcher
            items.filter { matcher.prefixMatches(it) }
                .forEach { result.addElement(com.intellij.codeInsight.lookup.LookupElementBuilder.create(it)) }
        }
    }
}
