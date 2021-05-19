package expert.rightperception.attributesapp.ui.content

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.lifecycleScope
import expert.rightperception.attributesapp.R
import expert.rightperception.attributesapp.ui.common.InjectableFragment
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
import javax.inject.Inject


class ContentFragment : InjectableFragment(), ContentView, StoryBridgeView {

    companion object {
        const val TAG = "ContentFragment"
        const val KEY_SESSION_ID = "session_id"

        private const val LICENSE_ID = "LICENSE_ID"
        private const val PRESENTATION_ID = "PRESENTATION_ID"
        private const val INJECTION_SCRIPT = "INJECTION_SCRIPT"

        fun newInstance(licenseId: String, presentationId: Int, script: String): ContentFragment {
            return ContentFragment().apply {
                arguments = Bundle().apply {
                    putString(LICENSE_ID, licenseId)
                    putInt(PRESENTATION_ID, presentationId)
                    putString(INJECTION_SCRIPT, script)
                }
            }
        }
    }

    @Inject
    lateinit var viewModel: ContentViewModel

    private val compositeDisposable = CompositeDisposable()

    private lateinit var storyBridge: StoryBridge
    private lateinit var presentationControls: List<PresentationControls>

    private var restoreSessionDialog: AlertDialog? = null
    private var endSessionDialog: AlertDialog? = null

    private var showControlsJob: Job? = null

    private var sessionData: SessionData? = null

    private var script: String = ""
    private var storyObject: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { args ->
            script = args.getString(INJECTION_SCRIPT, "")
            val id = args.getInt(PRESENTATION_ID, -1)
            if (id != -1) {
                viewModel.getData().observe(viewLifecycleOwner) { objectString ->
                    storyObject = objectString
                    createBridge(id, sessionData?.sessionId ?: savedInstanceState?.getString(KEY_SESSION_ID))
                }
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
                storyObject = story
                viewModel.updateStoryObject(story)
            }
        }

        contentView.addJavascriptInterface(JsObject(), "changeStoryObject")
        contentView.webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                return if (request.url.toString().contains(".html")) {
                    val obj = if (storyObject.isEmpty()) "" else "var _story = ${storyObject};\n"
                    val text = "<script id=\"context_injection_script\" type=\"text/javascript\">$obj$script\ndocument.getElementById('context_injection_script').remove();</script>" + File(request.url.toString().replace("file://", "")).readText()
                    WebResourceResponse("text/html", "UTF-8", text.byteInputStream())
                } else {
                    null
                }
            }
        }
        viewModel.storyObjectLiveData.observe(viewLifecycleOwner) { objectString ->
            if (objectString != storyObject) {
                storyObject = objectString
                contentView.evaluateJavascript("var _story = ${objectString}") {
                    contentView.loadUrl("javascript:_onStoryChange()")
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