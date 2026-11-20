package com.example.water

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WaterRepository(application)
    val snapshot = repository.data.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WaterSnapshot())
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun add(amountMl: Int) = perform { repository.addDrink(amountMl) }
    fun undo() = perform { repository.undoToday() }

    private fun perform(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
                _error.value = null
            } catch (error: Exception) {
                _error.value = "Could not save your drink. Please try again."
            }
        }
    }
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WaterRepository(application)
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun saveGoal(goalMl: Int, onSaved: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.setGoal(goalMl)
                _error.value = null
                onSaved()
            } catch (error: Exception) {
                _error.value = "Could not save your goal. Please try again."
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            try {
                repository.setTheme(theme)
                _error.value = null
            } catch (error: Exception) {
                _error.value = "Could not save your appearance. Please try again."
            }
        }
    }
}
