package com.blog.tetris.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.blog.tetris.operation.Operation

class GameView : View {

    //方块大小
    private val boxSize = 80F

    //方块之间的间隔
    private val boxCrevice = 2F

    private var mapPaint: Paint = Paint()
    private var boxPaint: Paint = Paint()

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

                    boxPaint.color = Color.parseColor("#FF0000")
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

                    boxPaint.color = Color.parseColor("#FEFF02")
                    canvas?.drawPath(path1, boxPaint)
                    boxPaint.color = Color.parseColor("#00FF00")
                    canvas?.drawPath(path2, boxPaint)
                    boxPaint.color = Color.parseColor("#00FBFA")
                    canvas?.drawPath(path3, boxPaint)
                    boxPaint.color = Color.parseColor("#0302FE")
                    canvas?.drawPath(path4, boxPaint)
                }
            }
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