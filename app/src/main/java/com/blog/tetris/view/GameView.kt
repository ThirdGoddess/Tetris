package com.blog.tetris.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.blog.tetris.operation.Operation

class GameView : View {

    //方块大小
    val boxSize = 80F

    //方块之间的间隔
    private val boxCrevice = 2F

    private var mapPaint: Paint = Paint()
    private var boxPaint: Paint = Paint()


    //方块颜色
    private var color10 = Color.parseColor("#0302FF")
    private var color11 = Color.parseColor("#3437F9")
    private var color12 = Color.parseColor("#6568FC")
    private var color13 = Color.parseColor("#00089C")
    private var color14 = Color.parseColor("#00027B")

    private var color20 = Color.parseColor("#FEFF04")
    private var color21 = Color.parseColor("#FDFE3E")
    private var color22 = Color.parseColor("#FCFD65")
    private var color23 = Color.parseColor("#ACA706")
    private var color24 = Color.parseColor("#717804")

    private var color30 = Color.parseColor("#FB0101")
    private var color31 = Color.parseColor("#F33A36")
    private var color32 = Color.parseColor("#F66A64")
    private var color33 = Color.parseColor("#A80400")
    private var color34 = Color.parseColor("#7E0003")

    private var color40 = Color.parseColor("#06FD06")
    private var color41 = Color.parseColor("#37F936")
    private var color42 = Color.parseColor("#68FD67")
    private var color43 = Color.parseColor("#0AA109")
    private var color44 = Color.parseColor("#037407")

    private var color50 = Color.parseColor("#FF7F04")
    private var color51 = Color.parseColor("#F89A38")
    private var color52 = Color.parseColor("#F8B56A")
    private var color53 = Color.parseColor("#A75304")
    private var color54 = Color.parseColor("#723709")

    private var color60 = Color.parseColor("#F505F8")
    private var color61 = Color.parseColor("#FB36FB")
    private var color62 = Color.parseColor("#FF66F9")
    private var color63 = Color.parseColor("#A801A7")
    private var color64 = Color.parseColor("#780378")

    private var color70 = Color.parseColor("#01FFFD")
    private var color71 = Color.parseColor("#37FDFE")
    private var color72 = Color.parseColor("#69FAF9")
    private var color73 = Color.parseColor("#08A5AA")
    private var color74 = Color.parseColor("#01746F")

    constructor(context: Context?) : super(context) {

    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }

    override fun onDraw(canvas: Canvas?) {
        mapPaint.color = Color.parseColor("#000000")
        boxPaint.isAntiAlias = true
        drawMap(canvas)
        drawBoxByArr(canvas)
    }


    /**
     * 绘制地图
     */
    private fun drawMap(canvas: Canvas?) {
        for (i in 0 until 20) {
            for (j in 0 until 10) {
                canvas?.drawRect(
                    j * boxSize + 200,
                    i * boxSize,
                    (j + 1) * boxSize + 200,
                    (i + 1) * boxSize,
                    mapPaint
                )
            }
        }
    }

    /**
     * 根据数组绘制图形
     */
    private fun drawBoxByArr(canvas: Canvas?) {

        val edge = boxSize / 4F

        val mapArr = Operation.gameMap
        for (i in mapArr.indices) {

            for (j in mapArr[i].indices) {
                if (mapArr[i][j] > 0) {

                    boxPaint.color = getBoxColor(mapArr[i][j])[0]
                    canvas?.drawRect(
                        j * boxSize + 200,
                        i * boxSize,
                        (j + 1) * boxSize + 200,
                        (i + 1) * boxSize,
                        boxPaint
                    )

                    //绘制单个模块四周梯形
                    val path1 = Path()
                    path1.moveTo(j * boxSize + 200, i * boxSize)
                    path1.lineTo(j * boxSize + 200 + edge, i * boxSize + edge)
                    path1.lineTo(
                        j * boxSize + 200 + edge,
                        (i + 1) * boxSize - edge
                    )
                    path1.lineTo(j * boxSize + 200, (i + 1) * boxSize)
                    path1.close()

                    val path2 = Path()
                    path2.moveTo(j * boxSize + 200, i * boxSize)
                    path2.lineTo(j * boxSize + 200 + boxSize, i * boxSize)
                    path2.lineTo(
                        j * boxSize + 200 + boxSize - edge,
                        i * boxSize + edge
                    )
                    path2.lineTo(j * boxSize + 200 + edge, i * boxSize + edge)
                    path2.close()

                    val path3 = Path()
                    path3.moveTo(j * boxSize + 200 + boxSize - edge, i * boxSize + edge)
                    path3.lineTo(j * boxSize + 200 + boxSize, i * boxSize)
                    path3.lineTo(j * boxSize + 200 + boxSize, (i + 1) * boxSize)
                    path3.lineTo(
                        j * boxSize + 200 + boxSize - edge,
                        (i + 1) * boxSize - edge
                    )
                    path3.close()

                    val path4 = Path()
                    path4.moveTo(j * boxSize + 200, (i + 1) * boxSize)
                    path4.lineTo(
                        j * boxSize + 200 + edge,
                        (i + 1) * boxSize - edge
                    )
                    path4.lineTo(
                        j * boxSize + 200 + boxSize - edge,
                        (i + 1) * boxSize - edge
                    )
                    path4.lineTo(j * boxSize + 200 + boxSize, (i + 1) * boxSize)
                    path4.close()

                    boxPaint.color = getBoxColor(mapArr[i][j])[1]
                    canvas?.drawPath(path1, boxPaint)
                    boxPaint.color = getBoxColor(mapArr[i][j])[2]
                    canvas?.drawPath(path2, boxPaint)
                    boxPaint.color = getBoxColor(mapArr[i][j])[3]
                    canvas?.drawPath(path3, boxPaint)
                    boxPaint.color = getBoxColor(mapArr[i][j])[4]
                    canvas?.drawPath(path4, boxPaint)
                }
            }
        }
    }

    private fun getBoxColor(type: Int): Array<Int> {
        return when (type) {
            1, 11 -> arrayOf(color10, color11, color12, color13, color14)
            2, 12 -> arrayOf(color20, color21, color22, color23, color24)
            3, 13 -> arrayOf(color30, color31, color32, color33, color34)
            4, 14 -> arrayOf(color40, color41, color42, color43, color44)
            5, 15 -> arrayOf(color50, color51, color52, color53, color54)
            6, 16 -> arrayOf(color60, color61, color62, color63, color64)
            7, 17 -> arrayOf(color70, color71, color72, color73, color74)
            else -> arrayOf(color10, color11, color12, color13, color14)
        }
    }

    /**
     * 刷新
     */
    fun refresh() {
        postInvalidate()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d("mylog-onDraw", "onMeasure")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


}