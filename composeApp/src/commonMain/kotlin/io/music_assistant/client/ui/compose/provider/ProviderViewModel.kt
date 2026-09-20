package io.music_assistant.client.ui.compose.provider

import androidx.collection.LruCache
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
    private val providerIconsCache = LruCache<String, ProviderIconModel>(10)

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

        viewModelScope.launch {
            val iconModel = if (domain == ServerMediaItem.LIBRARY_PROVIDER) {
                ProviderIconModel.Mdi(BookshelfIcon, Color.White)
            } else {
                val iconSvg =
                    serviceClient.sendRequest(Request.Provider.icon(domain)).resultAs<String>()
                if (iconSvg != null) ProviderIconModel.fromSvg(iconSvg) else null
            }

            if (iconModel != null) {
                stateFlow.value = iconModel
                providerIconsCache.put(domain, iconModel)
            }
        }

        return stateFlow
    }
}
