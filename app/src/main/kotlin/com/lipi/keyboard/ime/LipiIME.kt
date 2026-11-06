package com.lipi.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.lipi.keyboard.keyboard.KeyboardRootView
import com.lipi.keyboard.suggest.SuggestionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LipiIME : InputMethodService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var suggestionEngine: SuggestionEngine
    private lateinit var controller: InputController
    private var rootView: KeyboardRootView? = null

    override fun onCreate() {
        super.onCreate()
        suggestionEngine = SuggestionEngine(this)
        serviceScope.launch {
            suggestionEngine.initialize()
        }

        controller = InputController(
            connectionProvider = { currentInputConnection },
            suggestionEngine = suggestionEngine,
            onCandidatesUpdated = { candidates ->
                rootView?.candidateBarView?.setCandidates(candidates)
            },
            onLayoutRequest = { layoutId ->
                rootView?.loadLayout(layoutId)
            }
        )
    }

    override fun onCreateInputView(): View {
        val root = KeyboardRootView(this, controller)
        rootView = root
        return root
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        controller.onStart(info, restarting)
        rootView?.loadLayout(controller.currentLayoutId)
    }

    override fun onFinishInput() {
        controller.onFinish()
        super.onFinishInput()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
