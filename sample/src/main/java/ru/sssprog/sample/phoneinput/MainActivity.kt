package ru.sssprog.sample.phoneinput

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import ru.sssprog.phoneinput.PhoneInputView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initValidation()
        actionButton.setOnClickListener {
            AlertDialog.Builder(this)
                    .setMessage("Phone number: ${phoneView.getPhoneNumber()}")
                    .show()
        }
    }

    private fun initValidation() {
        phoneView.textChangedListener = PhoneInputView.TextChangedListener {
            updateButtonState()
        }
        updateButtonState()
    }

    private fun updateButtonState() {
        actionButton.isEnabled = phoneView.isValidPhone()
    }
}