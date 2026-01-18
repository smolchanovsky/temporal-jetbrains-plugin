package com.github.smolchanovsky.temporalplugin.state

import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.domain.Namespace

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data class Connecting(val namespace: String) : ConnectionState()
    data class Connected(val environment: Environment, val namespace: Namespace) : ConnectionState()
    data class Refreshing(val environment: Environment, val namespace: Namespace) : ConnectionState()
}
