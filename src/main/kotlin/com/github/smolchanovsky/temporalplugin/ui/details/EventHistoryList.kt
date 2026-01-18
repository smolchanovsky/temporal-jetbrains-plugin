package com.github.smolchanovsky.temporalplugin.ui.details

import com.github.smolchanovsky.temporalplugin.domain.EventCategory
import com.github.smolchanovsky.temporalplugin.domain.WorkflowEvent
import com.github.smolchanovsky.temporalplugin.domain.WorkflowEventType
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.github.smolchanovsky.temporalplugin.ui.common.FormatUtils
import com.intellij.icons.AllIcons
import com.intellij.util.IconUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.SwingUtilities

class EventHistoryList(project: Project) : JPanel(BorderLayout()), Disposable {

    private val state = project.service<TemporalState>()

    private val listPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.empty(8)
    }

    private val onViewStateChanged: (ViewState) -> Unit = { viewState ->
        if (viewState is ViewState.WorkflowDetailsView) {
            SwingUtilities.invokeLater { update(viewState.history?.events ?: emptyList()) }
        }
    }

    init {
        add(JBScrollPane(listPanel).apply { border = null }, BorderLayout.CENTER)
        state.addViewStateListener(onViewStateChanged)
    }

    private fun update(events: List<WorkflowEvent>) {
        listPanel.removeAll()
        for (event in events.reversed()) {
            listPanel.add(EventListItem(event))
            listPanel.add(Box.createVerticalStrut(4))
        }
        listPanel.add(Box.createVerticalGlue())
        listPanel.revalidate()
        listPanel.repaint()
    }

    override fun dispose() {
        state.removeViewStateListener(onViewStateChanged)
    }
}

class EventListItem(private val event: WorkflowEvent) : JPanel(BorderLayout()) {

    private var expanded = false
    private val expandIcon = JBLabel(AllIcons.General.ArrowRight)
    private val detailsPanel = EventDetailsPanel(event).apply { isVisible = false }

    init {
        border = JBUI.Borders.empty(4, 8)
        background = JBColor.background()

        add(createHeader(), BorderLayout.NORTH)
        add(detailsPanel, BorderLayout.CENTER)
    }

    private fun createHeader(): JPanel {
        val header = JPanel(BorderLayout()).apply {
            isOpaque = false
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        }

        expandIcon.border = JBUI.Borders.emptyRight(8)
        header.add(expandIcon, BorderLayout.WEST)
        header.add(createInfoPanel(), BorderLayout.CENTER)
        header.add(createEventIdLabel(), BorderLayout.EAST)

        header.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) = toggleExpanded()
            override fun mouseEntered(e: MouseEvent) {
                this@EventListItem.background = JBColor(0xF5F5F5, 0x3C3F41)
            }
            override fun mouseExited(e: MouseEvent) {
                this@EventListItem.background = JBColor.background()
            }
        })

        return header
    }

    private fun createInfoPanel(): JPanel {
        val panel = JPanel(BorderLayout()).apply { isOpaque = false }

        val title = buildTitle()
        val titleLabel = JBLabel(title).apply {
            font = JBUI.Fonts.label().deriveFont(font.style or Font.BOLD)
            icon = EventTypePresentation.getIcon(event.eventType)
            iconTextGap = 8
        }
        panel.add(titleLabel, BorderLayout.CENTER)

        val timestampLabel = JBLabel(FormatUtils.formatTime(event.timestamp)).apply {
            foreground = JBUI.CurrentTheme.Label.disabledForeground()
            font = JBUI.Fonts.smallFont()
        }
        panel.add(timestampLabel, BorderLayout.EAST)

        return panel
    }

    private fun createEventIdLabel() = JBLabel("#${event.eventId}").apply {
        foreground = JBUI.CurrentTheme.Label.disabledForeground()
        font = JBUI.Fonts.smallFont()
        border = JBUI.Borders.emptyLeft(8)
    }

    private fun buildTitle(): String {
        val base = event.eventType.displayName
        return when {
            event.activityName != null -> "$base [${event.activityName}]"
            event.timerId != null -> "$base [${event.timerId}]"
            else -> base
        }
    }

    private fun toggleExpanded() {
        expanded = !expanded
        detailsPanel.isVisible = expanded
        expandIcon.icon = if (expanded) AllIcons.General.ArrowDown else AllIcons.General.ArrowRight
        revalidate()
        repaint()
    }
}

object EventTypePresentation {

    private fun getColor(type: WorkflowEventType): JBColor = when {
        type.isCompleted || type.isFired -> JBColor(0x34A853, 0x59A869)   // Green
        type.isFailed -> JBColor(0xEA4335, 0xDB5860)                       // Red
        type.isCanceled -> JBColor(0x9E9E9E, 0x6E6E6E)                     // Gray
        type.isTerminated -> JBColor(0x9C27B0, 0xAB47BC)                   // Purple
        type.isTimedOut -> JBColor(0xFBBC04, 0xE0A82E)                     // Yellow
        type.isStarted -> JBColor(0x4285F4, 0x589DF6)                      // Blue
        type.isScheduled || type.isInitiated -> JBColor(0x00BCD4, 0x00ACC1) // Cyan
        type.isSignaled -> JBColor(0x00BCD4, 0x00BCD4)                     // Cyan
        else -> JBColor(0x5F6368, 0x9E9E9E)                                // Gray
    }

    fun getIcon(type: WorkflowEventType): Icon {
        val baseIcon = when (type.category) {
            EventCategory.TASK, EventCategory.MARKER, EventCategory.OTHER -> AllIcons.General.GearPlain
            else -> AllIcons.Nodes.Function
        }
        return IconUtil.colorize(baseIcon, getColor(type))
    }
}
