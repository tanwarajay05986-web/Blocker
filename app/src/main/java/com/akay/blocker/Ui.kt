package com.akay.blocker

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.Toast

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

fun Context.toast(m: String) {
    Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}

// a rounded box with an optional border
fun Context.shape(fill: Int, radius: Int, stroke: Int = 0, strokeDp: Int = 1): GradientDrawable {
    val g = GradientDrawable()
    g.setColor(fill)
    g.cornerRadius = dp(radius).toFloat()
    if (stroke != 0) g.setStroke(dp(strokeDp), stroke)
    return g
}

object C {
    val coral = 0xFFF96060.toInt()
    val bg = 0xFFF8F9FB.toInt()
    val card = 0xFFFFFFFF.toInt()
    val edge = 0xFFC9CBD3.toInt()
    val text = 0xFF1F2024.toInt()
    val sub = 0xFF8A8C94.toInt()
    val nav = 0xFFF3EDF7.toInt()
    val head = 0xFF5B5E6A.toInt()
    val green = 0xFF2FB34E.toInt()
    val offFill = 0xFFEAE4F2.toInt()
    val offLine = 0xFF7B7487.toInt()
    val icon = 0xFF5F6168.toInt()
    val divider = 0xFFE3E4E8.toInt()
}

object G {
    const val BLOCK = 0
    const val LIST = 1
    const val GEAR = 2
    const val INFO = 3
    const val TRASH = 4
    const val GLOBE = 5
    const val DLBOX = 6
    const val CHAT = 7
    const val TIMER = 8
    const val LINK = 9
    const val PENCIL = 10
    const val SEARCH = 11
    const val YT = 12
    const val IG = 13
    const val SNAP = 14
    const val WA = 15
    const val ADULT = 16
    const val SHIELD = 17
    const val MONITOR = 18
    const val ACCESS = 19
    const val CHECK = 20
    const val CHEV = 21
}

// every icon of the app is drawn here
class Glyph(ctx: Context, private val kind: Int, var color: Int) : View(ctx) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rf = RectF()

    fun tint(c: Int) {
        color = c
        invalidate()
    }

    override fun onDraw(c: Canvas) {
        val s = minOf(width, height).toFloat()
        val ox = (width - s) / 2f
        val oy = (height - s) / 2f
        fun x(v: Float): Float = ox + v * s
        fun y(v: Float): Float = oy + v * s
        fun stroke(w: Float) {
            p.style = Paint.Style.STROKE
            p.strokeWidth = w * s
            p.strokeCap = Paint.Cap.ROUND
            p.strokeJoin = Paint.Join.ROUND
        }
        fun fill() {
            p.style = Paint.Style.FILL
        }
        p.shader = null
        p.color = color
        fill()

        when (kind) {
            G.BLOCK -> {
                stroke(0.08f)
                c.drawCircle(x(0.5f), y(0.5f), 0.38f * s, p)
                c.drawLine(x(0.23f), y(0.23f), x(0.77f), y(0.77f), p)
            }
            G.LIST -> {
                stroke(0.08f)
                for (i in 0..2) {
                    val yy = y(0.24f + i * 0.26f)
                    c.drawLine(x(0.34f), yy, x(0.9f), yy, p)
                }
                fill()
                for (i in 0..2) c.drawCircle(x(0.14f), y(0.24f + i * 0.26f), 0.05f * s, p)
            }
            G.GEAR -> {
                c.save()
                c.translate(x(0.5f), y(0.5f))
                for (i in 0 until 8) {
                    rf.set(-0.08f * s, -0.46f * s, 0.08f * s, -0.28f * s)
                    c.drawRoundRect(rf, 0.03f * s, 0.03f * s, p)
                    c.rotate(45f)
                }
                c.restore()
                path.reset()
                path.fillType = Path.FillType.EVEN_ODD
                path.addCircle(x(0.5f), y(0.5f), 0.33f * s, Path.Direction.CW)
                path.addCircle(x(0.5f), y(0.5f), 0.13f * s, Path.Direction.CW)
                c.drawPath(path, p)
                path.fillType = Path.FillType.WINDING
            }
            G.INFO -> {
                stroke(0.07f)
                c.drawCircle(x(0.5f), y(0.5f), 0.4f * s, p)
                c.drawLine(x(0.5f), y(0.46f), x(0.5f), y(0.7f), p)
                fill()
                c.drawCircle(x(0.5f), y(0.31f), 0.05f * s, p)
            }
            G.TRASH -> {
                stroke(0.07f)
                c.drawLine(x(0.18f), y(0.26f), x(0.82f), y(0.26f), p)
                path.reset()
                path.moveTo(x(0.4f), y(0.26f))
                path.lineTo(x(0.4f), y(0.14f))
                path.lineTo(x(0.6f), y(0.14f))
                path.lineTo(x(0.6f), y(0.26f))
                c.drawPath(path, p)
                path.reset()
                path.moveTo(x(0.26f), y(0.3f))
                path.lineTo(x(0.31f), y(0.88f))
                path.lineTo(x(0.69f), y(0.88f))
                path.lineTo(x(0.74f), y(0.3f))
                c.drawPath(path, p)
                c.drawLine(x(0.42f), y(0.42f), x(0.42f), y(0.76f), p)
                c.drawLine(x(0.58f), y(0.42f), x(0.58f), y(0.76f), p)
            }
            G.GLOBE -> {
                stroke(0.06f)
                c.drawCircle(x(0.5f), y(0.5f), 0.4f * s, p)
                rf.set(x(0.32f), y(0.1f), x(0.68f), y(0.9f))
                c.drawOval(rf, p)
                c.drawLine(x(0.1f), y(0.5f), x(0.9f), y(0.5f), p)
                c.drawLine(x(0.17f), y(0.3f), x(0.83f), y(0.3f), p)
                c.drawLine(x(0.17f), y(0.7f), x(0.83f), y(0.7f), p)
            }
            G.DLBOX -> {
                stroke(0.07f)
                rf.set(x(0.14f), y(0.14f), x(0.86f), y(0.86f))
                c.drawRoundRect(rf, 0.12f * s, 0.12f * s, p)
                c.drawLine(x(0.5f), y(0.3f), x(0.5f), y(0.62f), p)
                path.reset()
                path.moveTo(x(0.37f), y(0.5f))
                path.lineTo(x(0.5f), y(0.64f))
                path.lineTo(x(0.63f), y(0.5f))
                c.drawPath(path, p)
            }
            G.CHAT -> {
                stroke(0.07f)
                rf.set(x(0.1f), y(0.14f), x(0.9f), y(0.68f))
                c.drawRoundRect(rf, 0.1f * s, 0.1f * s, p)
                path.reset()
                path.moveTo(x(0.3f), y(0.68f))
                path.lineTo(x(0.28f), y(0.88f))
                path.lineTo(x(0.5f), y(0.68f))
                c.drawPath(path, p)
                c.drawLine(x(0.28f), y(0.34f), x(0.72f), y(0.34f), p)
                c.drawLine(x(0.28f), y(0.5f), x(0.58f), y(0.5f), p)
            }
            G.TIMER -> {
                stroke(0.07f)
                c.drawCircle(x(0.5f), y(0.57f), 0.32f * s, p)
                c.drawLine(x(0.5f), y(0.57f), x(0.5f), y(0.38f), p)
                c.drawLine(x(0.5f), y(0.57f), x(0.64f), y(0.64f), p)
                c.drawLine(x(0.4f), y(0.11f), x(0.6f), y(0.11f), p)
                c.drawLine(x(0.5f), y(0.11f), x(0.5f), y(0.24f), p)
            }
            G.LINK -> {
                stroke(0.07f)
                c.save()
                c.rotate(-45f, x(0.5f), y(0.5f))
                rf.set(x(0.06f), y(0.4f), x(0.56f), y(0.6f))
                c.drawRoundRect(rf, 0.1f * s, 0.1f * s, p)
                rf.set(x(0.44f), y(0.4f), x(0.94f), y(0.6f))
                c.drawRoundRect(rf, 0.1f * s, 0.1f * s, p)
                c.restore()
            }
            G.PENCIL -> {
                stroke(0.075f)
                path.reset()
                path.moveTo(x(0.62f), y(0.18f))
                path.lineTo(x(0.82f), y(0.38f))
                path.lineTo(x(0.4f), y(0.8f))
                path.lineTo(x(0.16f), y(0.84f))
                path.lineTo(x(0.2f), y(0.6f))
                path.close()
                c.drawPath(path, p)
                c.drawLine(x(0.52f), y(0.28f), x(0.72f), y(0.48f), p)
            }
            G.SEARCH -> {
                p.color = 0xFFE5E1F8.toInt()
                c.drawCircle(x(0.5f), y(0.5f), 0.5f * s, p)
                p.color = 0xFF4B4B8C.toInt()
                stroke(0.07f)
                c.drawCircle(x(0.45f), y(0.45f), 0.16f * s, p)
                c.drawLine(x(0.57f), y(0.57f), x(0.7f), y(0.7f), p)
            }
            G.YT -> {
                p.color = 0xFFF04444.toInt()
                rf.set(x(0.04f), y(0.2f), x(0.96f), y(0.8f))
                c.drawRoundRect(rf, 0.12f * s, 0.12f * s, p)
                p.color = 0xFFFFFFFF.toInt()
                path.reset()
                path.moveTo(x(0.42f), y(0.36f))
                path.lineTo(x(0.64f), y(0.5f))
                path.lineTo(x(0.42f), y(0.64f))
                path.close()
                c.drawPath(path, p)
            }
            G.IG -> {
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
