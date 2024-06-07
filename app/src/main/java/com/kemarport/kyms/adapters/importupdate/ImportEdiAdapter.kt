package com.kemarport.kyms.adapters.importupdate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kemarport.kyms.R
import com.kemarport.kyms.databinding.ItemEdiBinding
import com.kemarport.kyms.models.importupdate.importstocktally.importjobmaster.ImportJobMasterResponseList
import java.text.SimpleDateFormat

import java.util.*
import kotlin.math.roundToInt

class ImportEdiAdapter : RecyclerView.Adapter<ImportEdiAdapter.ImportEditViewHolder>() {

    inner class ImportEditViewHolder(val binding: ItemEdiBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<ImportJobMasterResponseList>() {
        override fun areItemsTheSame(oldItem:ImportJobMasterResponseList, newItem: ImportJobMasterResponseList): Boolean {
            return oldItem.importJobMasterId == newItem.importJobMasterId
        }

        override fun areContentsTheSame(
            oldItem: ImportJobMasterResponseList,
            newItem: ImportJobMasterResponseList
        ): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImportEditViewHolder {
        val binding =
            ItemEdiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImportEditViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImportEditViewHolder, position: Int) {
       // val ediResponseItem = differ.currentList[position]
        val item = differ.currentList[position]
        if (item is ImportJobMasterResponseList) {
            // Now you can safely cast
            val ediResponseItem = item
            // Rest of your code

            holder.binding.apply {
                tvDateValue.text=dateFormat(ediResponseItem.arrivalDateTime)
                tvJobStatusValue.text="Active"
                tvJobStatus.text="Status (${ediResponseItem.stockTallyCount.roundToInt()}/${ediResponseItem.totalCount.roundToInt()}):"
                tvRakeCodeValue.text = ediResponseItem.vesselName
                root.setOnClickListener {
                    onItemClickListener?.let {
                        it(ediResponseItem)
                    }
                }
                ivRakeCode.setImageResource(R.drawable.cargo_ship)
            }
        } else {
            // Handle the case where the item is not of the expected type
        }

    }
    private fun dateFormat(date:String):String
    {

        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.getDefault())
        val inputDate = inputDateFormat.parse(date)
        val outputDateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
        val outputDateStr = outputDateFormat.format(inputDate)
        return outputDateStr
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((ImportJobMasterResponseList) -> Unit)? = null

    fun setOnItemClickListener(listener: (ImportJobMasterResponseList) -> Unit) {
        onItemClickListener = listener
    }
}