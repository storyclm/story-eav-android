package expert.rightperception.attributesapp.ui.content

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import expert.rightperception.attributesapp.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_content.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.breffi.story.domain.bridge.*
import ru.breffi.story.domain.bridge.StoryBridgeView.RestoreHandler
import ru.breffi.story.domain.bridge.model.BridgeSettings
import ru.breffi.story.domain.bridge.model.CloseMode
import ru.breffi.story.domain.bridge.model.SessionData
import ru.breffi.story.util.setSystemUiVisible
import java.io.File


class ContentFragment : Fragment(), ContentView, StoryBridgeView {

    companion object {
        const val TAG = "ContentFragment"
        const val KEY_SESSION_ID = "session_id"

        private const val PRESENTATION_ID = "PRESENTATION_ID"
        private const val INJECTION_SCRIPT = "INJECTION_SCRIPT"

        fun newInstance(presentationId: Int, script: String): ContentFragment {
            return ContentFragment().apply {
                arguments = Bundle().apply {
                    putInt(PRESENTATION_ID, presentationId)
                    putString(INJECTION_SCRIPT, script)
                }
            }
        }
    }

    private class Story {

        var app = "DemoApp"

        var extra = ExtraData()
    }

    private class ExtraData {

        var version = 1
    }

    private val compositeDisposable = CompositeDisposable()

    private lateinit var storyBridge: StoryBridge
    private lateinit var presentationControls: List<PresentationControls>

    private var restoreSessionDialog: AlertDialog? = null
    private var endSessionDialog: AlertDialog? = null

    private var showControlsJob: Job? = null

    private var sessionData: SessionData? = null

    private var script: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { args ->
            script = args.getString(INJECTION_SCRIPT, "")
            val id = args.getInt(PRESENTATION_ID, -1)
            if (id != -1) {
                createBridge(id, sessionData?.sessionId ?: savedInstanceState?.getString(KEY_SESSION_ID))
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_SESSION_ID, sessionData?.sessionId)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        requireActivity().setSystemUiVisible(true)

        storyBridge.stopSession(true)

        compositeDisposable.dispose()
        restoreSessionDialog?.dismiss()
        endSessionDialog?.dismiss()
        storyBridge.onBridgeViewDestroyed()

        super.onDestroyView()
    }

    private fun createBridge(presentationId: Int, sessionId: String?) {
        val bridgeSettings = BridgeSettings(sessionId = sessionId, userId = "demo", resumeSession = sessionId != null, restoreSessions = false)
        val bridgeContext = StoryBridgeContext(this, presentationId, bridgeSettings)
        val modules = listOf<BridgeModule>(
//            UiModule(requireActivity(), close_button)
        )
        presentationControls = modules.mapNotNull { it as? PresentationControls }
        storyBridge = StoryBridgeFactory.create(bridgeContext, modules)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initPresentationControls() {
        val gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                showControlsJob?.cancel()
                presentationControls.forEach { it.setVisibility(true) }
                showControlsJob = lifecycleScope.launch {
                    delay(5000)
                    presentationControls.forEach { it.setVisibility(false) }
                }
                return false
            }
        })
//        contentView.setOnTouchListener { _, event ->
//            gestureDetector.onTouchEvent(event)
//        }
        actionBtn.setOnClickListener {
            val updatedStory = Story().apply { extra.version = 99 }
            contentView.evaluateJavascript("var story = ${Gson().toJson(updatedStory)}") {
                contentView.loadUrl("javascript:onStoryChange()")
            }
        }
        close_button.setOnClickListener { closePresentation(CloseMode.DIALOG) }
    }

    @Suppress("DEPRECATION")
    override fun closePresentation(closeMode: CloseMode) {
        //no-op
    }

    override fun endSession(save: Boolean) {
        storyBridge.stopSession(save)
    }

    override fun onInitialized(sessionData: SessionData) {
        this.sessionData = sessionData
        initPresentationControls()

        WebView.setWebContentsDebuggingEnabled(true)
        class JsObject {

            @JavascriptInterface
            fun changeStory(story: String) {
                Log.e("DBG_1", "$story")
            }
        }

        var initialized = false
        contentView.addJavascriptInterface(JsObject(), "changeStoryObject")
        contentView.webViewClient = object : WebViewClient() {


            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                return if (request.url.toString().contains(".html")) {
                    val obj = if (initialized) "" else "var story = ${Gson().toJson(Story())};\n"
                    initialized = true
                    val text = "<script id=\"context_injection_script\" type=\"text/javascript\">$obj$script\ndocument.getElementById('context_injection_script').remove();</script>" + File(request.url.toString().replace("file://", "")).readText()
                    WebResourceResponse("text/html", "UTF-8", text.byteInputStream())
                } else {
                    null
                }
            }
        }
    }

    override fun onFailed(reason: String) {
        Log.d(TAG, "Failed with reason: $reason")
    }

    override fun getWebView(): WebView {
        return contentView
    }

    override fun onRequestRestoreSession(handler: RestoreHandler) {
        restoreSessionDialog = AlertDialog.Builder(requireContext()).setTitle(R.string.restore_session)
            .setNegativeButton(R.string.no) { dialog, _ ->
                handler.proceed(false)
                dialog.dismiss()
            }
            .setPositiveButton(R.string.yes) { dialog, _ ->
                handler.proceed(true)
                dialog.dismiss()
            }
            .show()
    }
}