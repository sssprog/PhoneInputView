# PhoneInputView
Android library for entering international phone numbers.

Google's **libphonenumber** is used for phone validation, formatting and number examples.

![Alt text](media/sample.gif?raw=true)

To customize appearance, view accepts custom layout in parameter `phone_layout`
```
<ru.sssprog.phoneinput.PhoneInputView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:phone_layout="@layout/custom_phone_input" />
```

Custom layout must contain `TextView` with id `maskView` and `EditText` with id `editText`.
