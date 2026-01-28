package com.github.smolchanovsky.temporalplugin.utils

object AddressUtils {

    fun classifyAddress(value: String, default: String): String {
        val normalized = value.trim().lowercase()
            .removePrefix("http://")
            .removePrefix("https://")
        return when {
            normalized.isBlank() -> "empty"
            value == default -> "default"
            isLocalAddress(normalized) -> "local"
            else -> "remote"
        }
    }

    private fun isLocalAddress(address: String): Boolean {
        return address.startsWith("localhost") ||
            address.startsWith("127.0.0.1") ||
            address.startsWith("host.docker.internal") ||
            address.startsWith("172.17.") ||
            address.startsWith("172.18.") ||
            address.startsWith("host-gateway")
    }
}
