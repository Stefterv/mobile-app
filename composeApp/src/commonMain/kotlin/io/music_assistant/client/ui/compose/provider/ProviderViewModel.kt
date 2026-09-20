package io.music_assistant.client.ui.compose.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.music_assistant.client.api.Request
import io.music_assistant.client.api.ServiceClient
import io.music_assistant.client.data.model.client.ProviderDetails
import io.music_assistant.client.data.model.server.ServerProviderInstance
import io.music_assistant.client.utils.resultAs
import kotlinx.coroutines.launch

class ProviderViewModel(serviceClient: ServiceClient) : ViewModel() {
    private val providerDetails = mutableMapOf<String, ProviderDetails>()

    init {
        viewModelScope.launch {
            serviceClient.sendRequest(Request.Library.providers())
                .resultAs<List<ServerProviderInstance>>()?.filter { it.type == "music" }
                ?.let { manifests ->
                    manifests.forEach {
                        providerDetails[it.domain] = ProviderDetails(it.name)
                    }
                }
        }
    }

    fun getProviderDetails(domain: String): ProviderDetails? {
        return providerDetails[domain]
    }
}
