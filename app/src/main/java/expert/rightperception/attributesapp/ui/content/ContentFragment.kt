package expert.rightperception.attributesapp.ui.content

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.lifecycleScope
import expert.rightperception.attributesapp.R
import expert.rightperception.attributesapp.domain.model.objects.ObjectsContainer
import expert.rightperception.attributesapp.ui.common.InjectableFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_content.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.breffi.story.domain.bridge.*
import ru.breffi.story.domain.bridge.StoryBridgeView.RestoreHandler
import ru.breffi.story.domain.bridge.model.*
import ru.breffi.story.util.setSystemUiVisible
import javax.inject.Inject


class ContentFragment : InjectableFragment(), ContentView, StoryBridgeView {

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

    @Inject
    lateinit var viewModel: ContentViewModel

    private val compositeDisposable = CompositeDisposable()

    private lateinit var storyBridge: StoryBridge
    private lateinit var presentationControls: List<PresentationControls>

    private var restoreSessionDialog: AlertDialog? = null
    private var endSessionDialog: AlertDialog? = null

    private var showControlsJob: Job? = null

    private var sessionData: SessionData? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        WebView.setWebContentsDebuggingEnabled(true)
        arguments?.let { args ->
            val id = args.getInt(PRESENTATION_ID, -1)
            if (id != -1) {
                viewModel.getInitialData().observe(viewLifecycleOwner) { initialObject ->
                    val sessionId = sessionData?.sessionId ?: savedInstanceState?.getString(KEY_SESSION_ID)
                    createBridge(id, sessionId, initialObject)
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

    private fun createBridge(presentationId: Int, sessionId: String?, initialObject: ObjectsContainer) {
        val attrsContextObjectConfiguration = ContextObjectConfiguration(
            name = "presentation",
            initialObject = initialObject,
            mutabilityPolicy = AppContentMutablePolicy(viewModel.storyObjectRepository, viewModel.storyObjectRepository)
        )
        val bridgeSettings = BridgeSettings(
            sessionId = sessionId,
            userId = "demo",
            resumeSession = sessionId != null,
            restoreSessions = false
        )
        val bridgeContext = StoryBridgeContext(
            this,
            presentationId,
            bridgeSettings,
            listOf(attrsContextObjectConfiguration)
        )
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