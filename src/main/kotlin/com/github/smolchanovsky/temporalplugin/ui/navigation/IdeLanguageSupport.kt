package com.github.smolchanovsky.temporalplugin.ui.navigation

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.intellij.openapi.application.ApplicationInfo

object IdeLanguageSupport {

    fun getContributionUrl(): String = TextBundle.message("navigation.unsupported.contributeUrl")

    fun getUnsupportedLanguageName(): String? {
        val productCode = ApplicationInfo.getInstance().build.productCode
        return getLanguageByProductCode(productCode)
    }

    private fun getLanguageByProductCode(productCode: String): String? {
        return when (productCode) {
            "PY", "PC" -> "Python"        // PyCharm
            "WS" -> "TypeScript"          // WebStorm
            "RD" -> ".NET"                // Rider
            "PS" -> "PHP"                 // PhpStorm
            "RM" -> "Ruby"                // RubyMine
            "CL" -> "C++"                 // CLion
            "GO" -> null                  // GoLand - supported
            "IU", "IC" -> null            // IntelliJ IDEA - supported
            "AI" -> null                  // Android Studio - supported
            else -> null                  // Unknown IDE
        }
    }
}
