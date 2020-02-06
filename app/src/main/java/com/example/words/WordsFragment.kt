package com.example.words


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ItemTouchHelper.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_word_item_card.view.*
import kotlinx.android.synthetic.main.fragment_words.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onQueryTextListener

/**
 * A simple [Fragment] subclass.
 */
class WordsFragment : Fragment() {
    var mSharedPreferences: SharedPreferences? = null
    var mNavController: NavController? = null
    var mWordViewModel: WordViewModel? = null
    var mWordAdapter: WordAdapter? = null
    //var mLiveWords: LiveData<List<Word>>? = null
    var mLiveWords: LiveData<PagedList<Word>>? = null
    var mLiveIsUseCard = MutableLiveData<Boolean>(null)
    var mDividerItemDecoration: DividerItemDecoration? = null
    // undo操作，不要滚动
    var mUndoNoScroll = false

    init {
        Log.d ("FLQ", "WordsFragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_words, container, false)
    }

    fun initVarOnce() {
        mSharedPreferences = mSharedPreferences?: activity!!.getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE)

        // must after ac created
        mNavController = mNavController?: findNavController()

        mWordViewModel = mWordViewModel?: ViewModelProvider(activity!!)[WordViewModel::class.java]
        mLiveWords = mLiveWords?: mWordViewModel!!.mLiveWords

        mLiveIsUseCard.value = mLiveIsUseCard.value?: mSharedPreferences!!.getBoolean(getString(R.string.pref_is_use_card), true)

        mWordAdapter = mWordAdapter?: WordAdapter(mWordViewModel!!, getItemLayout(mLiveIsUseCard.value))
        mDividerItemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
    }

    fun getDividerItemDecoration(wordAdapter: WordAdapter?): DividerItemDecoration?{
        if (wordAdapter?.itemLayout == R.layout.fragment_word_item_normal)
            return mDividerItemDecoration
        return null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.d ("FLQ", "onAcC "+(mLiveWords==null).toString())

        initVarOnce()

        // enable action bar menu
        setHasOptionsMenu(true)

        floatingActionButton.onClick {
            mNavController!!.navigate(R.id.action_wordsFragment_to_addFragment)
        }

        recyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mWordAdapter
            getDividerItemDecoration(mWordAdapter)?.let { addItemDecoration(it) }
            itemAnimator = object : DefaultItemAnimator() {
                override fun onAnimationFinished(viewHolder: RecyclerView.ViewHolder) {
                    super.onAnimationFinished(viewHolder)

                    val linearLayoutManager = recyclerview.layoutManager
                    if (linearLayoutManager is LinearLayoutManager)
                        for (i: Int in linearLayoutManager.findFirstVisibleItemPosition()..linearLayoutManager.findLastVisibleItemPosition())
                            recyclerview.findViewHolderForAdapterPosition(i)?.itemView?.tvWordID?.text = (i+1).toString()
                }
            }
        }

        val pagedListCallback = object: PagedList.Callback() {
            override fun onChanged(position: Int, count: Int) {
                Log.d("FLQ", "pagelist onchange: " + position.toString() + " " + count.toString())
            }

            override fun onInserted(position: Int, count: Int) { }
            override fun onRemoved(position: Int, count: Int) { }
        }

        // 因为会和搜索处共用mLiveWords
        if (mLiveWords!!.hasObservers())    mLiveWords?.removeObservers(viewLifecycleOwner)
        //mLiveWords?.observe(viewLifecycleOwner, Observer<List<Word>> {
        mLiveWords?.observe(viewLifecycleOwner, Observer<PagedList<Word>> {
            Log.d("FLQ", "change")
            it.addWeakCallback(null, pagedListCallback)
            changeAdapterData(mWordAdapter, it)
        })

        if (!mLiveIsUseCard.hasObservers()) {
            mLiveIsUseCard.observe(viewLifecycleOwner, Observer<Boolean> {
                Log.d("FLQ", "card")

                var itemLayout = getItemLayout(it)
                if (mWordAdapter!!.itemLayout != itemLayout) {
                    Log.d("FLQ", "card reset rv, notify")
                    mWordAdapter = WordAdapter(mWordViewModel!!, itemLayout)    // update wordAdapter
                    reSetAdapterToRV(mWordAdapter!!)
                }
            })
        }


        ItemTouchHelper(object: SimpleCallback(UP or DOWN, START or END) {
            override fun onMove( recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder ): Boolean {
                val wordFrom = mLiveWords!!.value?.get(viewHolder.adapterPosition)
                val wordTo = mLiveWords!!.value?.get(target.adapterPosition)
                if (wordFrom is Word && wordTo is Word) {
                    val temp = wordFrom.id
                    wordFrom.id = wordTo.id
                    wordTo.id = temp
                    Log.d("FLQ", "exchange")
                    mWordAdapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                    mWordAdapter?.notifyItemMoved(target.adapterPosition, viewHolder.adapterPosition)
                    mWordViewModel?.update(wordFrom, wordTo)
                }
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val word = mLiveWords!!.value?.get(viewHolder.adapterPosition)
                if (word is Word) {
                    mUndoNoScroll = true
                    mWordViewModel?.delete(word)

                    Snackbar.make(wordsFragmentCL, "Are u sure to delete word?", Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            mWordViewModel?.insert(word)
                        }.show()
                }
            }
        }).attachToRecyclerView(recyclerview)
    }


    private fun getItemLayout(isUseCard: Boolean?): Int {
        return if (isUseCard == true) R.layout.fragment_word_item_card else R.layout.fragment_word_item_normal
    }


    private fun reSetAdapterToRV(wordAdapter: WordAdapter) {
        recyclerview.apply {
            adapter = wordAdapter
            mDividerItemDecoration?.let { removeItemDecoration(it) }
            getDividerItemDecoration(mWordAdapter)?.let { addItemDecoration(it) }
        }
        changeAdapterData(wordAdapter, mLiveWords!!.value)
    }


    // 用it来更新adapter再更新
    //private fun changeAdapterData(wordAdapter: WordAdapter?, it: List<Word>?) {
    private fun changeAdapterData(wordAdapter: WordAdapter?, it: PagedList<Word>?) {
        wordAdapter!!.apply {
            Log.d ("FLQ", "notify")
            if (wordAdapter.itemCount != it?.size) {
                if (!mUndoNoScroll) {
                    mUndoNoScroll = false
                    recyclerview.smoothScrollBy(0, -resources.getDimension(R.dimen.word_item_height).toInt())
                }
                submitList(it)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.fragment_words, menu)

        val searchView: SearchView = menu.findItem(R.id.itemAppBarSearch).actionView as SearchView
        searchView.onQueryTextListener {
            onQueryTextChange {
                val text = searchView.query.trim().toString()

                // 在用新的text查找前，先把之前的释放掉
                if (mLiveWords!!.hasObservers())    mLiveWords?.removeObservers(viewLifecycleOwner)
                mLiveWords = mWordViewModel!!.select(text)
                //mLiveWords?.observe(viewLifecycleOwner, Observer<List<Word>> {
                mLiveWords?.observe(viewLifecycleOwner, Observer<PagedList<Word>> {
                    Log.d("FLQ", "change2")
                    changeAdapterData(mWordAdapter, it)
                })

                Log.d ("FLQ", text)
                true
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemClearAllData -> {
                AlertDialog.Builder(activity).apply {
                    setPositiveButton("OK") { _: DialogInterface, _: Int ->
                        mWordViewModel!!.deleteAll()
                    }
                    setNegativeButton("Cancel") { _: DialogInterface, _: Int -> }
                    setTitle("clear all data").create().show()
                }
            }

            R.id.itemSwitchViewType -> {
                val isUseCardKey = getString(R.string.pref_is_use_card)
                with(mSharedPreferences!!.edit()) {
                    mLiveIsUseCard.value = !(mLiveIsUseCard.value!!)
                    putBoolean(isUseCardKey, mLiveIsUseCard.value!!)
                    commit()
                }
            }
        }
        return true
    }

    override fun onStop() {
        Log.d ("FLQ", "stop")
        super.onStop()
    }

    override fun onDestroyView() {
        Log.d ("FLQ", "destroy view")
        super.onDestroyView()
    }
}
