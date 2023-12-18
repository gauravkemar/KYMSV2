package com.kemarport.kyms.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kemarport.kyms.R
import com.kemarport.kyms.databinding.ItemEdiDetailsBinding
import com.kemarport.kyms.models.CoilData.CoilData

class EdiDetailsAdapter : RecyclerView.Adapter<EdiDetailsAdapter.EdiDetailsViewHolder>() {
    private val TAG = "EdiDetailsAdapter"
    private val scannedCoilDataList = mutableListOf<CoilData>()

    inner class EdiDetailsViewHolder(val binding: ItemEdiDetailsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EdiDetailsViewHolder {
        val binding =
            ItemEdiDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EdiDetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EdiDetailsViewHolder, position: Int) {
        val scannedCoilDataItem = scannedCoilDataList[position]
        holder.binding.apply {
            tvCount.text = "${position + 1}."
            tvBarcodeValue.text = scannedCoilDataItem.batchNo
            tvShipToValue.text = scannedCoilDataItem.shipToPartyName
            tvJSWGradeValue.text = scannedCoilDataItem.jswGrade
            tvRakeRefNoValue.text = scannedCoilDataItem.rakeRefNo
//            tvProductStatusValue.text = scannedCoilDataItem.status
            if (scannedCoilDataItem.status == "Pending") {
                cv1.setCardBackgroundColor(
                    ContextCompat.getColor(
                        root.context,
                        R.color.md_theme_secondaryContainer
                    )
                )
            } else if (scannedCoilDataItem.status == "Unloaded") {
                cv1.setCardBackgroundColor(
                    ContextCompat.getColor(
                        root.context,
                        R.color.white
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return scannedCoilDataList.size
    }

    fun updateCoilDataList(scannedCoilDataList: List<CoilData>) {
        val diffCallback = CoilDiffCallback(this.scannedCoilDataList, scannedCoilDataList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.scannedCoilDataList.clear()
        this.scannedCoilDataList.addAll(scannedCoilDataList)
        diffResult.dispatchUpdatesTo(this)
        Log.d(TAG, "updateCoilDataList: ${scannedCoilDataList.size}")
    }
}