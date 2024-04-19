package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.launch

class RemindersListViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {

    val remindersList = MutableLiveData<List<ReminderDataItem>>()

    fun loadReminders() {
        showLoading.value = true
        viewModelScope.launch {
            val result = dataSource.getReminders()
            showLoading.postValue(false)
            handleResult(result)
        }
    }

    private fun handleResult(result: Result<*>) {
        when (result) {
            is Result.Success<*> -> {
                val dataList = (result.data as? List<ReminderDTO>)?.map { reminderDTO ->
                    ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                } ?: emptyList()
                remindersList.value = dataList
            }
            is Result.Error ->
                showSnackBar.value = result.message
        }
        invalidateShowNoData()
    }

    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value.isNullOrEmpty()
    }
}
