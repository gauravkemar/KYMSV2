package com.kemarport.kyms.adapters.importupdate

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kemarport.kyms.databinding.ItemImportEdiDetailsBinding
import com.kemarport.kyms.models.importupdate.importstocktally.importjobmaster.GetJobDetailsListItemResponse

class ImportEdiDetailsAdapter : RecyclerView.Adapter<ImportEdiDetailsAdapter.ImportEditDetailsViewHolder>() {
    private val getJobDetailsListItemResponse = mutableListOf<GetJobDetailsListItemResponse>()
    inner class ImportEditDetailsViewHolder(val binding: ItemImportEdiDetailsBinding) :
        RecyclerView.ViewHolder(binding.root)

    /*private val differCallback = object : DiffUtil.ItemCallback<GetJobDetailsListItemResponse>() {
        override fun areItemsTheSame(oldItem: GetJobDetailsListItemResponse, newItem: GetJobDetailsListItemResponse): Boolean {
            return oldItem.batchNumber== newItem.batchNumber
        }

        override fun areContentsTheSame(
            oldItem: GetJobDetailsListItemResponse,
            newItem: GetJobDetailsListItemResponse
        ): Boolean {
            return oldItem == newItem
        }

    }*/

    //val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImportEditDetailsViewHolder {
        val binding =
            ItemImportEdiDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImportEditDetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImportEditDetailsViewHolder, position: Int) {
       // val ediResponseItem = differ.currentList[position]
        //val item = differ.currentList[position]
        val item=getJobDetailsListItemResponse[position]
        if (item is GetJobDetailsListItemResponse) {
            // Now you can safely cast
            val ediResponseItem = item
            // Rest of your code

            holder.binding.apply {
                tvBatchNoValue.text = ediResponseItem.batchNumber
                tvCount.text = "${position + 1}."
            }
        } else {
            // Handle the case where the item is not of the expected type
        }

    }
    fun updateImportDataList(getJobDetailsListItemResponse: List<GetJobDetailsListItemResponse>) {
        val diffCallback = ImportDiffCallback(this.getJobDetailsListItemResponse, getJobDetailsListItemResponse)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.getJobDetailsListItemResponse.clear()
        this.getJobDetailsListItemResponse.addAll(getJobDetailsListItemResponse)
        diffResult.dispatchUpdatesTo(this)
        Log.d("TAG", "updateCoilDataList: ${getJobDetailsListItemResponse.size}")
    }
    override fun getItemCount(): Int {
        return getJobDetailsListItemResponse.size
    }

    private var onItemClickListener: ((GetJobDetailsListItemResponse) -> Unit)? = null

    fun setOnItemClickListener(listener: (GetJobDetailsListItemResponse) -> Unit) {
        onItemClickListener = listener
    }
}