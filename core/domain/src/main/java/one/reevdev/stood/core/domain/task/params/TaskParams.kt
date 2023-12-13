package one.reevdev.stood.core.domain.task.params

import one.reevdev.stood.core.domain.task.model.Category

data class TaskParams(
    val title: String,
    val time: String,
    val date: String,
    val priority: Int,
    val category: Category
)
