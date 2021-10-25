package com.blog.tetris

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


    fun getMapArr(): Array<Array<Int>> {
        gameMap[0][1] = 0
        gameMap[1][2] = 1
        gameMap[2][3] = 2
        gameMap[2][4] = 3
        gameMap[3][1] = 4
        gameMap[3][2] = 5
        gameMap[4][0] = 6
        gameMap[4][1] = 7
        gameMap[4][4] = 7
        return gameMap
    }


}