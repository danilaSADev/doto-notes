package uni.digi2.dotonotes.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import uni.digi2.dotonotes.data.tasks.TaskRepository
import uni.digi2.dotonotes.data.tasks.TodoTask
import uni.digi2.dotonotes.data.tasks.TodoTasksDao
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(viewModel: TodoViewModel = TodoViewModel(TaskRepository(TodoTasksDao()))) {
    val tasks by viewModel.tasks.collectAsState()
    val showCreateDialog = remember { mutableStateOf(false) }
    val showEditDialog = remember { mutableStateOf("") }
    val showDeleteDialog = remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog.value = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        content = {
            it.calculateBottomPadding()
            Column() {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Todo List", style = MaterialTheme.typography.headlineLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(tasks.filter { item -> !item.completed }) { task ->
                        TodoTaskItem(
                            task = task,
                            onTaskUpdate = { updatedTask ->
                                auth.currentUser?.let { it1 ->
                                    viewModel.updateTask(
                                        it1.uid,
                                        updatedTask
                                    )
                                }
                            },
                            showEditDialog = { showEditDialog.value = task.id },
                            showDeleteDialog = { showDeleteDialog.value = task.id }
                        )
                    }
                }
//                Button(
//                    onClick = {
//                        if (tasks.isNotEmpty()) {
//                            showDeleteDialog.value = "all"
//                        }
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                        .height(56.dp),
//                    enabled = tasks.isNotEmpty()
//                ) {
//                    Text("Delete All")
//                }
            }
        }
    )

    if (showCreateDialog.value) {
        CreateTaskDialog(
            onTaskCreated = { task ->
                auth.currentUser?.let { it1 ->
                    viewModel.addTask(
                        it1.uid,
                        task
                    )
                }
            },
            onDismiss = { showCreateDialog.value = false }
        )
    }
    if (showEditDialog.value != "") {
        UpdateTaskDialog(
            tasks.first { it.id == showEditDialog.value },
            onTaskUpdated = { task ->
                auth.currentUser?.let { it1 ->
                    viewModel.updateTask(
                        it1.uid,
                        task
                    )
                }
            },
            onDismiss = { showEditDialog.value = "" }
        )
    }

    if (showDeleteDialog.value != "") {
        if (showDeleteDialog.value == "all") {
            DeleteAllTasksDialog(
                onTasksDeleted = {
                    auth.currentUser?.let { it1 ->
                        viewModel.deleteAllTasks(it1.uid)
                    }
                },
                onDismiss = { showDeleteDialog.value = "" }
            )
        } else {
            DeleteTaskDialog(
                tasks.first { it.id == showDeleteDialog.value },
                onTaskDeleted = { deletedTask ->
                    auth.currentUser?.let { it1 ->
                        viewModel.deleteTask(
                            it1.uid,
                            deletedTask.id
                        )
                    }
                },
                onDismiss = { showDeleteDialog.value = "" }
            )
        }
    }
}

@Composable
fun DeleteAllTasksDialog(
    onTasksDeleted: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete All Tasks") },
        text = { Text("Are you sure you want to delete all tasks?") },
        confirmButton = {
            Button(
                onClick = {
                    onTasksDeleted()
                    onDismiss()
                }
            ) {
                Text("Yes!")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateTaskDialog(
    onTaskCreated: (TodoTask) -> Unit,
    onDismiss: () -> Unit
) = TaskDialog(
    task = null,
    label = "Create ToDo",
    onSubmit = onTaskCreated,
    onDismiss = onDismiss
)

@Composable
fun UpdateTaskDialog(
    todoTask: TodoTask,
    onTaskUpdated: (TodoTask) -> Unit,
    onDismiss: () -> Unit
) = TaskDialog(
    task = todoTask,
    label = "Edit ToDo",
    onSubmit = onTaskUpdated,
    onDismiss = onDismiss
)

@Composable
fun DeleteTaskDialog(
    todoTask: TodoTask,
    onTaskDeleted: (TodoTask) -> Unit,
    onDismiss: () -> Unit
) = DeleteDialog(
    task = todoTask,
    label = "Delete ToDo",
    onSubmit = onTaskDeleted,
    onDismiss = onDismiss
)


@Composable
fun DeleteDialog(
    task: TodoTask?,
    label: String,
    onSubmit: (TodoTask) -> Unit,
    onDismiss: () -> Unit
) {
    var taskTitle by remember { mutableStateOf(task?.title ?: "") }
    var taskDescription by remember { mutableStateOf(task?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(label) },
        text = {
           Text(text =  "Are you sure you want to delete  \"${task?.title ?: "this"}\"  task?")
        },
        confirmButton = {
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        onSubmit(task?.copy(title = taskTitle, description = taskDescription) ?: TodoTask(title = taskTitle, description = taskDescription))
                        onDismiss()
                    }
                }
            ) {
                Text("Yes!")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    task: TodoTask?,
    label: String,
    onSubmit: (TodoTask) -> Unit,
    onDismiss: () -> Unit
) {
    var taskTitle by remember { mutableStateOf(task?.title ?: "") }
    var taskDescription by remember { mutableStateOf(task?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(label) },
        text = {
            Column {
                TextField(
                    value = taskTitle,
                    onValueChange = { newValue -> taskTitle = newValue },
                    label = { Text("Task Title") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = taskDescription,
                    onValueChange = { newValue -> taskDescription = newValue },
                    label = { Text("Description") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        onSubmit(task?.copy(title = taskTitle, description = taskDescription) ?: TodoTask(title = taskTitle, description = taskDescription))
                        onDismiss()
                    }
                }
            ) {
                Text("Submit!")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TodoTaskItem(
    task: TodoTask,
    onTaskUpdate: (TodoTask) -> Unit,
    showEditDialog: () -> Unit,
    showDeleteDialog: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.completed,
            onCheckedChange = { check ->
                onTaskUpdate(
                    task.copy(
                        completed = check,
                        checkedOn = if (check) Date() else task.checkedOn
                    )
                )
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = task.title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = showEditDialog
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Task")
        }
        IconButton(
            onClick = showDeleteDialog
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Task")
        }
    }
}
