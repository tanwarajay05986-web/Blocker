                p.shader = LinearGradient(
                    x(0.1f), y(0.95f), x(0.9f), y(0.05f),
                    intArrayOf(0xFFFFC107.toInt(), 0xFFF5365C.toInt(), 0xFF8A3AB9.toInt()),
                    null, Shader.TileMode.CLAMP
                )
                rf.set(x(0.04f), y(0.04f), x(0.96f), y(0.96f))
                c.drawRoundRect(rf, 0.26f * s, 0.26f * s, p)
                p.shader = null
                p.color = 0xFFFFFFFF.toInt()
                stroke(0.07f)
                rf.set(x(0.22f), y(0.22f), x(0.78f), y(0.78f))
                c.drawRoundRect(rf, 0.18f * s, 0.18f * s, p)
                c.drawCircle(x(0.5f), y(0.5f), 0.15f * s, p)
                fill()
                c.drawCircle(x(0.68f), y(0.32f), 0.04f * s, p)
            }
            G.SNAP -> {
                p.color = 0xFFFFD21F.toInt()
                rf.set(x(0.04f), y(0.04f), x(0.96f), y(0.96f))
                c.drawRoundRect(rf, 0.22f * s, 0.22f * s, p)
                p.color = 0xFFFFFFFF.toInt()
                c.drawCircle(x(0.5f), y(0.42f), 0.17f * s, p)
                rf.set(x(0.33f), y(0.42f), x(0.67f), y(0.74f))
                c.drawRect(rf, p)
                c.drawCircle(x(0.38f), y(0.74f), 0.05f * s, p)
                c.drawCircle(x(0.5f), y(0.76f), 0.05f * s, p)
                c.drawCircle(x(0.62f), y(0.74f), 0.05f * s, p)
            }
            G.WA -> {
                p.color = 0xFF2DB742.toInt()
                c.drawCircle(x(0.5f), y(0.5f), 0.5f * s, p)
                p.color = 0xFFFFFFFF.toInt()
                stroke(0.06f)
                c.drawCircle(x(0.5f), y(0.48f), 0.26f * s, p)
                fill()
                path.reset()
                path.moveTo(x(0.27f), y(0.68f))
                path.lineTo(x(0.22f), y(0.84f))
                path.lineTo(x(0.4f), y(0.75f))
                path.close()
                c.drawPath(path, p)
                stroke(0.07f)
                c.drawLine(x(0.43f), y(0.41f), x(0.57f), y(0.55f), p)
            }
            G.ADULT -> {
                p.color = C.coral
                stroke(0.07f)
                c.drawCircle(x(0.5f), y(0.5f), 0.42f * s, p)
                fill()
                p.textAlign = Paint.Align.CENTER
                p.typeface = Typeface.DEFAULT_BOLD
                p.textSize = 0.34f * s
                c.drawText("18+", x(0.5f), y(0.5f) + 0.12f * s, p)
            }
            G.SHIELD -> {
                stroke(0.07f)
                path.reset()
                path.moveTo(x(0.5f), y(0.1f))
                path.lineTo(x(0.85f), y(0.24f))
                path.lineTo(x(0.85f), y(0.5f))
                path.cubicTo(x(0.85f), y(0.72f), x(0.7f), y(0.84f), x(0.5f), y(0.92f))
                path.cubicTo(x(0.3f), y(0.84f), x(0.15f), y(0.72f), x(0.15f), y(0.5f))
                path.lineTo(x(0.15f), y(0.24f))
                path.close()
                c.drawPath(path, p)
                path.reset()
                path.moveTo(x(0.34f), y(0.5f))
                path.lineTo(x(0.46f), y(0.62f))
                path.lineTo(x(0.68f), y(0.38f))
                c.drawPath(path, p)
            }
            G.MONITOR -> {
                stroke(0.07f)
                rf.set(x(0.1f), y(0.16f), x(0.9f), y(0.7f))
                c.drawRoundRect(rf, 0.08f * s, 0.08f * s, p)
                c.drawLine(x(0.5f), y(0.7f), x(0.5f), y(0.84f), p)
                c.drawLine(x(0.3f), y(0.86f), x(0.7f), y(0.86f), p)
            }
            G.ACCESS -> {
                p.color = 0xFF3DB571.toInt()
                c.drawCircle(x(0.5f), y(0.12f), 0.1f * s, p)
                rf.set(x(0.04f), y(0.26f), x(0.96f), y(0.35f))
                c.drawRect(rf, p)
                rf.set(x(0.38f), y(0.35f), x(0.62f), y(0.64f))
                c.drawRect(rf, p)
                rf.set(x(0.38f), y(0.64f), x(0.48f), y(0.96f))
                c.drawRect(rf, p)
                rf.set(x(0.52f), y(0.64f), x(0.62f), y(0.96f))
                c.drawRect(rf, p)
            }
            G.CHECK -> {
                stroke(0.11f)
                path.reset()
                path.moveTo(x(0.2f), y(0.52f))
                path.lineTo(x(0.42f), y(0.72f))
                path.lineTo(x(0.8f), y(0.3f))
                c.drawPath(path, p)
            }
            G.CHEV -> {
                stroke(0.09f)
                path.reset()
                path.moveTo(x(0.36f), y(0.2f))
                path.lineTo(x(0.68f), y(0.5f))
                path.lineTo(x(0.36f), y(0.8f))
                c.drawPath(path, p)
            }
        }
    }
}

// the dark title tab of a section, its right side is cut slanted
class TagShape(color: Int) : Drawable() {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    init {
        p.color = color
        p.style = Paint.Style.FILL
    }

    override fun draw(c: Canvas) {
        val b = bounds
        val l = b.left.toFloat()
        val t = b.top.toFloat()
        val r = b.right.toFloat()
        val bt = b.bottom.toFloat()
        val h = bt - t
        val rad = h * 0.35f
        val cut = h * 0.6f
        path.reset()
        path.moveTo(l, bt)
        path.lineTo(l, t + rad)
        path.quadTo(l, t, l + rad, t)
        path.lineTo(r, t)
        path.lineTo(r - cut, bt)
        path.close()
        c.drawPath(path, p)
    }

    override fun setAlpha(alpha: Int) {
        p.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        p.colorFilter = cf
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

// the on / off switch
class Toggle(ctx: Context) : View(ctx) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rf = RectF()

    var on = false
        set(v) {
            field = v
            invalidate()
        }

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        setMeasuredDimension(context.dp(54), context.dp(32))
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val sw = context.dp(2).toFloat()
        p.style = Paint.Style.FILL
        if (on) {
            p.color = C.green
            rf.set(0f, 0f, w, h)
            c.drawRoundRect(rf, h / 2f, h / 2f, p)
            p.color = 0xFFFFFFFF.toInt()
            c.drawCircle(w - h / 2f, h / 2f, h * 0.38f, p)
        } else {
            p.color = C.offFill
            rf.set(sw / 2f, sw / 2f, w - sw / 2f, h - sw / 2f)
            c.drawRoundRect(rf, h / 2f, h / 2f, p)
            p.style = Paint.Style.STROKE
            p.strokeWidth = sw
            p.color = C.offLine
            c.drawRoundRect(rf, h / 2f, h / 2f, p)
            p.style = Paint.Style.FILL
            c.drawCircle(h / 2f, h / 2f, h * 0.22f, p)
        }
    }
}
