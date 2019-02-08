package `in`.bitotsav.teams.ui

import `in`.bitotsav.databinding.ItemMemberDetailsBinding
import `in`.bitotsav.shared.ui.BaseViewModel
import `in`.bitotsav.shared.utils.executeAfter
import `in`.bitotsav.teams.data.RegistrationMember
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TeamRegistrationAdapter(
    val lifecycleOwner: LifecycleOwner,
    val viewModel: BaseViewModel
) : ListAdapter<RegistrationMember, TeamRegistrationAdapter.ViewHolder>(
    DiffCallback()
) {

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
        val viewModel: BaseViewModel
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(registrationMember: RegistrationMember) {
            binding.executeAfter {
                color = this@ViewHolder.viewModel.mColor
                this.member = registrationMember
                // Need to set this *after* setting the viewModel, causes crash otherwise
                lifecycleOwner = this@ViewHolder.lifecycleOwner
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RegistrationMember>() {
        override fun areItemsTheSame(oldItem: RegistrationMember, newItem: RegistrationMember): Boolean =
            oldItem.index == newItem.index

        override fun areContentsTheSame(oldItem: RegistrationMember, newItem: RegistrationMember): Boolean =
            oldItem.bitotsavId.text.value == newItem.bitotsavId.text.value
    }
}


