package com.grio.lib.features.editor.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.grio.lib.R

class ToolOptions @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    init {
        LayoutInflater.from(context).inflate(R.layout.v_tool_options, this, true)
        orientation = VERTICAL
    }
}