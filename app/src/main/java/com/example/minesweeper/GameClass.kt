package com.example.minesweeper

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

//custom button for the grid
class MineCell(var value : Int = 0, var isRevealed : Boolean = false, var isMarked : Boolean = false, context: Context)
    : androidx.appcompat.widget.AppCompatButton(context)