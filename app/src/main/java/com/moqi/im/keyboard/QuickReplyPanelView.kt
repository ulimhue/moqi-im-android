package com.moqi.im.keyboard

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.moqi.im.theme.ThemePalette

class QuickReplyPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    interface Callback {
        fun onBack()
        fun onReplySelected(text: String)
        fun onReplyDelete(index: Int)
    }

    var callback: Callback? = null

    private val density = resources.displayMetrics.density
    private val scrollView = ScrollView(context)
    private val content = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), dp(8), dp(12), dp(12))
    }

    init {
        applyThemeBackground()
        val header = createHeader()
        addView(header, LayoutParams(LayoutParams.MATCH_PARENT, dp(48)))
        scrollView.addView(
            content,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )
        addView(
            scrollView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                topMargin = dp(48)
            }
        )
        render(emptyList())
    }

    fun render(items: List<String>) {
        applyThemeBackground()
        content.removeAllViews()
        if (items.isEmpty()) {
            content.addView(createEmptyHint())
            return
        }
        items.forEachIndexed { index, text ->
            content.addView(createReplyItem(text, index))
        }
    }

    private fun createHeader(): View {
        val theme = ThemePalette.current(context)
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), 0, dp(8), 0)
            setBackgroundColor(theme.candidateBackgroundColor)
        }
        row.addView(
            TextView(context).apply {
                text = "←"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                setTextColor(theme.textColor)
                setPadding(dp(8), dp(8), dp(8), dp(8))
                setOnClickListener { callback?.onBack() }
            },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        )
        row.addView(
            TextView(context).apply {
                text = "快捷回复"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(theme.textColor)
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        return row
    }

    private fun createEmptyHint(): View {
        val theme = ThemePalette.current(context)
        return TextView(context).apply {
            text = "长按候选词可加入快捷回复"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(adjustAlpha(theme.textColor, 0.55f))
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(32), dp(16), dp(32))
        }
    }

    private fun createReplyItem(text: String, index: Int): View {
        val theme = ThemePalette.current(context)
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val vertical = dp(6)
            setPadding(dp(12), vertical, dp(8), vertical)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = dp(8)
            layoutParams = lp
            background = roundedBackground(
                fillColor = if (isDarkMode()) 0xFF2A3138.toInt() else 0xFFF3F5F8.toInt(),
                strokeColor = if (isDarkMode()) 0xFF3E4852.toInt() else 0xFFD7DCE2.toInt()
            )
            setOnClickListener { callback?.onReplySelected(text) }
        }
        val body = TextView(context).apply {
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (text.length <= 12) 16f else 14f)
            setTextColor(theme.textColor)
            maxLines = if (text.length <= 24) 2 else 4
            ellipsize = TextUtils.TruncateAt.END
            setLineSpacing(dp(2).toFloat(), 1f)
        }
        container.addView(
            body,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        container.addView(
            TextView(context).apply {
                this.text = "删除"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(0xFFE57373.toInt())
                setPadding(dp(8), dp(4), dp(4), dp(4))
                setOnClickListener {
                    callback?.onReplyDelete(index)
                }
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        return container
    }

    private fun applyThemeBackground() {
        setBackgroundColor(ThemePalette.current(context).candidateBackgroundColor)
    }

    private fun isDarkMode(): Boolean =
        (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

    private fun roundedBackground(fillColor: Int, strokeColor: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = dp(10).toFloat()
            setColor(fillColor)
            setStroke(dp(1), strokeColor)
        }
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = ((color ushr 24) and 0xFF) * factor
        return (color and 0x00FFFFFF) or ((alpha.toInt().coerceIn(0, 255)) shl 24)
    }

    private fun dp(value: Int): Int = (value * density).toInt()
}
