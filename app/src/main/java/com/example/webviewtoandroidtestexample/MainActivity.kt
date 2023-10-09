package com.example.webviewtoandroidtestexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.example.webviewtoandroidtestexample.databinding.ActivityMainBinding
import com.example.webviewtoandroidtestexample.databinding.FragmentWebviewBinding


class MainActivity : FragmentActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btGotoWeb.setOnClickListener {
        val url = binding.tvUrl.text.toString().ifBlank {
            "file:///android_asset/samplewebpage.html"
        }
            if (url.isNotBlank()) {
                val bundle = Bundle().apply {
                    putString(WebViewDialogFragment.EXTRA_WEB_URL, url)
                }
                WebViewDialogFragment.show(bundle, this) { receivedMessage ->
                    binding.tvUrl.apply {
                        text.clear()
                        setText(receivedMessage)
                    }
                }
            } else Toast.makeText(this, "Url can't be blank", Toast.LENGTH_SHORT).show()
        }
    }
}



class WebViewDialogFragment(private val onMessageReceived: (String) -> Unit): DialogFragment() {

    companion object {
        const val EXTRA_WEB_URL = "web_url"
        private const val TAG = "scan_qr_code_dialog_fragment"

        fun show(bundle: Bundle, activity: FragmentActivity, resultCallback: (String) -> Unit) {
            WebViewDialogFragment(resultCallback).apply {
                arguments = bundle
                show(activity.supportFragmentManager, TAG)
            }
        }
    }

    private lateinit var binding: FragmentWebviewBinding
    private lateinit var webPageclient: WebViewClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWebviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            title = "Web view"
        }

        val webUrl = arguments?.getString(EXTRA_WEB_URL)
        webPageclient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest?
            ): Boolean {
                view.loadUrl(request?.url.toString())
                return true
            }
        }
        binding.webpage.apply {
            settings.run {
                javaScriptEnabled = true
                builtInZoomControls = true
                supportZoom()
            }
            addJavascriptInterface(JSBridge(onMessageReceived), "JSBridge")
            webViewClient = webPageclient
            loadUrl(webUrl!!)
        }
    }

    /**
     * Receive message from webview and pass on to native.
     */
    inner class JSBridge(private val onMessageReceived: (String) -> Unit) {
        @JavascriptInterface
        fun showMessageInNative(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            onMessageReceived(message)
            this@WebViewDialogFragment.dismiss()
        }
    }

    /**
     * Send data to webview through function updateFromNative.
     */
    private fun sendDataToWebView(webView: WebView, messageToSend: String){
        webView.evaluateJavascript(
            "javascript: updateFromNative(\"$messageToSend\")",
            null)
    } // We don't need this for our usecase here

}