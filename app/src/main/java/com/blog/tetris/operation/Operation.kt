package com.blog.tetris.operation

import android.util.Log
import java.lang.Exception

/**
 * 操作类，包含地图、游戏的主运行逻辑、操作入口
 */
object Operation {

    //地图
    //0表示可操作空间占位（默认值）
    //1表示已固定方块占位
    //2表示正在下落方块占位
    val gameMap = Array(20) { Array(10) { 0 } }

    //方块下落速度
    //初始默认值750毫秒（0.75s）/格
    var speed = 750L

    var isInGame = false

    fun getMapArr(): Array<Array<Int>> {
//        gameMap[0][1] = 1
//        gameMap[1][2] = 1
//        gameMap[2][3] = 2
//        gameMap[2][4] = 3
//        gameMap[3][1] = 4
//        gameMap[3][2] = 5
//        gameMap[4][0] = 6
//        gameMap[4][1] = 7
//        gameMap[4][4] = 7
        return gameMap
    }

    /**
     * 开始游戏
     */
    fun startGame() {

        if (isInGame) {
            return
        }

        isInGame = true

        Thread {

            while (isInGame) {


                Thread.sleep(speed)

                val downArr = mutableListOf<Coordinate>()

                //扫描要下移的坐标，最后在统一交给一个方法处理
                for (i in gameMap.indices) {
                    for (j in gameMap[i].indices) {

                        val coordinate = gameMap[i][j]

                        if (coordinate in 11..17) {
                            downArr.add(Coordinate(i, j, coordinate))
                        }
                    }
                }
                val downBox = downBox(downArr)
                //是否无法继续下移
                if (!downBox) {
                    //生成一个新的模块
                    downArr.clear()
                    createTopBox()
                }
                changeListener?.onChange()
            }

        }.start()
    }

    /**
     * 方块下落
     */
    private fun downBox(downArr: MutableList<Coordinate>): Boolean {

        if (downArr.size == 0) {
            //无法继续下移
            return false
        }

        Log.d("mylog-uuyf", "down")
        for (i in downArr.size - 1 downTo 0) {
            val coordinate = downArr[i]

            //判断当前是否能继续下移，无法下移则固定模块，反之继续下移


            if (coordinate.x + 1 >= 20) {
                //无法继续下移,固定模块
                for (k in 0 until downArr.size) {
                    val coordinateTemp = downArr[k]
                    gameMap[coordinateTemp.x][coordinateTemp.y] = coordinateTemp.value - 10
                }
                return false
            } else {
                //继续下移
                gameMap[coordinate.x + 1][coordinate.y] = coordinate.value
                gameMap[coordinate.x][coordinate.y] = 0
            }
        }
        return true
    }

    /**
     * 在顶部创建一个新的模块
     */
    private fun createTopBox() {

        //随机生成一个类型模块
        val boxType = (1..1).random()

        //随机生成旋转度数
        val rotateType = (1..4).random()

        when (boxType) {
            1 -> {
                gameMap[0][4] = 11
                gameMap[0][5] = 11
                gameMap[1][4] = 11
                gameMap[1][5] = 11
            }

            2 -> {

            }
        }

    }


    var changeListener: ChangeListener? = null

    interface ChangeListener {
        fun onChange()
    }

    /**
     * 坐标实体类
     */
    data class Coordinate(
        var x: Int,
        var y: Int,
        var value: Int
    )

}