package dk.napp.jockey;

import java.util.Map;

import android.webkit.JsResult;
import android.widget.Toast;
import android.os.Build ; 

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;

import ti.modules.titanium.ui.widget.webview.TiUIWebView;
import ti.modules.titanium.ui.widget.webview.TiWebChromeClient;
import ti.modules.titanium.ui.widget.webview.TiWebViewClient;

import com.jockeyjs.Jockey;
import com.jockeyjs.JockeyHandler;
import com.jockeyjs.JockeyImpl;

import android.util.Log;
import android.webkit.WebView;
import android.webkit.CookieManager;

public class DkNappJockeyWebView extends TiUIWebView  {

	private Jockey jockey;
	// Static Properties
	private static final String TAG = "DkNappJockeyWebView";
	public static final String PROPERTY_WEBVIEW_URL = "url";

	public DkNappJockeyWebView(TiViewProxy proxy) {
		super(proxy);

		String ua = getUserAgentString();
		
		// Add user agent. Dont replace - jockey will die if "Android" is not included
		setUserAgentString(ua + DkNappJockeyModule.userAgent);
		
		// Get the default Jockey Implementation
		jockey = JockeyImpl.getDefault();
		
		// Configure your webView to be used with Jockey
		jockey.configure(getWebView());

		// Pass Jockey your custom WebViewClient
	    // Notice we can do this even after our webView has been configured.
		jockey.setWebViewClient(new TiWebViewClient(this, getWebView()) {
			@Override
			public void onPageFinished(WebView view, String url) {
				// In 4.0.0 we cannot call super.onPageFinished anymore, because TiWebViewClient assumes the proxy to be a WebViewProxy
				//super.onPageFinished(view, url);
				
				//Log.d(TAG, "page finished loading!");
				
				TiUIWebView webview = DkNappJockeyWebView.this;
				
				
				TiViewProxy proxy = webview.getProxy();
				
				webview.changeProxyUrl(url);
				
				KrollDict data = new KrollDict();
				data.put("url", url);
				
				proxy.fireEvent(TiC.EVENT_LOAD, data);
			}
		});
		
		
		getWebView().setWebChromeClient(new TiWebChromeClient(this) {
			// Handling alerts from Javascript
			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				//Toast.makeText(getProxy().getActivity(), message, Toast.LENGTH_SHORT).show();
				result.confirm();
				return true;
			}
		});
	}

	// Send an event to JavaScript, passing a payload
	void sendJockeyEvent(final String event_name,final  Object payload) {
		getProxy().getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				jockey.send(event_name, getWebView(), payload);
			}
		});

	}

	//Listen for an event from JavaScript and log a message when we have receied it.
	void setJockeyListener(final String event_name) {
		jockey.on(event_name, new JockeyHandler() {
			@Override
			protected void doPerform(Map<Object, Object> payload) {
				if (getProxy().hasListeners(event_name)) {
					getProxy().fireEvent(event_name, payload);
				}
			}
		});
	}

}