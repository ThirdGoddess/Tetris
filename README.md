## 效果图
废话不多说，先干效果图，源码在文章末尾

![效果图](https://img-blog.csdnimg.cn/64bfb76431b645258023c54e163f3360.gif)
![在这里插入图片描述](https://img-blog.csdnimg.cn/313cc4026b084e929fe4efba189f9611.png)



---

## 游戏概念
《俄罗斯方块》是由七种方块，开始时，一个 落下期间，玩家可以以90度为单位旋转方块，以格子为单位左右移动方块，或让方块加速落下。当方块下落到区域最下方或着落到其他方块上无法再向下移动时，就会固定在该处，然后一个新的随机的方块会出现在区域上方开始落下。当区域中某一横行（同时消除的行数越多，得分指数级上升。当固定的方块堆到区域最顶端而无法消除层数时，游戏就会结束。

---
## 操作设计
起初我想的是在屏幕底部做按键操作的，可后来一想，这也太扯淡了
![在这里插入图片描述](https://img-blog.csdnimg.cn/40d2fcdbbf1240128cdc2486e8b890e6.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA56ys5LiJ5aWz56We56iL5b-G6Zq-,size_7,color_FFFFFF,t_70,g_se,x_16 =150x)
现在是触屏手机，做按钮的话不好按，容易误触，所定以下的手势:

1. 向上滑动：顺时针旋转正在下落的方块90° 
2. 向左滑动：正在下落的方块向左移动一个方格单位
3. 向右滑动，正在下落的方块向右移动一个方格单位
4. 向左持续滑动：正在下落的方块向左持续移动
5. 向右持续滑动：正在下落的方块向右持续移动
6. 向下滑动：方块直落底部

说到扯淡但，来个更扯淡的操作吧，你可以设置成
向下滑动和向上滑动操作相反
向左滑动和向右滑动操作相反
向左持续滑动和向右持续滑动操作相反
（反正这个操作没多大工作量，写个if调换以下方法位置就行...）

---
## 算法规则
当行满足无空格时，消除当前行，有几行会消除几行，一次消除的越多，得分也就越多，咱就不定1分2分的了，这么玩着没啥意思
![在这里插入图片描述](https://img-blog.csdnimg.cn/3c11e56cd84d42168407e64775bb5dd6.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA56ys5LiJ5aWz56We56iL5b-G6Zq-,size_7,color_FFFFFF,t_70,g_se,x_16 =150x)

干脆消除1行100分
一次性消除2行是200分
一次性消除3行是400分
一次性消除4行是800分

方块下落速度会越来越快，刚开始下落速度为750ms/格，逐渐累加，最快是150ms/格
按方块下落的个数计算，每5个方块为1个递增，递增20ms/格（这边是我自己想的）

---
## 整体算法概述
UI使用自定义View来实现，命名为GameView，背后的运行逻辑通过二维数组来控制，10X20布局；
当方块每下落一格和消除时，刷新整个GameView；
0表示可操作空间占位；
1表示已固定方块类型一方块占位，11表示正在下落的类型一方块
2表示已固定方块类型二方块占位，12表示正在下落的类型二方块
3表示已固定方块类型三方块占位，13表示正在下落的类型三方块
以此类推共7种

---
## 代码实操
“只说不练的把式，光练不说傻把式”，这是程序员的最大忌讳，昨天媳妇给力（做的饭好吃），今天快快地撸代码！

### 操作设计
创建二维数组，并允许可以静态调用，其默认值为0。封装多个公共调用方法！详细看以下注释

```kotlin
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
```

---

### 可视化View
数据逻辑写完了，要让用户看的见呀，通过自定义View来实现，详细看以下注释

```kotlin
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
```

---

### MainActivity
都到这里了，代码也就贴全了

```kotlin
package com.blog.tetris

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.blog.tetris.operation.Operation
import com.blog.tetris.view.GameView
import com.blog.tetris.view.StatusBarUtils


class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var playGame: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initData()
        initListener()

    }

    private fun initView() {

        //设置状态栏字体为黑色
        StatusBarUtils.darkMode(this)

        gameView = findViewById(R.id.gameView)
        playGame = findViewById(R.id.playGame)
    }

    private fun initData() {
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {

        var downX = 0F
        var downY = 0F
        var direction = 0

        gameView.setOnTouchListener { _, p1 ->
            when (p1?.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = p1.x
                    downY = p1.y
                }
                MotionEvent.ACTION_MOVE -> {

                    //锁定方向
                    if (0 == direction) {
                        if (p1.y - downY > 200) direction = 4
                        if ((p1.x - downX < -80 || p1.x - downX > 80)) direction = 1
                        if (p1.y - downY < -200) direction = 2
                    } else {
                        when (direction) {

                            //横向滑动
                            1 -> {
                                if (p1.x - downX > gameView.boxSize) {
                                    downX = p1.x
                                    Operation.toRight()
                                } else if (p1.x - downX < -gameView.boxSize) {
                                    downX = p1.x
                                    Operation.toLeft()
                                }
                            }

                            //向上滑动
                            2 -> {
                                direction = -1
                                Operation.deformation()
                            }

                            //向下滑动
                            4 -> {
                                direction = -1
                                Operation.downBottom()
                            }
                        }
                    }


                }
                MotionEvent.ACTION_UP -> {
                    downX = 0F
                    downY = 0F
                    direction = 0
                }
            }
            true
        }


        //游戏变化回调
        Operation.changeListener = object : Operation.ChangeListener {
            override fun onChange() {
                gameView.refresh()
            }

            override fun gameOver(score: Long) {
                runOnUiThread {
                    val dialog = AlertDialog.Builder(this@MainActivity)
                    dialog.setTitle("提示")
                    dialog.setMessage("游戏结束! 得分:$score")
                    dialog.setPositiveButton(
                        "重新开始"
                    ) { _, _ -> Operation.startGame() }
                    dialog.setNegativeButton(
                        "取消"
                    ) { _, _ -> }
                    dialog.setCancelable(false)
                    dialog.show()
                }
            }
        }

        playGame.setOnClickListener {
            Operation.startGame()
        }
    }


}
```

---

## 源码下载
Github：[https://github.com/ThirdGoddess/Tetris](https://github.com/ThirdGoddess/Tetris)
