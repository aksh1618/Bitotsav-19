package `in`.bitotsav.events.ui

import `in`.bitotsav.databinding.ItemMemberDetailsBinding
import `in`.bitotsav.events.data.EventRegistrationMember
import `in`.bitotsav.shared.utils.executeAfter
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class EventRegistrationAdapter(
    val lifecycleOwner: LifecycleOwner,
    val viewModel: EventViewModel
) : ListAdapter<EventRegistrationMember, EventRegistrationAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMemberDetailsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            lifecycleOwner,
            viewModel
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemMemberDetailsBinding,
        val lifecycleOwner: LifecycleOwner,
        val viewModel: EventViewModel
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(eventRegistrationMember: EventRegistrationMember) {
            binding.executeAfter {
                viewModel = this@ViewHolder.viewModel
                this.member = eventRegistrationMember
                // Need to set this *after* setting the viewModel, causes crash otherwise
                lifecycleOwner = this@ViewHolder.lifecycleOwner
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<EventRegistrationMember>() {
        override fun areItemsTheSame(oldItem: EventRegistrationMember, newItem: EventRegistrationMember): Boolean =
            oldItem.index == newItem.index

        override fun areContentsTheSame(oldItem: EventRegistrationMember, newItem: EventRegistrationMember): Boolean =
            oldItem.bitotsavId.text.value == newItem.bitotsavId.text.value
    }
}


