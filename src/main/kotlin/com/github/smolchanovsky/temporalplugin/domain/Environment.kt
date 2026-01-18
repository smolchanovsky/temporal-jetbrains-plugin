package com.github.smolchanovsky.temporalplugin.domain

data class Environment(val name: String) {
    companion object {
        val LOCAL = Environment("None")
    }

    val isLocal: Boolean get() = name == "None"
}
