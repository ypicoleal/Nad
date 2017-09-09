package com.dranilsaarias.nad

import android.content.Context
import android.graphics.Canvas
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.PopupMenu
import android.util.AttributeSet
import android.view.Menu

class ClickToSelectEditText<T : Listable> : AppCompatEditText {

    internal lateinit var mItems: List<T>
    internal var mHint: CharSequence

    internal lateinit var onItemSelectedListener: OnItemSelectedListener<T>

    constructor(context: Context) : super(context) {

        mHint = hint
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        mHint = hint
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        mHint = hint
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        isFocusable = false
        isClickable = true
        isLongClickable = false
    }

    fun setItems(items: List<T>) {
        this.mItems = items

        configureOnClickListener()
    }

    private fun configureOnClickListener() {
        val popup = PopupMenu(this.context, this)
        val menu = popup.menu
        for (i in mItems.indices) {
            menu.add(Menu.NONE, Menu.NONE, i + 1, mItems[i].label)
        }

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            if (item.order > 0) {
                val selectedIndex = item.order - 1
                setText(item.title)
                onItemSelectedListener.onItemSelectedListener(mItems[selectedIndex], selectedIndex)
                return@OnMenuItemClickListener true
            }
            false
        })

        setOnClickListener { popup.show() }
    }

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener<T>) {
        this.onItemSelectedListener = onItemSelectedListener
    }

    interface OnItemSelectedListener<T> {
        fun onItemSelectedListener(item: T, selectedIndex: Int)
    }
}