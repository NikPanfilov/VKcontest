package com.example.vk

import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.vk.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val ellipseList = mutableListOf<View>()
    private val buttonList = mutableListOf<Button>()
    private val addDelay = 750L // Сколько миллисекунд нужно держать палец на кнопке

    private lateinit var binding: ActivityMainBinding
    private var pinCode = ""
    private var num = ""
    private var rect = Rect()
    private var lastLength = -1
    private var lastAddSuccess = true
    private var dialogShowing = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        for (id in binding.ellipses.referencedIds) {
            ellipseList.add(findViewById(id))
        }

        for (id in binding.numButtons.referencedIds) {
            val button = findViewById<Button>(id)
            buttonList.add(button)
            button.setOnTouchListener { view, motionEvent ->
                swipeInput(view, motionEvent)
            }
        }

        binding.deleteButton.setOnClickListener { deleteSymbol() }
        binding.root.setOnTouchListener { view, motionEvent ->
            swipeInput(view, motionEvent)
        }
    }

    private fun swipeInput(view: View, event: MotionEvent): Boolean {
        var buttonTouch = false
        if (event.action == MotionEvent.ACTION_DOWN && view in buttonList) {
            addSymbol((view as Button).text.toString())
        }
        if (event.action == MotionEvent.ACTION_MOVE) {
            for (button in buttonList) {
                button.getHitRect(rect)
                if (rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    buttonTouch = true
                    if ((num != button.text.toString() || lastAddSuccess) && !dialogShowing) {
                        num = button.text.toString()
                        lastAddSuccess = false
                        checkByTime(num)
                    }
                }
            }
        }
        if (event.action == MotionEvent.ACTION_UP || !buttonTouch) {
            num = ""
        }
        return true
    }

    private fun checkByTime(touchNum: String) {
        object : CountDownTimer(addDelay, 1) {
            override fun onTick(p0: Long) {}
            override fun onFinish() {
                if (touchNum == num) {
                    lastAddSuccess = true
                    addSymbol(touchNum)
                    checkByTime(touchNum)
                }
            }
        }.start()
    }

    private fun deleteSymbol() {
        if (pinCode.isNotEmpty()) {
            pinCode = pinCode.substring(0, pinCode.length - 1)
            ellipseList[pinCode.length].setBackgroundResource(R.drawable.ic_ellipse_empty)
            lastLength = pinCode.length
        }
    }


    private fun addSymbol(symbol: String) {
        pinCode += symbol
        ellipseList[pinCode.length - 1].setBackgroundResource(R.drawable.ic_ellipse_full)
        // Смена background не работает для четвёртого символа,
        // судя по дебагеру - строка выполняется,
        // но background меняется с сильной задержкой (так не только для последнего),
        // то есть он просто не успевает заменить его
        // invalidate не помог.
        // В мобильном банке сбера такая же проблема.

        if (pinCode.length == 4) {
            val processing = PinCodeProcessing(this)
            when (processing.checkPinCode(pinCode)) {
                PinCheckResult.ASSIGNED -> showMessage(
                    R.string.title_confirmation,
                    R.string.text_pin_assigned
                )
                PinCheckResult.SUCCESS -> showMessage(
                    R.string.title_confirmation,
                    R.string.text_right_pin
                )
                PinCheckResult.FAILED -> showMessage(
                    R.string.title_wrong_pin,
                    R.string.text_wrong_pin
                )
            }

            for (ellipse in ellipseList) {
                ellipse.setBackgroundResource(R.drawable.ic_ellipse_empty)
            }
            pinCode = ""
            num = ""
            lastLength = -1
        }

    }


    private fun showMessage(title: Int, text: Int) {
        dialogShowing = true
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(getString(title))
        alertDialog.setMessage(getString(text))
        alertDialog.setNeutralButton(
            getString(R.string.ok)
        ) { _, _ -> dialogShowing = false }
        alertDialog.show()
    }
}