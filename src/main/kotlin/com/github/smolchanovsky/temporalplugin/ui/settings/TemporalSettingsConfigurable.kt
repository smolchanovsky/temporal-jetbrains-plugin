package com.github.smolchanovsky.temporalplugin.ui.settings

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel

class TemporalSettingsConfigurable(project: Project) : BoundConfigurable(
    TextBundle.message("settings.title")
) {

    private val settings = TemporalSettings.getInstance(project)

    override fun createPanel(): DialogPanel = panel {
        group(TextBundle.message("settings.cli.group")) {
            row(TextBundle.message("settings.cli.path")) {
                @Suppress("DEPRECATION")
                val pathField = textFieldWithBrowseButton(
                    FileChooserDescriptorFactory.createSingleFileDescriptor()
                        .withTitle(TextBundle.message("settings.cli.path.browse"))
                )
                    .columns(40)
                    .bindText(
                        getter = { settings.temporalCliPath ?: "" },
                        setter = { settings.temporalCliPath = it.ifBlank { null } }
                    )
                    .comment(TextBundle.message("settings.cli.path.help"))

                button(TextBundle.message("settings.cli.path.autoDetect")) {
                    TemporalCliDetector.detect()?.let { path ->
                        pathField.component.text = path
                    }
                }
            }
        }
        group(TextBundle.message("settings.server.group")) {
            row(TextBundle.message("settings.server.host")) {
                textField()
                    .columns(40)
                    .bindText(settings::serverAddress)
                    .comment(TextBundle.message("settings.server.host.help"))
            }
            row(TextBundle.message("settings.server.webUi")) {
                textField()
                    .columns(40)
                    .bindText(settings::webUiAddress)
                    .comment(TextBundle.message("settings.server.webUi.help"))
            }
        }
        group(TextBundle.message("settings.refresh.group")) {
            row(TextBundle.message("settings.refresh.interval")) {
                intTextField(1..300)
                    .bindIntText(settings::refreshIntervalSeconds)
                    .gap(RightGap.SMALL)
                text(TextBundle.message("settings.refresh.interval.seconds"))
                    .gap(RightGap.SMALL)
                contextHelp(TextBundle.message("settings.refresh.interval.help"))
            }
        }
    }
}
