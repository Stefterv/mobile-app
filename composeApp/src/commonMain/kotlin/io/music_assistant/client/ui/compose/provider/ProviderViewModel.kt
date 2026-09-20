package io.music_assistant.client.ui.compose.provider

import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.music_assistant.client.api.Request
import io.music_assistant.client.api.ServiceClient
import io.music_assistant.client.data.model.client.ProviderDetails
import io.music_assistant.client.data.model.server.ServerMediaItem
import io.music_assistant.client.data.model.server.ServerProviderInstance
import io.music_assistant.client.ui.compose.common.icons.BookshelfIcon
import io.music_assistant.client.ui.compose.common.providers.ProviderIconModel
import io.music_assistant.client.utils.resultAs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProviderViewModel(private val serviceClient: ServiceClient) : ViewModel() {
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

    fun getProviderIcon(domain: String): StateFlow<ProviderIconModel?> {
        val stateFlow = MutableStateFlow<ProviderIconModel?>(null)

        if (domain == ServerMediaItem.LIBRARY_PROVIDER) {
            stateFlow.value = ProviderIconModel.Mdi(BookshelfIcon, Color.White)
        } else {
            viewModelScope.launch {
                val iconSvg =
                    serviceClient.sendRequest(Request.Provider.icon(domain)).resultAs<String>()
                stateFlow.value = ProviderIconModel.from(null, iconSvg)
            }
        }

        return stateFlow
    }
}
