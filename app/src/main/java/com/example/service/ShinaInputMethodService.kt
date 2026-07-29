package com.example.service

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import com.example.data.ShinaDatabase
import com.example.data.ShinaKeyboardRepository
import com.example.ai.ShinaAiEngine
import com.example.model.KeyboardPreferences
import com.example.ui.components.KeyboardLayoutView
import com.example.ui.theme.ShinaTheme
import kotlinx.coroutines.*

class ShinaInputMethodService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val mViewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = mViewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var database: ShinaDatabase
    private lateinit var repository: ShinaKeyboardRepository
    private lateinit var aiEngine: ShinaAiEngine

    private var currentText: String = ""
    private var keyboardPreferences = KeyboardPreferences()

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        database = ShinaDatabase.getDatabase(this)
        repository = ShinaKeyboardRepository(database)
        aiEngine = ShinaAiEngine(database)
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val composeView = ComposeView(this)
        composeView.setViewTreeLifecycleOwner(this)
        composeView.setViewTreeViewModelStoreOwner(this)
        composeView.setViewTreeSavedStateRegistryOwner(this)

        composeView.setContent {
            ShinaTheme(theme = keyboardPreferences.theme) {
                KeyboardLayoutView(
                    preferences = keyboardPreferences,
                    currentText = currentText,
                    onKeyPress = { char ->
                        currentInputConnection?.commitText(char, 1)
                        currentText += char
                        if (!keyboardPreferences.incognitoMode) {
                            serviceScope.launch {
                                aiEngine.learnFromTyping(currentText, keyboardPreferences.dialect.displayName)
                            }
                        }
                    },
                    onBackspace = {
                        if (currentText.isNotEmpty()) {
                            currentText = currentText.dropLast(1)
                        }
                        currentInputConnection?.deleteSurroundingText(1, 0)
                    },
                    onSpace = {
                        currentInputConnection?.commitText(" ", 1)
                        currentText += " "
                    },
                    onEnter = {
                        currentInputConnection?.commitText("\n", 1)
                        currentText += "\n"
                    },
                    onSuggestionSelected = { suggestion ->
                        // Replace last word
                        val words = currentText.trim().split(Regex("\\s+"))
                        if (words.isNotEmpty()) {
                            val lastWordLen = words.last().length
                            currentInputConnection?.deleteSurroundingText(lastWordLen, 0)
                            currentInputConnection?.commitText("$suggestion ", 1)
                            val newText = words.dropLast(1).joinToString(" ") + " $suggestion "
                            currentText = newText.trimStart()
                        }
                    },
                    onPreferenceChange = { updated ->
                        keyboardPreferences = updated
                    }
                )
            }
        }
        return composeView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        currentText = ""
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceScope.cancel()
    }
}
