package ru.sssprog.phoneinput

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.*

class PhoneInputView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val maskView: TextView
    private val editText: EditText

    private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    private var phone = ""
    var textChangedListener: TextChangedListener? = null

    init {
        val array = getContext().obtainStyledAttributes(attrs, R.styleable.PhoneInputView)
        val phoneLayout = array.getResourceId(R.styleable.PhoneInputView_phone_layout, R.layout.phone_input_view)
        array.recycle()

        inflate(context, phoneLayout, this)
        maskView = findViewById(R.id.maskView)
        editText = findViewById(R.id.editText)
        initBehavior()

        val callingCode = phoneNumberUtil.getCountryCodeForRegion(Locale.getDefault().country)
        phone = "+$callingCode"
        editText.setText(phone)
    }

    private fun initBehavior() {
        editText.addTextChangedListener(object : TextWatcher {
            private var canValidate = true
            private var inserted = false
            private var deleted = false

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count > 0 && (before > 0 || s.length != start + count)) {
                    inserted = true
                }
                if (before > 0 && count == 0) {
                    deleted = true
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (!canValidate) {
                    return
                }
                canValidate = false

                if (!inserted) {
                    phone = updatePhone(s.toString(), deleted)
                }

                s.replace(0, s.length, phone)
                canValidate = true
                inserted = false
                deleted = false
                textChangedListener?.onTextChanged()
            }
        })
    }

    private fun findCountry(input: String): String? {
        var digits = input.replace(Regex("[^\\d.]"), "")
        while (digits.isNotEmpty()) {
            val callingCode = try {
                digits.toInt()
            } catch (e: Throwable) {
                0
            }
            val countryCode = phoneNumberUtil.getRegionCodeForCountryCode(callingCode)
//            Timber.d("countryCode for code $callingCode, $countryCode")
            if (countryCode !in listOf("ZZ", "001")) {
                return countryCode
            }
            digits = digits.substring(0, digits.length - 1)
        }
        return null
    }

    @SuppressLint("SetTextI18n")
    private fun updatePhone(input: String, deleted: Boolean): String {
        val countryCode = findCountry(input)
//        Timber.d("countryCode $countryCode")
        if (countryCode == null) {
            maskView.text = null
            return input
        }

        val examplePhone = phoneNumberUtil.getExampleNumber(countryCode)
        val mask = phoneNumberUtil.format(examplePhone, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)

        val formattedPhoneNumber = if (deleted) {
            input
        } else {
            formatPhoneNumber(input, mask)
        }

        val maskText = SpannableStringBuilder(formattedPhoneNumber + mask.substring(formattedPhoneNumber.length))
        maskText.setSpan(ForegroundColorSpan(Color.TRANSPARENT), 0, formattedPhoneNumber.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        maskView.text = maskText

        return formattedPhoneNumber
    }

    private fun formatPhoneNumber(input: String, mask: String): String {
        var result = ""
        var digits = input.replace(Regex("[^\\d.]"), "")
        var index = 0
        while (digits.isNotEmpty() && index < mask.length) {
            val char = mask[index]
            if (char.isDigit()) {
                result += digits.first()
                digits = digits.substring(1)
            } else {
                result += mask[index]
            }
            index++
        }

        while (index < mask.length && !mask[index].isDigit()) {
            result += mask[index]
            index++
        }

        return result
    }

    fun getPhoneNumber(): String = editText.text.toString()

    fun isValidPhone(): Boolean {
        return getPhoneNumber().isNotEmpty() && getPhoneNumber() == maskView.text.toString()
    }

    fun interface TextChangedListener {
        fun onTextChanged()
    }
}