package com.github.smolchanovsky.temporalplugin.ui.common

import com.github.smolchanovsky.temporalplugin.domain.WorkflowStatus
import com.intellij.ui.JBColor
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.Icon

object WorkflowStatusPresentation {

    fun getIcon(status: WorkflowStatus): Icon = when (status) {
        WorkflowStatus.RUNNING -> CircleIcon(JBColor(0x4285F4, 0x589DF6))      // Blue
        WorkflowStatus.COMPLETED -> CircleIcon(JBColor(0x34A853, 0x59A869))    // Green
        WorkflowStatus.FAILED -> CircleIcon(JBColor(0xEA4335, 0xDB5860))       // Red
        WorkflowStatus.CANCELED -> CircleIcon(JBColor(0x9E9E9E, 0x6E6E6E))     // Gray
        WorkflowStatus.TERMINATED -> CircleIcon(JBColor(0x9C27B0, 0xAB47BC))   // Purple
        WorkflowStatus.TIMED_OUT -> CircleIcon(JBColor(0xFBBC04, 0xE0A82E))    // Yellow
        WorkflowStatus.CONTINUED_AS_NEW -> CircleIcon(JBColor(0x00BCD4, 0x00ACC1)) // Cyan
        WorkflowStatus.UNKNOWN -> CircleIcon(JBColor(0x9E9E9E, 0x6E6E6E))      // Gray
    }

    private class CircleIcon(private val color: Color, private val size: Int = 12) : Icon {
        override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
            val g2 = g.create() as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.color = color
            g2.fillOval(x, y, size, size)
            g2.dispose()
        }

        override fun getIconWidth(): Int = size
        override fun getIconHeight(): Int = size
    }
}
