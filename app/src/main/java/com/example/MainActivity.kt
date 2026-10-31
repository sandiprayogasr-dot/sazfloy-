package com.example

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ProjectRepository
import com.example.editor.templates.TemplateData
import com.example.editor.templates.VideoTemplate
import com.example.model.AspectRatioMode
import com.example.model.AudioTrack
import com.example.model.ClipType
import com.example.model.ColorAdjustments
import com.example.model.EditingProject
import com.example.model.MediaClip
import com.example.model.ProjectType
import com.example.model.ScreenOrientationMode
import com.example.model.TextOverlay
import com.example.model.TransitionType
import java.util.UUID
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PhotoEditorScreen
import com.example.ui.screens.VideoEditorScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    data class VideoEditor(val project: EditingProject) : Screen()
    data class PhotoEditor(val project: EditingProject?, val imageUri: Uri? = null) : Screen()
}

class MainViewModel(private val repository: ProjectRepository, private val context: Context) : ViewModel() {

    var currentScreen by mutableStateOf<Screen>(Screen.Home)
        private set

    var orientationMode by mutableStateOf(ScreenOrientationMode.AUTO)
        private set

    val projects: StateFlow<List<EditingProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Safe migration: clean up any legacy sample/demo projects from older test builds without touching user projects
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("sazfloy_prefs", Context.MODE_PRIVATE)
            val hasCleanedDemo = prefs.getBoolean("has_cleaned_legacy_demo_projects_v2", false)
            if (!hasCleanedDemo) {
                runCatching {
                    val currentList = repository.allProjects.first()
                    currentList.filter { 
                        it.title == "SAZFLOY Cinematic Reel" || it.title == "Sunset Portrait Glow"
                    }.forEach { demo ->
                        repository.deleteProject(demo.id)
                    }
                    prefs.edit().putBoolean("has_cleaned_legacy_demo_projects_v2", true).apply()
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }

    fun setOrientation(activity: Activity, mode: ScreenOrientationMode) {
        orientationMode = mode
        when (mode) {
            ScreenOrientationMode.AUTO -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            ScreenOrientationMode.PORTRAIT -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            ScreenOrientationMode.LANDSCAPE -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
    }

    fun createProjectFromMedia(uris: List<Uri>, context: Context) {
        viewModelScope.launch {
            val importedClips = uris.map { uri ->
                val mime = context.contentResolver.getType(uri) ?: ""
                val isVideo = mime.startsWith("video") || uri.toString().endsWith(".mp4", ignoreCase = true)
                com.example.media.MediaManager.importMediaFromPicker(context, uri, isVideo)
            }
            val newProj = EditingProject(
                id = UUID.randomUUID().toString(),
                title = "Project ${System.currentTimeMillis() % 1000}",
                type = ProjectType.VIDEO,
                aspectRatio = AspectRatioMode.RATIO_9_16,
                clips = if (importedClips.isNotEmpty()) importedClips else listOf(
                    MediaClip(name = "Clip 1", durationMs = 3000L),
                    MediaClip(name = "Clip 2", durationMs = 3000L)
                ),
                textOverlays = listOf(
                    TextOverlay(text = "SAZFLOY Title", startMs = 0L, durationMs = 3000L, posYRatio = 0.5f)
                )
            )
            repository.saveProject(newProj)
            currentScreen = Screen.VideoEditor(newProj)
        }
    }

    fun createNewProject() {
        val newProj = EditingProject(
            id = UUID.randomUUID().toString(),
            title = "New Project ${System.currentTimeMillis() % 1000}",
            type = ProjectType.VIDEO,
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clips = listOf(
                MediaClip(name = "Clip 1", durationMs = 3000L, transitionType = TransitionType.FADE),
                MediaClip(name = "Clip 2", durationMs = 3000L, transitionType = TransitionType.DISSOLVE)
            ),
            textOverlays = listOf(
                TextOverlay(text = "SAZFLOY Title", startMs = 0L, durationMs = 3000L, posYRatio = 0.5f)
            )
        )
        viewModelScope.launch {
            repository.saveProject(newProj)
            currentScreen = Screen.VideoEditor(newProj)
        }
    }

    fun createPhotoProject(uri: Uri?, context: Context) {
        viewModelScope.launch {
            val clip = if (uri != null) {
                com.example.media.MediaManager.importMediaFromPicker(context, uri, isVideo = false)
            } else {
                MediaClip(name = "Portrait Master", durationMs = 3000L, type = ClipType.PHOTO)
            }
            val newProj = EditingProject(
                id = UUID.randomUUID().toString(),
                title = "Photo ${System.currentTimeMillis() % 1000}",
                type = ProjectType.PHOTO,
                aspectRatio = AspectRatioMode.RATIO_4_5,
                clips = listOf(clip)
            )
            repository.saveProject(newProj)
            val clipUri = if (clip.uri.isNotBlank()) Uri.parse(clip.uri) else uri
            currentScreen = Screen.PhotoEditor(project = newProj, imageUri = clipUri)
        }
    }

    fun startFromTemplate(template: VideoTemplate) {
        val proj = template.instantiateProject()
        viewModelScope.launch {
            repository.saveProject(proj)
            currentScreen = Screen.VideoEditor(proj)
        }
    }

    fun saveProject(project: EditingProject) {
        viewModelScope.launch {
            repository.saveProject(project)
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    fun renameProject(id: String, newTitle: String) {
        viewModelScope.launch {
            repository.updateProjectTitle(id, newTitle)
        }
    }

    fun duplicateProject(project: EditingProject) {
        val duplicated = project.copy(
            id = UUID.randomUUID().toString(),
            title = "${project.title} (Salinan)",
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveProject(duplicated)
        }
    }
}

class MainViewModelFactory(
    private val repository: ProjectRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = ProjectRepository(applicationContext)
        val viewModelFactory = MainViewModelFactory(repository, applicationContext)

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
                val projects by viewModel.projects.collectAsState()
                val currentScreen = viewModel.currentScreen
                val orientationMode = viewModel.orientationMode
                val activity = LocalContext.current as Activity

                Crossfade(
                    targetState = currentScreen,
                    modifier = Modifier.fillMaxSize(),
                    label = "screen_crossfade"
                ) { screen ->
                    when (screen) {
                        is Screen.Home -> {
                            HomeScreen(
                                projects = projects,
                                orientationMode = orientationMode,
                                onToggleOrientation = { mode -> viewModel.setOrientation(activity, mode) },
                                onNewProject = { viewModel.createNewProject() },
                                onNewProjectWithMedia = { uris -> viewModel.createProjectFromMedia(uris, applicationContext) },
                                onOpenProject = { proj ->
                                    if (proj.type == ProjectType.VIDEO) {
                                        viewModel.navigateTo(Screen.VideoEditor(proj))
                                    } else {
                                        val clipUri = proj.clips.firstOrNull()?.uri?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
                                        viewModel.navigateTo(Screen.PhotoEditor(project = proj, imageUri = clipUri))
                                    }
                                },
                                onDeleteProject = { id -> viewModel.deleteProject(id) },
                                onRenameProject = { id, title -> viewModel.renameProject(id, title) },
                                onDuplicateProject = { proj -> viewModel.duplicateProject(proj) },
                                onStartPhotoEditor = { uri -> viewModel.createPhotoProject(uri, applicationContext) },
                                onStartFromTemplate = { template -> viewModel.startFromTemplate(template) }
                            )
                        }
                        is Screen.VideoEditor -> {
                            VideoEditorScreen(
                                project = screen.project,
                                orientationMode = orientationMode,
                                onToggleOrientation = { mode -> viewModel.setOrientation(activity, mode) },
                                onSaveProject = { updated -> viewModel.saveProject(updated) },
                                onNavigateBack = { viewModel.navigateTo(Screen.Home) }
                            )
                        }
                        is Screen.PhotoEditor -> {
                            PhotoEditorScreen(
                                initialProject = screen.project,
                                initialImageUri = screen.imageUri,
                                onSaveProject = { updated -> viewModel.saveProject(updated) },
                                onNavigateBack = { viewModel.navigateTo(Screen.Home) }
                            )
                        }
                    }
                }
            }
        }
    }
}
