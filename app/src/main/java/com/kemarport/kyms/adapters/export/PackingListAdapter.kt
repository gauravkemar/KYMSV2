package com.kemarport.kyms.adapters.export

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kemarport.kyms.R
import com.kemarport.kyms.databinding.ItemPackingListBinding
import com.kemarport.kyms.models.export.packingList.PackingListResponseItem

class PackingListAdapter : RecyclerView.Adapter<PackingListAdapter.PackingListViewHolder>() {

    inner class PackingListViewHolder(val binding: ItemPackingListBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<PackingListResponseItem>() {
        override fun areItemsTheSame(
            oldItem: PackingListResponseItem,
            newItem: PackingListResponseItem
        ): Boolean {
            return oldItem.packingId == newItem.packingId
        }

        override fun areContentsTheSame(
            oldItem: PackingListResponseItem,
            newItem: PackingListResponseItem
        ): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackingListViewHolder {
        val binding =
            ItemPackingListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PackingListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PackingListViewHolder, position: Int) {
        val ediResponseItem = differ.currentList[position]
        holder.binding.apply {
            tvVesselName.text = ediResponseItem.fileName.trim()
            var jobStatus = ""
            when (ediResponseItem.status) {
                "Loading" -> {
                    jobStatus = "Loading In-progress"
                }
                "Completed" -> {
                    jobStatus = "Loading Completed"
                }
                "Pending" -> {
                    jobStatus = "Pending"
                }
                else -> {
                    jobStatus = "Unknown"
                }
            }
            tvJobStatusValue.text = jobStatus
            tvUnloadingValue.text = ediResponseItem.unloadingatBirth
            val remaining = ediResponseItem.dispatchedRecord
            val total = ediResponseItem.totalRecord
            tvJobStatus.text = "Status ($remaining/$total) : "
            ivRakeCode.setImageResource(R.drawable.ic_vessel)
            root.setOnClickListener {
                onItemClickListener?.let {
                    it(ediResponseItem)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((PackingListResponseItem) -> Unit)? = null

    fun setOnItemClickListener(listener: (PackingListResponseItem) -> Unit) {
        onItemClickListener = listener
    }
}