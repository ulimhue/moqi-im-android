package com.moqi.im.keyboard

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.moqi.im.theme.ThemePalette

object CandidateActionMenu {
    private var activePopup: PopupWindow? = null

    fun dismiss() {
        activePopup?.dismiss()
        activePopup = null
    }

    fun show(
        anchor: View,
        canResetFrequency: Boolean,
        onResetFrequency: () -> Unit,
        onAddQuickReply: () -> Unit
    ) {
        dismiss()
        val context = anchor.context
        val theme = ThemePalette.current(context)
        val density = context.resources.displayMetrics.density
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(theme.candidateBackgroundColor)
            elevation = 8f * density
            val padH = (12 * density).toInt()
            val padV = (6 * density).toInt()
            setPadding(padH, padV, padH, padV)
        }
        if (canResetFrequency) {
            container.addView(
                menuItem(context, "恢复默认词频", theme.textColor) {
                    dismiss()
                    onResetFrequency()
                }
            )
        }
        container.addView(
            menuItem(context, "加入快捷回复", theme.textColor) {
                dismiss()
                onAddQuickReply()
            }
        )

        val popup = PopupWindow(
            container,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            false
        ).apply {
            setBackgroundDrawable(ColorDrawable(theme.candidateBackgroundColor))
            isOutsideTouchable = true
            isTouchable = true
            elevation = 12f * density
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
            }
            setOnDismissListener { activePopup = null }
        }
        activePopup = popup

        container.measure(
            View.MeasureSpec.makeMeasureSpec(
                (anchor.width * 3).coerceAtLeast((160 * density).toInt()),
                View.MeasureSpec.AT_MOST
            ),
            View.MeasureSpec.UNSPECIFIED
        )
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val y = (location[1] - container.measuredHeight - (8 * density)).toInt().coerceAtLeast(0)
        popup.showAtLocation(anchor, Gravity.TOP or Gravity.START, location[0], y)
    }

    private fun menuItem(
        context: android.content.Context,
        label: String,
        textColor: Int,
        onClick: () -> Unit
    ): TextView {
        val density = context.resources.displayMetrics.density
        return TextView(context).apply {
            text = label
            setTextColor(textColor)
            textSize = 15f
            val padH = (14 * density).toInt()
            val padV = (10 * density).toInt()
            setPadding(padH, padV, padH, padV)
            setOnClickListener { onClick() }
        }
    }
}
