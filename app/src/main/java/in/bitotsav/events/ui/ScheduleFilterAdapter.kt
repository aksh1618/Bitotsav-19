package `in`.bitotsav.events.ui

import `in`.bitotsav.databinding.ItemScheduleFilterBinding
import `in`.bitotsav.shared.utils.executeAfter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableBoolean
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ScheduleFilterAdapter(val viewModel: ScheduleViewModel) :
    ListAdapter<ScheduleFilter, ScheduleFilterAdapter.ViewHolder>(ScheduleFilterDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemScheduleFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                .apply {
                    viewModel = this@ScheduleFilterAdapter.viewModel
                }
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemScheduleFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ScheduleFilter) {
            binding.executeAfter {
                scheduleFilter = item
            }
        }
    }
}

class ScheduleFilterDiffCallback : DiffUtil.ItemCallback<ScheduleFilter>() {
    override fun areItemsTheSame(oldItem: ScheduleFilter, newItem: ScheduleFilter): Boolean =
        oldItem.label == newItem.label

    override fun areContentsTheSame(oldItem: ScheduleFilter, newItem: ScheduleFilter): Boolean {
        return oldItem.isSelected.get() == newItem.isSelected.get()
    }
}

class ScheduleFilter(val label: String, val color: Int, isSelected: Boolean) {
    val isSelected = ObservableBoolean(isSelected)
}