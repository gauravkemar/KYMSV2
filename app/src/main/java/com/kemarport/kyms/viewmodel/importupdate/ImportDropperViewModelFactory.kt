package com.kemarport.kyms.viewmodel.importupdate

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kemarport.kyms.repository.KYMSRepository

class ImportDropperViewModelFactory  (
    private val application: Application,
    private val kymsRepository: KYMSRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ImportDropperViewModel(application, kymsRepository) as T
    }
}