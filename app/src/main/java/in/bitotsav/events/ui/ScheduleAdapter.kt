package `in`.bitotsav.events.ui

import `in`.bitotsav.databinding.ItemEventBinding
import `in`.bitotsav.events.data.Event
import `in`.bitotsav.shared.utils.executeAfter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ScheduleAdapter(
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<Event, ScheduleAdapter.ViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            lifecycleOwner
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = getItem(position)
        holder.apply {
            bind(event, createOnClickListener(event.id))
            itemView.tag = event
        }
    }

    private fun createOnClickListener(eventId: Int): View.OnClickListener {
        return View.OnClickListener {
            it.findNavController().navigate(
                ScheduleFragmentDirections.showEventDetail(eventId)
            )
        }
    }

    inner class ViewHolder(
        private val binding: ItemEventBinding,
        private val lifecycleOwner: LifecycleOwner
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event, listener: View.OnClickListener) {
            binding.executeAfter {
                this.event = event
                this.listener = listener
                lifecycleOwner = this@ViewHolder.lifecycleOwner
            }
        }
    }
}

// TODO: Can this be improved?
private class EventDiffCallback : DiffUtil.ItemCallback<Event>() {

    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}