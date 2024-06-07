package com.kemarport.kyms.viewmodel.export

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kemarport.kyms.repository.KYMSRepository

class StockTallyWoAsnVMProviderFactory(
    private val application: Application,
    private val kymsRepository: KYMSRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StockTallyWoAsnVM(application, kymsRepository) as T
    }
}