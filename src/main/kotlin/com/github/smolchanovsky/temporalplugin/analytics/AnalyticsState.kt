package com.github.smolchanovsky.temporalplugin.analytics

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.extensions.PluginId

@Service(Service.Level.APP)
class AnalyticsState {

    @Volatile
    var temporalCliVersion: String? = null

    val userProperties: Map<String, Any> by lazy {
        val appInfo = ApplicationInfo.getInstance()
        val settings = AnalyticsSettings.getInstance()
        val plugin = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))
        val pluginVersion = plugin?.version ?: "unknown"
        val isEap = plugin?.version?.contains("eap", ignoreCase = true) == true ||
                    plugin?.version?.contains("alpha", ignoreCase = true) == true ||
                    plugin?.version?.contains("beta", ignoreCase = true) == true
        mapOf(
            "\$os" to System.getProperty("os.name", "unknown"),
            "install_id" to settings.installationId,
            "ide_type" to appInfo.build.productCode,
            "ide_version" to appInfo.fullVersion,
            "plugin_version" to pluginVersion,
            "eap" to isEap
        )
    }

    companion object {
        private const val PLUGIN_ID = "com.github.smolchanovsky.temporalplugin"

        fun getInstance(): AnalyticsState =
            ApplicationManager.getApplication().getService(AnalyticsState::class.java)
    }
}
