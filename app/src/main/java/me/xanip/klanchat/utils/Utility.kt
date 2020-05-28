package me.xanip.klanchat.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.Window
import android.view.WindowManager
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import me.xanip.klanchat.database.model.ChatHelper
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.math.roundToInt

object Utility {

    fun setStatusBarColor(activity: Activity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
        }
    }

    fun setLayoutFullScreen(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 21) {
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }

    fun View.slideUp(duration: Int = 500) {
        visibility = View.VISIBLE
        val animate = TranslateAnimation(0f, 0f, this.height.toFloat(), 0f)
        animate.duration = duration.toLong()
        animate.fillAfter = true
        this.startAnimation(animate)
    }

    fun View.slideDown(duration: Int = 500) {
        visibility = View.VISIBLE
        val animate = TranslateAnimation(0f, 0f, 0f, this.height.toFloat())
        animate.duration = duration.toLong()
        animate.fillAfter = true
        this.startAnimation(animate)
    }

    fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    val Int.dpToPx: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun pixelTodp(c: Context, pixel: Float): Float {
        val density = c.resources.displayMetrics.density
        return pixel / density
    }

    fun setMargins(
        view: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        if (view.layoutParams is MarginLayoutParams) {
            val p = view.layoutParams as MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId: Int = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun setProgressDialog(context: Context): AlertDialog {
        val ll = LinearLayout(context).apply {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.CENTER

            val progressBar = ProgressBar(context).apply {
                isIndeterminate = true
                setPadding(0, 0, 40, 0)
                layoutParams = params
            }

            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 40, 40, 40)
            gravity = Gravity.CENTER
            layoutParams = params

            addView(progressBar)
        }

        val builder = AlertDialog.Builder(context).apply {
            setCancelable(false)
            setView(ll)
        }

        return builder.create().apply {
            window?.let {
                val layoutParams = WindowManager.LayoutParams().apply {
                    copyFrom(it.attributes)
                    width = LinearLayout.LayoutParams.WRAP_CONTENT
                    height = LinearLayout.LayoutParams.WRAP_CONTENT
                }
                it.attributes = layoutParams
            }
        }
    }

    fun calculateNoOfColumns(
        context: Context,
        columnWidthDp: Float
    ): Int { // For example columnWidthdp=180
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        return (screenWidthDp / columnWidthDp + 0.5).toInt()
    }

    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })

        /*
        observeForever(object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
         */
    }

    fun getTimeMilliSec(timestamp: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        try {
            val date = format.parse(timestamp)
            return date?.time?:0
        }catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    fun getTimeClock(timestamp: String): String {
        var result = timestamp
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        try {
            val date = format.parse(timestamp)
            date?.let {
                val newFormat = SimpleDateFormat("HH.mm", Locale.US)
                result = newFormat.format(date)
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun getTimeClockHeadMessage(timestamp: String): String {
        var result = timestamp
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        try {
            val date = format.parse(timestamp)
            date?.let {
                val onDay = Calendar.getInstance()
                onDay.add(Calendar.DAY_OF_YEAR, 0)

                val yesterday = Calendar.getInstance()
                yesterday.add(Calendar.DAY_OF_YEAR, -1)

                val currentDay = Calendar.getInstance()
                currentDay.time = date

                result = if (yesterday.get(Calendar.YEAR) == currentDay.get(Calendar.YEAR)
                    && yesterday.get(Calendar.DAY_OF_YEAR) == currentDay.get(Calendar.DAY_OF_YEAR)) {
                    "Kemarin"
                }else {
                    if (onDay.get(Calendar.YEAR) == currentDay.get(Calendar.YEAR)
                        && onDay.get(Calendar.DAY_OF_YEAR) == currentDay.get(Calendar.DAY_OF_YEAR)) {
                        SimpleDateFormat("HH.mm", Locale.US).format(date)
                    }else {
                        SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date)
                    }
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun getDateOnly(timestamp: String): String {
        var result = timestamp
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        try {
            val date = format.parse(timestamp)
            date?.let {
                val newFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                result = newFormat.format(date)
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}