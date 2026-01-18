package com.github.smolchanovsky.temporalplugin.usecase

import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateWriter
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler

object DisconnectUseCase : Request.Unit

class DisconnectUseCaseHandler(
    private val state: TemporalStateWriter
) : RequestHandler.Unit<DisconnectUseCase> {

    override suspend fun handle(request: DisconnectUseCase) {
        state.updateConnectionState(ConnectionState.Disconnected)
        state.updateWorkflows(emptyList())
    }
}
