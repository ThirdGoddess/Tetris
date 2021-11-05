package com.blog.tetris.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.blog.tetris.operation.Operation

class GameView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    //方块大小
    val boxSize = 70F

    //方块边框大小
    private val edge = boxSize / 4F

    //地图绘制
    private var mapPaint: Paint = Paint()

    //方块绘制
    private var boxPaint: Paint = Paint()

    //预测方块绘制
    private var forecastPaint: Paint = Paint()

    //分数绘制
    private var scorePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var leftMargin = 0
    private var topMargin = 5 * boxSize

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

    init {
        scorePaint.color = Color.parseColor("#FFFFFF")
        mapPaint.color = Color.parseColor("#000000")
        scorePaint.textSize = boxSize / 1.8F
        boxPaint.isAntiAlias = true
        forecastPaint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {

        canvas?.let {

            //算出地图边界距离屏幕左侧距离（游戏地图放置在View中间）
            leftMargin = width / 2 - boxSize.toInt() * 5

            //绘制地图
            drawMap(it)

            //绘制方块
            drawBoxByArr(it)

            //绘制分数
            drawScore(it)

            //绘制预测
            drawForecast(it)
        }
    }

    /**
     * 绘制分数
     */
    private fun drawScore(canvas: Canvas) {

        //绘制标题
        canvas.drawText(
            "得分",
            leftMargin.toFloat() + boxSize / 2,
            scorePaint.textSize + boxSize / 2,
            scorePaint
        )

        //绘制得分
        canvas.drawText(
            Operation.score.toString(),
            leftMargin.toFloat() + boxSize / 2,
            scorePaint.textSize + boxSize / 2 + boxSize,
            scorePaint
        )
    }

    /**
     * 绘制预测
     */
    private fun drawForecast(canvas: Canvas) {

        //绘制标题
        canvas.drawText(
            "下一个",
            width / 2F,
            scorePaint.textSize + boxSize / 2,
            scorePaint
        )

        //绘制方块
        val forecastMap = Array(2) { Array(4) { 0 } }

        when (Operation.forecastType) {
            1 -> {
                forecastMap[0][0] = 1
                forecastMap[0][1] = 1
                forecastMap[1][0] = 1
                forecastMap[1][1] = 1
            }
            2 -> {
                forecastMap[0][2] = 2
                forecastMap[1][0] = 2
                forecastMap[1][1] = 2
                forecastMap[1][2] = 2
            }
            3 -> {
                forecastMap[0][0] = 3
                forecastMap[0][1] = 3
                forecastMap[1][1] = 3
                forecastMap[1][2] = 3
            }
            4 -> {
                forecastMap[0][1] = 4
                forecastMap[0][2] = 4
                forecastMap[1][0] = 4
                forecastMap[1][1] = 4
            }
            5 -> {
                forecastMap[0][0] = 5
                forecastMap[1][0] = 5
                forecastMap[1][1] = 5
                forecastMap[1][2] = 5
            }
            6 -> {
                forecastMap[0][1] = 6
                forecastMap[1][0] = 6
                forecastMap[1][1] = 6
                forecastMap[1][2] = 6
            }
            7 -> {
                forecastMap[0][0] = 7
                forecastMap[0][1] = 7
                forecastMap[0][2] = 7
                forecastMap[0][3] = 7
            }
        }

        //绘制预测方块
        val boxColor = getBoxColor(Operation.forecastType)

        for (i in forecastMap.indices) {
            for (j in forecastMap[i].indices) {
                if (forecastMap[i][j] > 0) {

                    val left = width / 2F + boxSize * j
                    val top = i * boxSize + boxSize * 1.5F
                    val right = width / 2F + boxSize * j + boxSize
                    val bottom = (i + 1) * boxSize + boxSize * 1.5F

                    //绘制方块
                    forecastPaint.color = boxColor[0]
                    canvas.drawRect(left, top, right, bottom, forecastPaint)

                    //绘制单个模块四周梯形
                    val path1 = Path()
                    path1.moveTo(left, bottom)
                    path1.lineTo(left, bottom - boxSize)
                    path1.lineTo(left + edge, bottom - boxSize + edge)
                    path1.lineTo(left + edge, bottom - edge)
                    path1.close()

                    val path2 = Path()
                    path2.moveTo(left + edge, top + edge)
                    path2.lineTo(left, top)
                    path2.lineTo(right, top)
                    path2.lineTo(right - edge, top + edge)
                    path2.close()

                    val path3 = Path()
                    path3.moveTo(right - edge, bottom - edge)
                    path3.lineTo(right - edge, top + edge)
                    path3.lineTo(right, top)
                    path3.lineTo(right, bottom)
                    path3.close()

                    val path4 = Path()
                    path4.moveTo(left, bottom)
                    path4.lineTo(left + edge, bottom - edge)
                    path4.lineTo(right - edge, bottom - edge)
                    path4.lineTo(right, bottom)
                    path4.close()

                    boxPaint.color = boxColor[1]
                    canvas.drawPath(path1, boxPaint)
                    boxPaint.color = boxColor[2]
                    canvas.drawPath(path2, boxPaint)
                    boxPaint.color = boxColor[3]
                    canvas.drawPath(path3, boxPaint)
                    boxPaint.color = boxColor[4]
                    canvas.drawPath(path4, boxPaint)
                }
            }
        }

    }


    /**
     * 绘制地图
     */
    private fun drawMap(canvas: Canvas) {

        //绘制地图
        for (i in 0 until 20) for (j in 0 until 10) canvas.drawRect(
            j * boxSize + leftMargin,
            i * boxSize + topMargin,
            (j + 1) * boxSize + leftMargin,
            (i + 1) * boxSize + topMargin,
            mapPaint
        )

        //绘制顶部状态图
        for (i in 0 until 4) for (j in 0 until 10) canvas.drawRect(
            j * boxSize + leftMargin,
            i * boxSize,
            (j + 1) * boxSize + leftMargin,
            (i + 1) * boxSize,
            mapPaint
        )

    }

    /**
     * 根据数组绘制图形
     */
    private fun drawBoxByArr(canvas: Canvas) {

        val mapArr = Operation.gameMap
        for (i in mapArr.indices) {

            for (j in mapArr[i].indices) {
                if (mapArr[i][j] > 0) {

                    boxPaint.color = getBoxColor(mapArr[i][j])[0]

                    val left = j * boxSize + leftMargin
                    val top = i * boxSize + topMargin
                    val right = (j + 1) * boxSize + leftMargin
                    val bottom = (i + 1) * boxSize + topMargin
                    canvas.drawRect(left, top, right, bottom, boxPaint)

                    //绘制单个模块四周梯形
                    val path1 = Path()
                    path1.moveTo(left, top)
                    path1.lineTo(left + edge, i * boxSize + edge + topMargin)
                    path1.lineTo(left + edge, bottom - edge)
                    path1.lineTo(left, bottom)
                    path1.close()

                    val path2 = Path()
                    path2.moveTo(left, top)
                    path2.lineTo(left + boxSize, top)
                    path2.lineTo(left + boxSize - edge, top + edge)
                    path2.lineTo(left + edge, top + edge)
                    path2.close()

                    val path3 = Path()
                    path3.moveTo(left + boxSize - edge, top + edge)
                    path3.lineTo(left + boxSize, top)
                    path3.lineTo(left + boxSize, bottom)
                    path3.lineTo(left + boxSize - edge, bottom - edge)
                    path3.close()

                    val path4 = Path()
                    path4.moveTo(left, bottom)
                    path4.lineTo(left + edge, bottom - edge)
                    path4.lineTo(left + boxSize - edge, bottom - edge)
                    path4.lineTo(left + boxSize, bottom)
                    path4.close()

                    boxPaint.color = getBoxColor(mapArr[i][j])[1]
                    canvas.drawPath(path1, boxPaint)
                    boxPaint.color = getBoxColor(mapArr[i][j])[2]
                    canvas.drawPath(path2, boxPaint)
                    boxPaint.color = getBoxColor(mapArr[i][j])[3]
                    canvas.drawPath(path3, boxPaint)
                    boxPaint.color = getBoxColor(mapArr[i][j])[4]
                    canvas.drawPath(path4, boxPaint)
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


}