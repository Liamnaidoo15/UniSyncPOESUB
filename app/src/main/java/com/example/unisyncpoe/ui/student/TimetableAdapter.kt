package com.example.unisyncpoe.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.unisyncpoe.databinding.ItemTimetableDayBinding
import com.example.unisyncpoe.databinding.ItemTimetableEntryBinding
import com.example.unisyncpoe.data.model.Timetable

class TimetableAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<TimetableItem>()

    companion object {
        private const val TYPE_DAY_HEADER = 0
        private const val TYPE_TIMETABLE_ENTRY = 1
    }

    sealed class TimetableItem {
        data class DayHeader(val dayName: String) : TimetableItem()
        data class Entry(val timetable: Timetable) : TimetableItem()
    }

    fun submitTimetable(timetableMap: Map<Int, List<Timetable>>) {
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val newItems = mutableListOf<TimetableItem>()
        
        timetableMap.forEach { (day, entries) ->
            if (entries.isNotEmpty()) {
                newItems.add(TimetableItem.DayHeader(dayNames[day - 1]))
                entries.forEach { entry ->
                    newItems.add(TimetableItem.Entry(entry))
                }
            }
        }
        
        val diffCallback = DiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items.clear()
        items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TimetableItem.DayHeader -> TYPE_DAY_HEADER
            is TimetableItem.Entry -> TYPE_TIMETABLE_ENTRY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DAY_HEADER -> {
                val binding = ItemTimetableDayBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DayHeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemTimetableEntryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                EntryViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TimetableItem.DayHeader -> (holder as DayHeaderViewHolder).bind(item.dayName)
            is TimetableItem.Entry -> (holder as EntryViewHolder).bind(item.timetable)
        }
    }

    override fun getItemCount() = items.size

    class DayHeaderViewHolder(
        private val binding: ItemTimetableDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dayName: String) {
            binding.tvDayName.text = dayName
        }
    }

    class EntryViewHolder(
        private val binding: ItemTimetableEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(timetable: Timetable) {
            binding.tvCourseName.text = timetable.courseName
            binding.tvTime.text = "${timetable.startTime} - ${timetable.endTime}"
            binding.tvVenue.text = "${timetable.venue} ${timetable.roomNumber ?: ""}".trim()
            binding.tvLecturer.text = timetable.lecturerName
        }
    }

    class DiffCallback(
        private val oldList: List<TimetableItem>,
        private val newList: List<TimetableItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            val oldItem = oldList[oldPos]
            val newItem = newList[newPos]
            return when {
                oldItem is TimetableItem.DayHeader && newItem is TimetableItem.DayHeader ->
                    oldItem.dayName == newItem.dayName
                oldItem is TimetableItem.Entry && newItem is TimetableItem.Entry ->
                    oldItem.timetable.id == newItem.timetable.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}

