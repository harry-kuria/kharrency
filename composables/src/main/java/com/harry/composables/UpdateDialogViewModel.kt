package com.harry.composables

import androidx.lifecycle.ViewModel
import com.harry.repository.ApkDownloadService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UpdateDialogViewModel @Inject constructor(
    val downloadService: ApkDownloadService
) : ViewModel() 