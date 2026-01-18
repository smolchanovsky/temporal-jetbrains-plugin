package com.github.smolchanovsky.temporalplugin.domain

data class Namespace(val name: String) {
    companion object {
        val DEFAULT = Namespace("default")
    }
}
