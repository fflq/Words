package com.example.words

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_word_item_normal.view.*
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange

/*
class WordAdapter(var wordViewModel: WordViewModel, var itemLayout: Int = R.layout.fragment_word_item_card)
    : RecyclerView.Adapter<WordAdapter.MyViewHolder>() {
 */
/*
class WordAdapter(var wordViewModel: WordViewModel, var itemLayout: Int = R.layout.fragment_word_item_card,
                  diffCallback: DiffUtil.ItemCallback<Word> = MyItemCallback())
    : ListAdapter<Word, WordAdapter.MyViewHolder>(diffCallback) {
*/
class WordAdapter(var wordViewModel: WordViewModel, var itemLayout: Int = R.layout.fragment_word_item_card,
                  diffCallback: DiffUtil.ItemCallback<Word> = MyItemCallback())
    : PagedListAdapter<Word, WordAdapter.MyViewHolder>(diffCallback) {

    // livewords.value always is null at begain
    //var liveWords = wordViewModel.liveWords
    // 因为adapter直接使用的livedata，所以lastSize用于记录更新前的上次行数
    //var lastSize = itemCount

    //var words = wordViewModel.liveWords.value
    //var words = ArrayList<Word>()

    var clickedByCode = false

    init {
        //Log.d ("FLQ", "WordAdapter" + words?.size)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        Log.d("FLQ", "create")
        var itemView = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
        var holder = MyViewHolder(itemView)

        itemView.swWordChineseInvisible.onCheckedChange { buttonView, isChecked ->
            // 确保是点击button，而不是code修改触发的，不然会死循环（因bind中有code修改）
            if (!(buttonView?.getTag(R.id.is_click_by_code) as Boolean)) {
                var word = itemView.getTag(R.id.viewholder_word_id) as Word
                if (word!! is Word) {
                    Log.d("FLQ", "click " + word.id.toString())
                    itemView.tvWordChinese.visibility = (if (isChecked) View.GONE else View.VISIBLE)
                    word.isChineseInvisible = isChecked
                    wordViewModel.update(word)
                    //wordViewModel.update(Word(40, count.toString(), count++.toString()))
                }
            }
            buttonView.setTag(R.id.is_click_by_code, false)
        }

        return holder
    }
    var count = 0

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d("FLQ", "bind")
        //var word = words?.get(position)
        var word = getItem(position)
        if (word is Word) {
            holder.itemView.setTag(R.id.viewholder_word_id, word)
            //holder.itemView.tvWordID.text = word.id.toString()
            holder.itemView.tvWordID.text = (position+1).toString()
            holder.itemView.tvWordEnglish.text = word.english
            holder.itemView.tvWordChinese.text = word.chinese
            // 在switch的lisenter中区分是用户click，还是code修改触发
            holder.itemView.swWordChineseInvisible.setTag(R.id.is_click_by_code, false)

            var visibility = View.VISIBLE
            var isChecked = false
            if (word.isChineseInvisible) {
                visibility = View.GONE
                isChecked = true
            }
            holder.itemView.tvWordChinese.visibility = visibility

            // 是否code修改过checked，然后触发了click
            if (holder.itemView.swWordChineseInvisible.isChecked != isChecked) {
                holder.itemView.swWordChineseInvisible.isChecked = isChecked
                holder.itemView.swWordChineseInvisible.setTag(R.id.is_click_by_code, true)
            }
        }
        else    holder.itemView.tvWordEnglish.text = "Loading ..."
    }

    /*
    override fun onViewAttachedToWindow(holder: MyViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.tvWordID.text = (holder.adapterPosition+1).toString()
    }
     */

    /*
    override fun getItemCount(): Int {
        // may db is empty
        return words?.size ?: 0
    }
     */
}


class MyItemCallback: DiffUtil.ItemCallback<Word>() {
    override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
        return (oldItem.id == newItem.id)
    }

    override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
        return ( (oldItem.english == newItem.english)
                && (oldItem.chinese == newItem.chinese))
                //&& (oldItem.isChineseInvisible == newItem.isChineseInvisible))
                // 开关修改自行处理，不算作区别
    }
}

