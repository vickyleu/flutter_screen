package flutter.plugins.screen.screen

import android.provider.Settings
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import android.view.WindowManager
import android.provider.Settings.SettingNotFoundException
import flutter.plugins.screen.screen.ScreenPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

/**
 * ScreenPlugin
 */
class ScreenPlugin : FlutterPlugin,MethodCallHandler,ActivityAware{

    private var activityAware:ActivityPluginBinding?=null
    private var channel:MethodChannel?=null

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "brightness" -> result.success(brightness)
            "setBrightness" -> {
                val brightness = call.argument<Double>("brightness")!!
                val layoutParams = activityAware?.activity?.window?.attributes
                layoutParams?.screenBrightness = brightness.toFloat()
                activityAware?.activity?.window?.attributes = layoutParams
                result.success(null)
            }
            "isKeptOn" -> {
                val flags = activityAware?.activity?.window?.attributes?.flags?:0
                result.success(flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0)
            }
            "keepOn" -> {
                val on = call.argument<Boolean>("on")
                if (on!!) {
                    println("Keeping screen on ")
                    activityAware?.activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    println("Not keeping screen on")
                    activityAware?.activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    // the application is using the system brightness
    private val brightness: Float
        get() {
            var result = activityAware?.activity?.window?.attributes?.screenBrightness?:0f
            if (result < 0) { // the application is using the system brightness
                try {
                    result = Settings.System.getInt(
                        activityAware?.activity?.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS
                    ) / 255.toFloat()
                } catch (e: SettingNotFoundException) {
                    result = 1.0f
                    e.printStackTrace()
                }
            }
            return result
        }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel=
            MethodChannel(binding.binaryMessenger, "github.com/clovisnicolas/flutter_screen")
        channel?.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityAware=binding
    }

    override fun onDetachedFromActivity() {
        activityAware=null
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }
}