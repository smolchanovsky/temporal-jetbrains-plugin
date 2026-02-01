package com.github.smolchanovsky.temporalplugin.ui.onboarding

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.ui.GotItTooltip
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler
import javax.swing.JComponent

data class ShowOnboardingConnectHelp(
    val component: JComponent,
    val disposable: Disposable
) : Request.Unit

class ShowOnboardingConnectHelpHandler : RequestHandler.Unit<ShowOnboardingConnectHelp> {

    companion object {
        private const val TOOLTIP_ID = "temporal.onboarding.connect"
    }

    override suspend fun handle(request: ShowOnboardingConnectHelp) {
        val connectButton = findFirstToolbarButton(request.component) ?: return

        val tooltip = GotItTooltip(TOOLTIP_ID, TextBundle.message("onboarding.connect"), request.disposable)
            .withHeader(TextBundle.message("onboarding.header"))

        if (tooltip.canShow()) {
            tooltip.show(connectButton, GotItTooltip.BOTTOM_MIDDLE)
        }
    }

    private fun findFirstToolbarButton(component: JComponent): JComponent? {
        if (component is ActionToolbarImpl) {
            return component.components.firstOrNull { it.isVisible } as? JComponent
        }
        for (child in component.components) {
            if (child is JComponent) {
                val found = findFirstToolbarButton(child)
                if (found != null) return found
            }
        }
        return null
    }
}
