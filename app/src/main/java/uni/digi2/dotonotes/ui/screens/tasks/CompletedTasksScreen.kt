package uni.digi2.dotonotes.ui.screens.tasks

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
fun CompletedTasksScreen(viewModel: TodoViewModel = TodoViewModel(TaskRepository(TodoTasksDao()))) {
    val tasks by viewModel.tasks.collectAsState()
    val showDeleteDialog = remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()

    Scaffold(
        content = {
            it.calculateBottomPadding()
            Column {
                Text("Completed ToDo List", style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(tasks.filter { item -> item.completed }) { task ->
                        CompletedTaskItem(
                            task = task,
                            onTaskUpdate = { updatedTask ->
                                auth.currentUser?.let { it1 ->
                                    viewModel.updateTask(
                                        it1.uid,
                                        updatedTask
                                    )
                                }
                            },
                            showDeleteDialog = {showDeleteDialog.value = task.id}
                        )
                    }
                }
            }
        }
    )

    if (showDeleteDialog.value != "") {
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


@Composable
fun CompletedTaskItem(
    task: TodoTask,
    onTaskUpdate: (TodoTask) -> Unit,
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
            onClick = showDeleteDialog
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Task")
        }
    }
}
