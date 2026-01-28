package com.github.smolchanovsky.temporalplugin.ui.analytics

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.analytics.AnalyticsSettings
import com.intellij.ide.BrowserUtil
import com.intellij.ui.EditorNotificationPanel
import java.awt.BorderLayout
import javax.swing.JPanel

class AnalyticsConsentBanner(
    private val onDismiss: () -> Unit
) : JPanel(BorderLayout()) {

    private val settings = AnalyticsSettings.getInstance()

    init {
        val panel = EditorNotificationPanel(EditorNotificationPanel.Status.Info).apply {
            text = TextBundle.message("analytics.consent.message")

            createActionLabel(TextBundle.message("analytics.consent.accept")) {
                settings.markConsentAsked()
                settings.analyticsEnabled = true
                onDismiss()
            }

            createActionLabel(TextBundle.message("analytics.consent.decline")) {
                settings.markConsentAsked()
                settings.analyticsEnabled = false
                onDismiss()
            }

            createActionLabel(TextBundle.message("analytics.consent.learnMore")) {
                BrowserUtil.browse(TextBundle.message("analytics.privacyUrl"))
            }
        }

        add(panel, BorderLayout.CENTER)
    }

    companion object {
        fun shouldShow(): Boolean = !AnalyticsSettings.getInstance().consentAsked
    }
}
