package com.kemarport.kyms.adapters.export

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kemarport.kyms.R
import com.kemarport.kyms.databinding.ItemEdiBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.models.export.EDI.EdiResponseItem
import java.text.SimpleDateFormat
import java.util.*

class EdiAdapter : RecyclerView.Adapter<EdiAdapter.EdiViewHolder>() {

    inner class EdiViewHolder(val binding: ItemEdiBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<EdiResponseItem>() {
        override fun areItemsTheSame(oldItem: EdiResponseItem, newItem: EdiResponseItem): Boolean {
            return oldItem.jobId == newItem.jobId
        }
        override fun areContentsTheSame(
            oldItem: EdiResponseItem,
            newItem: EdiResponseItem
        ): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EdiViewHolder {
        val binding =
            ItemEdiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EdiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EdiViewHolder, position: Int) {
        val ediResponseItem = differ.currentList[position]
        holder.binding.apply {
            tvRakeCodeValue.text = ediResponseItem.rakeRefNo
            if (ediResponseItem.transportMode == Constants.MODE_TRAIN) {
                ivRakeCode.setImageResource(R.drawable.rake_icon)
            } else if (ediResponseItem.transportMode == Constants.MODE_TRUCK) {
                ivRakeCode.setImageResource(R.drawable.truck_icon)
            }
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val output: String = formatter.format(parser.parse(ediResponseItem.createdDate))
            tvDateValue.text = output
            var jobStatus = ""
            when (ediResponseItem.jobStatus) {
                "Active" -> {
                    jobStatus = "Active"
                }
                "Unloading Begin" -> {
                    jobStatus = "In-progress"
                }
                "Unloading Completed" -> {
                    jobStatus = "Completed"
                }
                "Pending" -> {
                    jobStatus = "Pending"
                }
                "Schedule" -> {
                    jobStatus = "Scheduled"
                }
                else -> {
                    jobStatus = "Unknown"
                }
            }
            tvJobStatusValue.text = jobStatus
            val remaining = ediResponseItem.unloadedRecord
            val total = ediResponseItem.pendingUnloadingRecord
            tvJobStatus.text = "Status ($remaining/$total): "
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

    private var onItemClickListener: ((EdiResponseItem) -> Unit)? = null

    fun setOnItemClickListener(listener: (EdiResponseItem) -> Unit) {
        onItemClickListener = listener
    }
}