package com.example.minesweeper

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.minesweeper.BoardActivity.Companion.LAST_GAME_SCORE
import com.example.minesweeper.BoardActivity.Companion.STATUS_WON
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    companion object {
        const val INTENT_ROWS = "ROWS"
        const val INTENT_COLS = "COLUMNS"
        const val INTENT_MINES = "MINES"
    }
    private var status = "STATUS LOST"
    private var highScore = 0
    private var lastGameScore = 0
    private lateinit var lastGameTimeLabel : TextView
    private lateinit var bestTimeLabel : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lastGameTimeLabel = findViewById(R.id.lastgameTime)
        bestTimeLabel = findViewById(R.id.bestTime)
        val startButton:Button = findViewById(R.id.start_button)
        start_button.setOnClickListener{
            selectDifficulty()}
// loads saved game score using shared preference
        loadGameScore()

        // saves game score into saved preference
        saveGameScore()

//        selects difficulty level
            selectDifficulty()

        //select custom board
        val customButton: Button = findViewById(R.id.custom_button)
        customButton.setOnClickListener{
            addDialog()
        }
    }

    // This function selects between easy, medium or hard
    //easy 9*9, mine=10
    //medium 12*12, mine = 15
    //hard 15*15, mine = 20
    private fun selectDifficulty() {

        difficulty.setOnCheckedChangeListener{_,checkedId ->
    when (checkedId) {
                R.id.easy -> {
                    val intent = Intent(this, BoardActivity::class.java ).apply {
                        intent.putExtra(INTENT_ROWS,9)
                        intent.putExtra(INTENT_COLS, 9)
                        intent.putExtra(INTENT_MINES, 10)
                    }
                    startActivity(intent)
                    Log.d("DEBUG","ACTIVITY STARTED")
                }
                R.id.medium-> {
                    val intent = Intent(this, BoardActivity::class.java ).apply {
                        intent.putExtra(INTENT_ROWS,12)
                        intent.putExtra(INTENT_COLS, 12)
                        intent.putExtra(INTENT_MINES, 15)

                    }
                    startActivity(intent)
                }
                R.id.hard -> {
                    val intent = Intent(this, BoardActivity::class.java ).apply {
                        intent.putExtra(INTENT_ROWS,15)
                       intent.putExtra(INTENT_COLS, 15)
                        intent.putExtra(INTENT_MINES, 20)
                    }
                    startActivity(intent)
                }

            }
        }
    }

    // custom layout
    private fun addDialog(){
        val inflater = this.layoutInflater
        val view : View = inflater.inflate(R.layout.custom_board,null)
        val rows = view.findViewById<EditText>(R.id.enter_rows)
        val columns: EditText = view.findViewById(R.id.enter_cols)
        val mines: EditText = view.findViewById(R.id.enter_mines)

        val builder = AlertDialog.Builder(this)
        with(builder){
            setTitle("Custom Board")
            setMessage("Please enter the details for custom board.")
            setView(view)
            setPositiveButton("Start"){ _,_ ->
                val enteredRows = rows.text.toString().toIntOrNull()
                val enteredCols = columns.text.toString().toIntOrNull()
                val enteredMines = mines.text.toString().toIntOrNull()
                if(enteredCols!=null && enteredRows!=null && enteredMines!=null) {
                    val intent = Intent(this@MainActivity, BoardActivity::class.java).apply {
                        putExtra(INTENT_ROWS, enteredRows)
                        putExtra(INTENT_COLS, enteredCols)
                        putExtra(INTENT_MINES, enteredMines)
                    }
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this@MainActivity,"Invalid Input", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel"){ _,_ ->
            }
            show()
        }
    }
    private fun loadGameScore(){
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        lastGameScore = sharedPref.getInt(getString(R.string.LAST_GAME_SCORE), 0)
        highScore = sharedPref.getInt(getString(R.string.HIGH_SCORE), 0)
        lastGameTimeLabel.text = "Last Game Time: ${lastGameScore.toString()} sec"
        bestTimeLabel.text = "Best Time: ${highScore.toString()} sec"
    }
    private fun saveGameScore(){
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        lastGameScore = intent.getIntExtra(LAST_GAME_SCORE, 0 )/1000

        status = intent.getStringExtra(STATUS_WON) ?: return
        if(highScore == 0 && status == "STATUS WON"){
            highScore = lastGameScore
        }
        else if(highScore>lastGameScore && status == "STATUS WON"){
            highScore = lastGameScore
        }
        with(sharedPreferences.edit()){
            putInt(getString(R.string.LAST_GAME_SCORE), lastGameScore)
            putInt(getString(R.string.HIGH_SCORE), highScore)
            commit()
        }
        lastGameTimeLabel.text = "Last Game Time: ${lastGameScore.toString()} sec"
        bestTimeLabel.text = "Best Time: ${highScore.toString()} sec"
    }

}




