package one.reevdev.stood.features.task.screen.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import one.reevdev.stood.core.domain.task.TaskUseCase
import one.reevdev.stood.core.domain.task.mapToApiString
import one.reevdev.stood.core.domain.task.model.Category
import one.reevdev.stood.core.domain.task.model.TaskPriority
import one.reevdev.stood.core.domain.task.model.TaskTime
import one.reevdev.stood.core.domain.task.params.TaskParams
import one.reevdev.stood.features.task.utils.UiState
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val taskUseCase: TaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTaskUiState(false))
    val uiState: StateFlow<AddTaskUiState> by lazy { _uiState }

    fun init() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            taskUseCase.getCategories()
                .catch {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Something went wrong", // Todo: To be replaced by API message
                            isTaskSaved = false
                        )
                    }
                }
                .collect { categories ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            categories = categories
                        )
                    }
                }
        }
    }

    fun addTask(taskParams: TaskParams) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                with(taskParams) {
                    taskUseCase.createTask(
                        title = title,
                        priority = TaskPriority.values().first { it.priorityLevel == priority },
                        time = TaskTime(
                            fullISOFormat = mapToApiString(time, date),
                            time = time,
                            date = date
                        ),
                        categoryId = category.id
                    )
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        isTaskSaved = true
                    )
                }
            } catch(e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Something went wrong", // Todo: To be replaced by API message
                        isTaskSaved = false
                    )
                }
            }
        }
    }
}

data class AddTaskUiState(
    override val isLoading: Boolean,
    override val errorMessage: String? = null,
    val isTaskSaved: Boolean = false,
    val categories: List<Category> = emptyList(),
) : UiState