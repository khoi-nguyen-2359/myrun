package akio.apps.myrun.feature.signin.view

import akio.apps._base.ui.inflate
import akio.apps._base.utils.PhoneNumberHelper
import akio.apps.myrun.R
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import java.util.Locale
import timber.log.Timber

class PhoneBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var eventListener: EventListener? = null

    private var phoneNumberWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            eventListener?.onPhoneBoxValueChanged(getFullNumber())
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }
    }

    private val phoneCodeSpinner: Spinner
    private val phoneNumberEditText: EditText

    init {
        inflate(R.layout.merge_phone_box, true)
        phoneCodeSpinner = findViewById<Spinner>(R.id.phone_code_spinner)
        phoneNumberEditText = findViewById<EditText>(R.id.phone_number_edit_text)

        val allEntries = createPhoneCodeEntries()
        phoneCodeSpinner.adapter = PhoneCodeAdapter(context, allEntries.first)
        phoneCodeSpinner.setSelection(allEntries.second)
        phoneNumberEditText.addTextChangedListener(phoneNumberWatcher)
    }

    /**
     * Return all phone code entries and the position of entry matched with device's locale settings
     */
    private fun createPhoneCodeEntries(): Pair<List<PhoneCodeEntry>, Int> {
        val phoneCodeMap = PhoneNumberHelper.createCountryCodeToRegionCodeMap()
        val phoneCodeEntries = mutableListOf<PhoneCodeEntry>()
        val availableLocales = Locale.getAvailableLocales()
        val defaultLocale = Locale.getDefault()
        var selectedIndex = -1
        for (index in 0 until phoneCodeMap.size()) {
            val phoneCode = phoneCodeMap.keyAt(index)
            phoneCodeMap.valueAt(index)
                .forEach { country ->
                    availableLocales.firstOrNull { it.country.equals(country, true) }
                        ?.let { locale ->
                            val phoneEntry =
                                PhoneCodeEntry(locale.country, locale.displayCountry, phoneCode)
                            phoneCodeEntries.add(phoneEntry)
                            Timber.d("phoneEntry = ${phoneEntry.displayCountry}")

                            if (selectedIndex == -1 && phoneEntry.country.equals(
                                    defaultLocale.country,
                                    true
                                )
                            ) {
                                selectedIndex = phoneCodeEntries.size - 1
                            }
                        }
                }
        }

        return Pair(phoneCodeEntries, selectedIndex)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        phoneCodeSpinner.isEnabled = enabled
        phoneNumberEditText.isEnabled = enabled
    }

    /**
     * @param numberWithCode number in format +PhoneCode_PhoneNumber
     */
    fun setFullNumber(numberWithCode: String?) {
        numberWithCode ?: return

        val unwrappedNumber = if (numberWithCode.startsWith("+"))
            numberWithCode.removePrefix("+")
        else
            numberWithCode

        for (index in 0 until phoneCodeSpinner.count) {
            val phoneCodeEntry = phoneCodeSpinner.getItemAtPosition(index) as PhoneCodeEntry
            val phoneCodeString = phoneCodeEntry.code.toString()
            if (unwrappedNumber.startsWith(phoneCodeString)) {
                phoneCodeSpinner.setSelection(index)
                phoneNumberEditText.setText(unwrappedNumber.removePrefix(phoneCodeString))
                return
            }
        }
    }

    /**
     * Return number value in format +PhoneCode_PhoneNumber
     */
    fun getFullNumber(): String? {
        return if (getPhoneNumber().isEmpty())
            null
        else "+${getPhoneCodeEntry().code}${getPhoneNumber()}"
    }

    private fun getPhoneCodeEntry() = phoneCodeSpinner.selectedItem as PhoneCodeEntry

    fun getPhoneCode() = getPhoneCodeEntry().code

    fun getPhoneNumber() = phoneNumberEditText.text.trim()

    interface EventListener {
        fun onPhoneBoxValueChanged(value: String?)
    }

    class PhoneCodeAdapter(
        context: Context,
        entries: List<PhoneCodeEntry>
    ) : ArrayAdapter<PhoneCodeEntry>(context, R.layout.item_selected_phone_code, entries) {

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val itemView = convertView
                ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_dropdown_phone_code, parent, false)

            val viewHolder = itemView.tag as? PhoneCodeViewHolder
                ?: PhoneCodeViewHolder(
                    itemView.findViewById(R.id.country_text_view),
                    itemView.findViewById(R.id.phone_code_text_view),
                )

            val phoneEntry = getItem(position)
            viewHolder.countryTextView.text = phoneEntry?.displayCountry
            viewHolder.phoneCodeTextView.text = phoneEntry?.toString()

            Timber.d("dropdown phoneEntry = ${phoneEntry?.displayCountry}")

            return itemView
        }
    }

    class PhoneCodeViewHolder(
        val countryTextView: TextView,
        val phoneCodeTextView: TextView
    )

    class PhoneCodeEntry(
        val country: String,
        val displayCountry: String,
        val code: Int
    ) {
        /**
         * For displaying the selected item text of spinner
         */
        override fun toString(): String {
            return "+$code"
        }
    }
}
