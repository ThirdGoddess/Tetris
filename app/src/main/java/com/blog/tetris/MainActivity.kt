package com.blog.tetris

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blog.tetris.operation.Operation
import com.blog.tetris.view.GameView


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

        gameView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {

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

//                Log.d("mylog-event-a", p1?.action.toString())
//                Log.d("mylog-event-x", p1?.x.toString())
//                Log.d("mylog-event-y", p1?.y.toString())
//                Log.d("mylog-event-t", p1?.downTime.toString())
//                Log.d("mylog-event-d", p1?.eventTime.toString())
//                Log.d("mylog-event", "==================")
                return true
            }
        })


        //游戏变化回调
        Operation.changeListener = object : Operation.ChangeListener {
            override fun onChange() {
                gameView.refresh()
            }
        }

        playGame.setOnClickListener {
            Operation.startGame()
        }
    }


}