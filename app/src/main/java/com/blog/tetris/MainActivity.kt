package com.blog.tetris

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        gameView.refresh()
    }

    private fun initData() {
    }

    private fun initListener() {

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