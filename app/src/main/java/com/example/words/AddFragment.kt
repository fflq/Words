package com.example.words


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_add.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * A simple [Fragment] subclass.
 */
class AddFragment : Fragment() {
    lateinit var navController: NavController
    lateinit var wordViewModel: WordViewModel
    lateinit var imm: InputMethodManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        navController = findNavController()
        wordViewModel = ViewModelProvider(activity!!)[WordViewModel::class.java]
        imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        btnAdd.isEnabled = false
        btnAdd.onClick {
            // like web, every display every get from db
            //(wordViewModel.liveWords.value as ArrayList<Word>).add (Word(etEnglish.text.toString(), etChinese.text.toString()))
            wordViewModel.insert(Word(etEnglish.text.toString(), etChinese.text.toString()))
            imm.hideSoftInputFromWindow(btnAdd.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            navController.navigateUp()
        }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnAdd.isEnabled = !etEnglish.text.isEmpty() && !etChinese.text.isEmpty()
            }
        }
        etEnglish.addTextChangedListener(textWatcher)
        etChinese.addTextChangedListener(textWatcher)
    }

}
