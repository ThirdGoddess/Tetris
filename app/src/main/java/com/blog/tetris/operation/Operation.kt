package com.blog.tetris.operation

import java.lang.Thread
import kotlin.math.pow

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

    //当前旋转度，默认是1
    private var rotate = 1

    //游戏分数
    var score = 0L

    //要生成的方块
    var forecastType = 0

    //已经生成的方块个数,根据此变量提升方块下落速度
    var boxNum = 0

    //游戏线程
    private lateinit var thread: Thread

    /**
     * 开始游戏
     */
    fun startGame() {

        if (isInGame) return

        isInGame = true

        thread = Thread {

            while (isInGame) {

                Thread.sleep(speed)

                val downBox = downBox()
                //是否无法继续下移
                if (!downBox) {

                    //固定模块
                    fixedMoveBox()

                    //消除方块
                    eliminate()

                    //生成一个新的模块
                    createTopBox()

                }
                changeListener?.onChange()
            }
        }

        thread.start()

    }

    //消除行数量
    var fullLine = 0

    /**
     * 消除方块
     * 从下到上递归扫描方块，一行一行消除
     */
    private fun eliminate(): Int {

        for (i in gameMap.size - 1 downTo 0) {

            //当前行是否满足
            var isFull = true

            for (j in gameMap[i].indices) if (gameMap[i][j] == 0) isFull = false

            if (isFull) {

                fullLine++

                for (j in gameMap[i].indices) {
                    gameMap[i][j] = 0
                }

                for (k in i downTo 1) for (n in gameMap[k].indices)
                    gameMap[k][n] = gameMap[k - 1][n]

                eliminate()
            }
        }

        if (fullLine > 0) {

            //根据消除行算出分数
            score += (2.toDouble().pow((fullLine - 1).toDouble())).toLong() * 100

            changeListener?.onChange()
            Thread.sleep(200)
            fullLine = 0
        }

        return fullLine
    }

    /**
     * 变形-正在下落的模块顺时针旋转90度
     */
    fun deformation() {

        //扫描正在下落的方块
        val boxArr = mutableListOf<Coordinate>()

        /**
         * 重置置为空
         */
        fun reset() {
            for (coordinate in boxArr) gameMap[coordinate.x][coordinate.y] = 0
        }

        //当前下落模块左上角坐标
        var boxMinX = 20
        var boxMinY = 10

        for (i in gameMap.indices) for (j in gameMap[i].indices) if (gameMap[i][j] in 12..17) {
            boxArr.add(Coordinate(i, j, gameMap[i][j]))
            if (i < boxMinX) boxMinX = i
            if (j < boxMinY) boxMinY = j
        }

        if (0 == boxArr.size) return

        val boxType = boxArr[0].value

        if (0 != boxArr.size) {

            //判断是否符合旋转标准
            //有足够的空间变形（不触碰左、右、下边界，不触碰其他固定方块）

            //取出模块类型
            when (boxType) {

                12 -> {
                    when (rotate) {
                        1 -> {
                            //判断是否符合旋转条件
                            if (boxMinX + 2 < 20
                                && boxMinY + 1 < 10
                                && gameMap[boxMinX][boxMinY] !in 1..7
                                && gameMap[boxMinX][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX + 2][boxMinY + 1] !in 1..7
                            ) {
                                reset()
                                gameMap[boxMinX][boxMinY] = 12
                                gameMap[boxMinX][boxMinY + 1] = 12
                                gameMap[boxMinX + 1][boxMinY + 1] = 12
                                gameMap[boxMinX + 2][boxMinY + 1] = 12
                                rotate = 2
                            }
                        }
                        2 -> {
                            if (boxMinX + 1 < 20
                                && boxMinY + 2 < 10
                                && gameMap[boxMinX][boxMinY] !in 1..7
                                && gameMap[boxMinX][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX][boxMinY + 2] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY] !in 1..7
                            ) {
                                reset()
                                gameMap[boxMinX][boxMinY] = 12
                                gameMap[boxMinX][boxMinY + 1] = 12
                                gameMap[boxMinX][boxMinY + 2] = 12
                                gameMap[boxMinX + 1][boxMinY] = 12
                                rotate = 3
                            }
                        }
                        3 -> {
                            if (boxMinX + 2 < 20
                                && boxMinY + 1 < 10
                                && gameMap[boxMinX][boxMinY] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY] !in 1..7
                                && gameMap[boxMinX + 2][boxMinY] !in 1..7
                                && gameMap[boxMinX + 2][boxMinY + 1] !in 1..7
                            ) {
                                reset()
                                gameMap[boxMinX][boxMinY] = 12
                                gameMap[boxMinX + 1][boxMinY] = 12
                                gameMap[boxMinX + 2][boxMinY] = 12
                                gameMap[boxMinX + 2][boxMinY + 1] = 12
                                rotate = 4
                            }
                        }
                        4 -> {
                            if (boxMinX + 1 < 20
                                && boxMinY + 2 < 10
                                && gameMap[boxMinX][boxMinY + 2] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY + 2] !in 1..7
                            ) {
                                reset()
                                gameMap[boxMinX][boxMinY + 2] = 12
                                gameMap[boxMinX + 1][boxMinY] = 12
                                gameMap[boxMinX + 1][boxMinY + 1] = 12
                                gameMap[boxMinX + 1][boxMinY + 2] = 12
                                rotate = 1
                            }
                        }
                    }
                }

                13 -> {
                    when (rotate) {
                        1 -> {
                            if (boxMinX + 2 < 20
                                && boxMinY + 1 < 10
                                && gameMap[boxMinX][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX + 2][boxMinY] !in 1..7
                            ) {
                                reset()
                                gameMap[boxMinX][boxMinY + 1] = 13
                                gameMap[boxMinX + 1][boxMinY] = 13
                                gameMap[boxMinX + 1][boxMinY + 1] = 13
                                gameMap[boxMinX + 2][boxMinY] = 13
                                rotate = 2
                            }
                        }
                        2 -> {
                            if (
                                boxMinX + 1 < 20
                                && boxMinY + 2 < 10
                                && gameMap[boxMinX][boxMinY] !in 1..7
                                && gameMap[boxMinX][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY + 2] !in 1..7
                            ) {
                                reset()
                                gameMap[boxMinX][boxMinY] = 13
                                gameMap[boxMinX][boxMinY + 1] = 13
                                gameMap[boxMinX + 1][boxMinY + 1] = 13
                                gameMap[boxMinX + 1][boxMinY + 2] = 13
                                rotate = 1
                            }
                        }
                    }
                }

                14 -> {
                    when (rotate) {
                        1 -> {

                            if (boxMinX + 2 < 20
                                && boxMinY + 1 < 10
                                && gameMap[boxMinX][boxMinY] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX + 2][boxMinY + 1] !in 1..7
                            ) {
                                reset()
                                gameMap[boxMinX][boxMinY] = 14
                                gameMap[boxMinX + 1][boxMinY] = 14
                                gameMap[boxMinX + 1][boxMinY + 1] = 14
                                gameMap[boxMinX + 2][boxMinY + 1] = 14
                                rotate = 2
                            }
                        }
                        2 -> {

                            if (boxMinX + 2 < 20
                                && boxMinY + 1 < 10
                                && gameMap[boxMinX][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX][boxMinY + 2] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY + 1] !in 1..7
                            ) {
                                reset()
                                gameMap[boxMinX][boxMinY + 1] = 14
                                gameMap[boxMinX][boxMinY + 2] = 14
                                gameMap[boxMinX + 1][boxMinY] = 14
                                gameMap[boxMinX + 1][boxMinY + 1] = 14
                                rotate = 1
                            }
                        }
                    }
                }

                15 -> {
                    when (rotate) {
                        1 -> {

                            if (boxMinX + 2 < 20
                                && boxMinY + 1 < 10
                                && gameMap[boxMinX][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX + 2][boxMinY] !in 1..7
                                && gameMap[boxMinX + 2][boxMinY + 1] !in 1..7
                            ) {
                                reset()
                                gameMap[boxMinX][boxMinY + 1] = 15
                                gameMap[boxMinX + 1][boxMinY + 1] = 15
                                gameMap[boxMinX + 2][boxMinY] = 15
                                gameMap[boxMinX + 2][boxMinY + 1] = 15
                                rotate = 2
                            }
                        }

                        2 -> {
                            if (boxMinX + 1 < 20
                                && boxMinY + 2 < 10
                                && gameMap[boxMinX][boxMinY] !in 1..7
                                && gameMap[boxMinX][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX][boxMinY + 2] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY + 2] !in 1..7
                            ) {
                                reset()
                                gameMap[boxMinX][boxMinY] = 15
                                gameMap[boxMinX][boxMinY + 1] = 15
                                gameMap[boxMinX][boxMinY + 2] = 15
                                gameMap[boxMinX + 1][boxMinY + 2] = 15
                                rotate = 3
                            }
                        }

                        3 -> {
                            if (boxMinX + 1 < 20
                                && boxMinY + 2 < 10
                                && gameMap[boxMinX][boxMinY] !in 1..7
                                && gameMap[boxMinX][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY] !in 1..7
                                && gameMap[boxMinX + 2][boxMinY] !in 1..7
                            ) {
                                reset()
                                gameMap[boxMinX][boxMinY] = 15
                                gameMap[boxMinX][boxMinY + 1] = 15
                                gameMap[boxMinX + 1][boxMinY] = 15
                                gameMap[boxMinX + 2][boxMinY] = 15
                                rotate = 4
                            }
                        }

                        4 -> {
                            if (boxMinX + 1 < 20
                                && boxMinY + 2 < 10
                                && gameMap[boxMinX][boxMinY] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY + 1] !in 1..7
                                && gameMap[boxMinX + 1][boxMinY + 2] !in 1..7
                            ) {
                                reset()
                                gameMap[boxMinX][boxMinY] = 15
                                gameMap[boxMinX + 1][boxMinY] = 15
                                gameMap[boxMinX + 1][boxMinY + 1] = 15
                                gameMap[boxMinX + 1][boxMinY + 2] = 15
                                rotate = 1
                            }
                        }
                    }
                }

                16 -> {
                    when (rotate) {
                        1 -> {
                            val midpoint = boxArr[2]

                            if (midpoint.x - 1 >= 0
                                && midpoint.x + 1 < 20
                                && midpoint.y - 1 >= 0
                            ) {
                                reset()
                                gameMap[midpoint.x - 1][midpoint.y] = 16
                                gameMap[midpoint.x][midpoint.y] = 16
                                gameMap[midpoint.x][midpoint.y - 1] = 16
                                gameMap[midpoint.x + 1][midpoint.y] = 16
                                rotate = 2
                            }
                        }
                        2 -> {
                            val midpoint = boxArr[2]

                            if (midpoint.x - 1 >= 0
                                && midpoint.x + 1 < 20
                                && midpoint.y - 1 >= 0
                                && midpoint.y + 1 < 10
                            ) {
                                reset()
                                gameMap[midpoint.x][midpoint.y - 1] = 16
                                gameMap[midpoint.x][midpoint.y] = 16
                                gameMap[midpoint.x][midpoint.y + 1] = 16
                                gameMap[midpoint.x + 1][midpoint.y] = 16
                                rotate = 3
                            }
                        }

                        3 -> {
                            val midpoint = boxArr[1]
                            reset()

                            gameMap[midpoint.x - 1][midpoint.y] = 16
                            gameMap[midpoint.x][midpoint.y] = 16
                            gameMap[midpoint.x][midpoint.y + 1] = 16
                            gameMap[midpoint.x + 1][midpoint.y] = 16

                            rotate = 4
                        }
                        4 -> {
                            val midpoint = boxArr[1]

                            if (midpoint.y - 1 >= 0) {
                                reset()
                                gameMap[midpoint.x - 1][midpoint.y] = 16
                                gameMap[midpoint.x][midpoint.y - 1] = 16
                                gameMap[midpoint.x][midpoint.y] = 16
                                gameMap[midpoint.x][midpoint.y + 1] = 16
                                rotate = 1
                            }
                        }
                    }
                }

                17 -> {

                    val midpoint = boxArr[1]

                    when (rotate) {

                        1 -> {
                            if (midpoint.x - 1 >= 0 && midpoint.x + 2 < 20) {
                                reset()
                                gameMap[midpoint.x - 1][midpoint.y] = 17
                                gameMap[midpoint.x][midpoint.y] = 17
                                gameMap[midpoint.x + 1][midpoint.y] = 17
                                gameMap[midpoint.x + 2][midpoint.y] = 17
                                rotate = 2
                            }

                        }
                        2 -> {
                            if (midpoint.y - 1 >= 0 && midpoint.y + 2 < 10) {
                                reset()
                                gameMap[midpoint.x][midpoint.y - 1] = 17
                                gameMap[midpoint.x][midpoint.y] = 17
                                gameMap[midpoint.x][midpoint.y + 1] = 17
                                gameMap[midpoint.x][midpoint.y + 2] = 17
                                rotate = 1
                            }
                        }
                    }
                }
            }

            changeListener?.onChange()
        }

    }

    /**
     * 向左移动正在下落的方块
     */
    fun toLeft() {
        val leftArr = mutableListOf<Coordinate>()

        for (i in gameMap.indices) for (j in gameMap[i].indices) if (gameMap[i][j] in 11..17)
            leftArr.add(Coordinate(i, j, gameMap[i][j]))

        if (0 == leftArr.size) return

        //是否允许向左移动
        var toLeftStatus = true

        //向左移动条件：符合移动方块并且任何左侧为空方块
        for (coordinate in leftArr) {
            if (coordinate.y == 0 || (coordinate.y > 0 && gameMap[coordinate.x][coordinate.y - 1] in 1..7)) {
                toLeftStatus = false
                break
            }
        }

        //向左移动
        if (toLeftStatus) {
            for (i in 0 until leftArr.size) {
                gameMap[leftArr[i].x][leftArr[i].y - 1] = leftArr[0].value
                gameMap[leftArr[i].x][leftArr[i].y] = 0
            }
            changeListener?.onChange()
        }
    }

    /**
     * 向右移动正在下落的方块
     */
    fun toRight() {

        val rightArr = mutableListOf<Coordinate>()

        for (i in gameMap.indices) for (j in gameMap[i].indices) if (gameMap[i][j] in 11..17)
            rightArr.add(Coordinate(i, j, gameMap[i][j]))

        if (0 == rightArr.size) return

        //是否允许向右移动
        var toRightStatus = true

        //向右移动条件：符合移动方块并且任何右侧为空方块
        for (coordinate in rightArr) if (coordinate.y == 9 || (coordinate.y < 10 && gameMap[coordinate.x][coordinate.y + 1] in 1..7)) {
            toRightStatus = false
            break
        }

        //向右移动
        if (toRightStatus) {
            for (i in rightArr.size - 1 downTo 0) {
                gameMap[rightArr[i].x][rightArr[i].y + 1] = rightArr[0].value
                gameMap[rightArr[i].x][rightArr[i].y] = 0
            }
            changeListener?.onChange()
        }
    }

    /**
     * 直接下落到底部
     */
    fun downBottom() {
        val downArr = mutableListOf<Coordinate>()
        for (i in gameMap.size - 1 downTo 0) for (j in gameMap[i].size - 1 downTo 0) {
            val coordinate = gameMap[i][j]
            if (coordinate in 11..17) downArr.add(Coordinate(i, j, coordinate))
        }

        if (0 != downArr.size) {

            var isFixed = false

            val valueTemp = downArr[0].value

            for (coordinate in downArr) if (coordinate.x + 1 >= 20 || gameMap[coordinate.x + 1][coordinate.y] in 1..7) return

            for (coordinate in downArr) {

                gameMap[coordinate.x][coordinate.y] = 0
                gameMap[coordinate.x + 1][coordinate.y] = valueTemp

                //判断是否落到了最后
                if (coordinate.x + 1 >= 19 || gameMap[coordinate.x + 2][coordinate.y] in 1..7)
                    isFixed = true
            }

            if (!isFixed) downBottom() else changeListener?.onChange()

        } else changeListener?.onChange()
    }

    /**
     * 在顶部创建一个新的模块
     */
    private fun createTopBox() {

        //判断是否有预测，没有预测则生成方块
        if (0 == forecastType) forecastType = (1..7).random()

        //重置旋转度数
        rotate = 1

        //每生成5个方块增加一次方块下落速度
        if (++boxNum % 5 == 0 && speed > 150) speed -= 20

        //游戏是否结束
        var gameOverStatus = false

        //生成下落方块
        when (forecastType) {
            1 -> if (gameMap[0][4] !in 1..7
                && gameMap[0][5] !in 1..7
                && gameMap[1][4] !in 1..7
                && gameMap[1][5] !in 1..7
            ) {
                gameMap[0][4] = 11
                gameMap[0][5] = 11
                gameMap[1][4] = 11
                gameMap[1][5] = 11
            } else gameOverStatus = true

            2 -> if (gameMap[0][5] !in 1..7
                && gameMap[1][3] !in 1..7
                && gameMap[1][4] !in 1..7
                && gameMap[1][5] !in 1..7
            ) {
                gameMap[0][5] = 12
                gameMap[1][3] = 12
                gameMap[1][4] = 12
                gameMap[1][5] = 12
            } else gameOverStatus = true

            3 -> if (gameMap[0][3] !in 1..7
                && gameMap[0][4] !in 1..7
                && gameMap[1][4] !in 1..7
                && gameMap[1][5] !in 1..7
            ) {
                gameMap[0][3] = 13
                gameMap[0][4] = 13
                gameMap[1][4] = 13
                gameMap[1][5] = 13
            } else gameOverStatus = true

            4 -> if (gameMap[0][4] !in 1..7
                && gameMap[0][5] !in 1..7
                && gameMap[1][3] !in 1..7
                && gameMap[1][4] !in 1..7
            ) {
                gameMap[0][4] = 14
                gameMap[0][5] = 14
                gameMap[1][3] = 14
                gameMap[1][4] = 14
            } else gameOverStatus = true

            5 -> if (gameMap[0][3] !in 1..7
                && gameMap[1][3] !in 1..7
                && gameMap[1][4] !in 1..7
                && gameMap[1][5] !in 1..7
            ) {
                gameMap[0][3] = 15
                gameMap[1][3] = 15
                gameMap[1][4] = 15
                gameMap[1][5] = 15
            } else gameOverStatus = true

            6 -> if (gameMap[0][4] !in 1..7
                && gameMap[1][3] !in 1..7
                && gameMap[1][4] !in 1..7
                && gameMap[1][5] !in 1..7
            ) {
                gameMap[0][4] = 16
                gameMap[1][3] = 16
                gameMap[1][4] = 16
                gameMap[1][5] = 16
            } else gameOverStatus = true

            7 -> if (gameMap[0][3] !in 1..7
                && gameMap[0][4] !in 1..7
                && gameMap[0][5] !in 1..7
                && gameMap[0][6] !in 1..7
            ) {
                gameMap[0][3] = 17
                gameMap[0][4] = 17
                gameMap[0][5] = 17
                gameMap[0][6] = 17
            } else gameOverStatus = true
        }

        //生成预测方块
        forecastType = (1..7).random()

        if (gameOverStatus) {

            //回调游戏结束
            changeListener?.gameOver(score)

            //重置游戏数据
            speed = 750L
            isInGame = false
            rotate = 1
            score = 0L
            forecastType = 0
            boxNum = 0
            for (i in gameMap.indices) for (j in gameMap[i].indices) gameMap[i][j] = 0
        }
    }

    /**
     * 固定移动中的方块方块
     */
    private fun fixedMoveBox() {
        for (i in gameMap.indices) for (j in gameMap[i].indices) if (gameMap[i][j] in 11..17)
            gameMap[i][j] = gameMap[i][j] - 10
    }

    /**
     * 方块下落
     */
    private fun downBox(): Boolean {

        //扫描需要下落的方块
        val downArr = mutableListOf<Coordinate>()
        for (i in gameMap.size - 1 downTo 0) for (j in gameMap[i].size - 1 downTo 0) if (gameMap[i][j] in 11..17)
            downArr.add(Coordinate(i, j, gameMap[i][j]))

        //判断是否无法继续下移
        if (downArr.size == 0) return false

        //判断是否还能继续下移
        //判断条件：任何一个正在移动的方块是否达到了第20行或者触碰到了固定方块
        for (i in 0 until downArr.size) if (downArr[i].x + 1 > 19 || gameMap[downArr[i].x + 1][downArr[i].y] in 1..7) return false

        val coordinateTempValue = downArr[0]
        for (j in 0 until downArr.size) {
            val coordinateTemp = downArr[j]
            gameMap[coordinateTemp.x][coordinateTemp.y] = 0
            gameMap[coordinateTemp.x + 1][coordinateTemp.y] = coordinateTempValue.value
        }

        return true
    }

    //游戏实时变化回调
    var changeListener: ChangeListener? = null

    /**
     * 游戏实时变化回调
     */
    interface ChangeListener {
        fun onChange()
        fun gameOver(score: Long)
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