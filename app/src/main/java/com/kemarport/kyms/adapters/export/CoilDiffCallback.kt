package com.kemarport.kyms.adapters.export

import androidx.recyclerview.widget.DiffUtil
import com.kemarport.kyms.models.export.CoilData.CoilData

class CoilDiffCallback(
    private val oldList: List<CoilData>,
    private val newList: List<CoilData>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].batchNo == newList[newItemPosition].batchNo
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}