package com.kemarport.kyms.adapters.importupdate

import androidx.recyclerview.widget.DiffUtil
import com.kemarport.kyms.models.importupdate.importstocktally.importjobmaster.GetJobDetailsListItemResponse

class ImportDiffCallback(
    private val oldList: MutableList<GetJobDetailsListItemResponse>,
    private val newList: List<GetJobDetailsListItemResponse>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].batchNumber == newList[newItemPosition].batchNumber
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
}