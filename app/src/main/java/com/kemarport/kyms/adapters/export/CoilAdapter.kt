package com.kemarport.kyms.adapters.export

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kemarport.kyms.R
import com.kemarport.kyms.models.export.CoilData.CoilData

class CoilAdapter(initActorsList: List<CoilData>) : RecyclerView.Adapter<CoilAdapter.ViewHolder>() {

    private val actors = mutableListOf<CoilData>()

    init {
        actors.addAll(initActorsList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_edi_details, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = actors.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(actor = actors[position])
    }

    fun swap(actors: List<CoilData>) {
        val diffCallback = CoilDiffCallback(this.actors, actors)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.actors.clear()
        this.actors.addAll(actors)
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val name: TextView = itemView.findViewById(R.id.tvBarcode)

        fun bind(actor: CoilData) {
            name.text = actor.batchNo
        }

    }
}