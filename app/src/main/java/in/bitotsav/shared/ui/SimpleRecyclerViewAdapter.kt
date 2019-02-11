package `in`.bitotsav.shared.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SimpleRecyclerViewAdapter<T : SimpleRecyclerViewAdapter.SimpleItem>(
    private val inflatingBlock: (LayoutInflater, ViewGroup, Boolean) -> ViewDataBinding,
    private val bindingBlock: (ViewDataBinding, T) -> Unit
) : ListAdapter<T, SimpleRecyclerViewAdapter.ViewHolder<T>>(DiffCallback<T>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        return ViewHolder(
            inflatingBlock.invoke(LayoutInflater.from(parent.context), parent, false),
            bindingBlock
        )
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder<T>(
        private val binding: ViewDataBinding,
        private val bindingBlock: (ViewDataBinding, T) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: T) {
            bindingBlock.invoke(binding, item)
        }
    }

    class DiffCallback<T : SimpleItem> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
            oldItem.getUniqueIdentifier() == newItem.getUniqueIdentifier()

        override fun areContentsTheSame(oldItem: T, newItem: T):
                Boolean =
            oldItem == newItem
    }

    abstract class SimpleItem {
        abstract fun getUniqueIdentifier(): String
        abstract override fun equals(other: Any?): Boolean
        abstract override fun hashCode(): Int
    }
}