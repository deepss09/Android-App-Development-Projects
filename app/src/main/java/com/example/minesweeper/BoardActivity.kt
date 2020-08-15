package com.example.minesweeper

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import kotlinx.android.synthetic.main.activity_board2.*


class BoardActivity : AppCompatActivity() {
    private var getRowIntent = 0 //no. of rows
    private var getColumnIntent = 0 //no. of columns
    private var getMineIntent = 0 //no. of mines
    private var mineCount = 0
    private var flag = 0
    private var lastGameTime = 0
    private var bestTime = 0
    private var elapsedTime : Long = 0
    private lateinit var minesCountLabel: TextView
    private lateinit var board: Array<Array<MineCell>> // custom button
    private lateinit var chronometer: Chronometer
    private var minesOptions: MutableSet<Int> = mutableSetOf() // list of buttons in which mines are present
    companion object {
        const val MINE = -1
        val movement = intArrayOf(-1, 0, 1)
        const val LAST_GAME_SCORE = "LAST_GAME_SCORE"
        const val STATUS_WON= "STATUS WON"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board2)

        minesCountLabel = findViewById(R.id.mines_left)
        getRowIntent = intent.getIntExtra(MainActivity.INTENT_ROWS, 9)
        getColumnIntent = intent.getIntExtra(MainActivity.INTENT_COLS, 9)
        getMineIntent = intent.getIntExtra(MainActivity.INTENT_MINES, 10)
        mineCount = getMineIntent
        minesCountLabel.text = mineCount.toString()
        board = Array(getRowIntent) { Array(getColumnIntent) { MineCell(context = this) } }

        //setup board
        setupGameBoard(getRowIntent, getColumnIntent)

        //update neighbours function
        for (i in 0 until getRowIntent) {
            for (j in 0 until getColumnIntent) {
                if (board[i][j].value == MINE) { ///MINE
                    updateNeighbours(i, j)
                }
            }
        }

        //move function for when we click the button
        for (i in 0 until getRowIntent) {
            for (j in 0 until getColumnIntent) {
                move(i, j)
            }
        }

        //restart button restarts the game and send time to the MainActivity only if the you won the game
        val restartButton = findViewById<Button>(R.id.restart_button)
        restartButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(LAST_GAME_SCORE, lastGameTime)
                if(status==Status.WON){
                    putExtra(STATUS_WON, "STATUS WON")
                }
            }
            startActivity(intent)
        }
    }

    private var status = Status.ONGOING
    //setup the game board into grid
    private fun setupGameBoard(row: Int, col: Int) {
        val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 150)
        var counter = 1
        val params2 = LinearLayout.LayoutParams(150, 150)
        for (i in 0 until row) {
            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.layoutParams = params1
            for (j in 0 until col) {
                val button = MineCell(context = this)
                button.id = counter
                button.layoutParams = params2
                button.setBackgroundResource(R.drawable.square)
                button.textSize = 30F
                board[i][j] = button
                linearLayout.addView(board[i][j])
                counter++
            }
            game_grid.addView(linearLayout)
        }
        randomMines()
    }

    // start a chronometer
    private fun startTimer() {
        chronometer = findViewById(R.id.chronometer)
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
    }

    //this function gives random values
    private fun rand(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        return (start..end).random()
    }

    //this function puts mines in random location of the board
    private fun randomMines() {
        while (minesOptions.size != getMineIntent) {
            minesOptions.add(rand(1, (getRowIntent * getColumnIntent)))
        }
        for (i in 0 until getRowIntent) {
            for (j in 0 until getColumnIntent) {
                if (minesOptions.contains(board[i][j].id)) {
                    board[i][j].value = MINE
                }
            }
        }
    }

    //this function shows images on the button after it is revealed or marked
    private fun showClickedTiles(row: Int, col: Int) {
        val drawableResource = when (board[row][col].value) {
            0 -> R.drawable.number_0
            1 -> R.drawable.number_1
            2 -> R.drawable.number_2
            3 -> R.drawable.number_3
            4 -> R.drawable.number_4
            5 -> R.drawable.number_5
            6 -> R.drawable.number_6
            7 -> R.drawable.number_7
            else -> R.drawable.number_8
        }
        if (board[row][col].value == MINE && board[row][col].isRevealed) {
            board[row][col].setBackgroundResource(R.drawable.mine)
        } else if (board[row][col].value != MINE && board[row][col].isRevealed) {
            board[row][col].setBackgroundResource(drawableResource)
        } else if (board[row][col].isMarked) {
            board[row][col].setBackgroundResource(R.drawable.flag)
        } else if (!board[row][col].isMarked) {
            board[row][col].setBackgroundResource(R.drawable.square)
        }
    }

    //decides the functionality of the whole game i.e, basically whole logic of the game. This
    // function reveals the button if it is clicked and marked the button if it is long Pressed

    private fun move(row: Int, col: Int) {
        if (board[row][col].isRevealed) {
            board[row][col].isClickable = false
        }
        board[row][col].setOnClickListener {
            if (board[row][col].isMarked && !board[row][col].isRevealed) {
                mineCount++
                minesCountLabel.text = "$mineCount"
                board[row][col].isRevealed = true
            }
            if (board[row][col].value == MINE) {
                //display image of all mines
                status = Status.LOST
                board[row][col].isRevealed = true
                showClickedTiles(row, col)
                displayBoard()
                disableButtons()
                chronometer.stop()
                elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
                lastGameTime = elapsedTime.toInt()
                Toast.makeText(this, "Sorry! you lost. Try again", Toast.LENGTH_SHORT).show()
            } else {
                // show value of button
                board[row][col].isRevealed = true
                showClickedTiles(row, col)
                reveal(row, col)
            }
            //to start a timer only on first button click
            if (flag == 0) {
                flag = 1
                startTimer()
            }
        }
        board[row][col].setOnLongClickListener {
            if (!board[row][col].isMarked && !board[row][col].isRevealed) {
                board[row][col].isMarked = true
                showClickedTiles(row, col)
                mineCount--
                minesCountLabel.text = "$mineCount"

            } else if (!board[row][col].isRevealed) {
                board[row][col].isMarked = false
                showClickedTiles(row, col)
                mineCount++
                minesCountLabel.text = "$mineCount"
            }
            if (isComplete() && status != Status.LOST) {
                status = Status.WON
                chronometer.stop()
                elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
                disableButtons()
                lastGameTime = elapsedTime.toInt()
                Toast.makeText(this, "Congratulations! You won.", Toast.LENGTH_SHORT).show()
            }
            return@setOnLongClickListener true
        }
    }

    //this function update neighbours around a mine
    private fun updateNeighbours(row: Int, col: Int) {
        for (i in movement) {
            for (j in movement) {
                if ((row + i) >= 0 && (row + i) < getRowIntent && (col + j) >= 0 && (col + j) < getColumnIntent
                    && board[row + i][col + j].value != MINE
                ) {
                    board[row + i][col + j].value++
                }
            }
        }
    }

    //this function checks if the game is complete or not
    private fun isComplete(): Boolean {
        var markedMine = true
        board.forEach { row ->
            row.forEach {
                if (it.value == MINE && !it.isMarked)
                    markedMine = false
            }
        }
        var areRevealed = true
        board.forEach { row ->
            row.forEach {
                if (it.value != MINE && !it.isRevealed) {
                    areRevealed = false
                }
            }
        }
        return markedMine || areRevealed
    }

    // this function reveals the button if its value is 0 and also its neighbours whose value is 0
    // and not equal to MINE(-1)
    private fun reveal(row: Int, col: Int) {
        if (board[row][col].value == 0) {
            for (i in movement) {
                for (j in movement) {
                    if ((i != 0 || j != 0) && (row + i) >= 0 && (row + i) < getRowIntent && (col + j) >= 0 && (col + j) < getColumnIntent
                        && board[row + i][col + j].value != MINE
                    ) {
                        if (!board[row + i][col + j].isRevealed && !board[row + i][col + j].isMarked) {
                            board[row + i][col + j].isRevealed = true
                            showClickedTiles(row + i, col + j)
                            reveal(row + i, col + j)
                        }
                    }
                }
            }
        }
    }

    //disables all the board buttons when game is complete (lost or won)
    private fun disableButtons() {
        board.forEach { row ->
            row.forEach {
                it.isEnabled = false
            }
        }
    }

    //reveals all buttons where mine is present if you won or lost
    private fun displayBoard() {
        board.forEach { row ->
            row.forEach {
                if (status == Status.WON && it.value == MINE) {
                    it.setBackgroundResource(R.drawable.mine2)
                } else if (status == Status.LOST && it.value == MINE) {
                    it.setBackgroundResource(R.drawable.mine)
                }
            }
        }
    }
}
