            item.gravity = Gravity.CENTER_HORIZONTAL

            val pillBox = FrameLayout(this)
            if (sel) pillBox.background = shape(C.coral, 16)
            pillBox.addView(
                Glyph(this, icons[i], if (sel) WHITE else C.icon),
                FrameLayout.LayoutParams(dp(24), dp(24), Gravity.CENTER)
            )
            item.addView(pillBox, LinearLayout.LayoutParams(dp(64), dp(32)))

            val tv = TextView(this)
            tv.text = names[i]
            tv.textSize = 14f
            tv.gravity = Gravity.CENTER
            tv.setTextColor(if (sel) C.coral else C.head)
            if (sel) tv.typeface = Typeface.DEFAULT_BOLD
            item.addView(tv, LinearLayout.LayoutParams(WC, WC).also { it.topMargin = dp(4) })

            item.setOnClickListener {
                tab = i
                render()
            }
            bar.addView(item, LinearLayout.LayoutParams(0, WC, 1f))
        }
        return bar
    }

    private fun say(title: String, msg: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun ask(title: String, cur: String, hint: String, type: Int, save: (String) -> Unit) {
        val et = EditText(this)
        et.inputType = type
        et.hint = hint
        et.setText(cur)
        et.setSelection(et.text.length)
        val box = FrameLayout(this)
        box.setPadding(dp(22), dp(10), dp(22), 0)
        box.addView(et, FrameLayout.LayoutParams(MP, WC))
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(box)
            .setPositiveButton("Save") { _, _ ->
                save(et.text.toString().trim())
                render()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
