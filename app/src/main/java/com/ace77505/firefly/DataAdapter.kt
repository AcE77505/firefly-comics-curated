// DataAdapter.kt
package com.ace77505.firefly

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DataAdapter(
    private var dataList: List<FilterData> = emptyList(),
    private val onItemClick: (FilterData) -> Unit
) : RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    class ViewHolder(
        itemView: View,
        private val onItemClick: (FilterData) -> Unit,
        private val context: Context
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvId: TextView = itemView.findViewById(R.id.tvId)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvFilter1: TextView = itemView.findViewById(R.id.tvFilter1)
        private val tvFilter2: TextView = itemView.findViewById(R.id.tvFilter2)
        private val tvFilter3: TextView = itemView.findViewById(R.id.tvFilter3)
        private val tvFilter4: TextView = itemView.findViewById(R.id.tvFilter4)
        private val tvFilter5: TextView = itemView.findViewById(R.id.tvFilter5)
        private val tvUpdateDate: TextView = itemView.findViewById(R.id.tvUpdateDate)

        private var currentData: FilterData? = null

        init {
            itemView.setOnClickListener {
                currentData?.let { data ->
                    onItemClick(data)
                }
            }
        }

        fun bind(data: FilterData) {
            currentData = data
            tvId.text = data.id
            tvTitle.text = data.title
            tvFilter1.text = context.getString(R.string.ai_format, getFilter1DisplayValue(data.filter1))
            tvFilter2.text = context.getString(R.string.censored_format, getFilter2DisplayValue(data.filter2))
            tvFilter3.text = context.getString(R.string.full_color_format, getFilter3DisplayValue(data.filter3))
            tvFilter4.text = context.getString(R.string.chunai_format, getFilter4DisplayValue(data.filter4))
            tvFilter5.text = context.getString(R.string.tag_format, getFilter5DisplayValue(data.filter5))
            tvUpdateDate.text = data.updateDate
        }

        private fun getFilter1DisplayValue(value: Int): String = when (value) {
            1 -> context.getString(R.string.yes)
            0 -> context.getString(R.string.no)
            else -> context.getString(R.string.unknown)
        }

        private fun getFilter2DisplayValue(value: String): String {
            return if (value.contains("+")) {
                value.split("+").joinToString("+") { part ->
                    when (part.trim()) {
                        "-1" -> context.getString(R.string.unknown)
                        "0" -> context.getString(R.string.uncensored)
                        "1" -> context.getString(R.string.c_black_lines)
                        "2" -> context.getString(R.string.c_thin_blur)
                        "3" -> context.getString(R.string.c_thick_blur)
                        "4" -> context.getString(R.string.white)
                        else -> part
                    }
                }
            } else {
                when (value) {
                    "-1" -> context.getString(R.string.unknown)
                    "0" -> context.getString(R.string.uncensored)
                    "1" -> context.getString(R.string.c_black_lines)
                    "2" -> context.getString(R.string.c_thin_blur)
                    "3" -> context.getString(R.string.c_thick_blur)
                    "4" -> context.getString(R.string.white)
                    else -> value
                }
            }
        }

        private fun getFilter3DisplayValue(value: Int): String = when (value) {
            1 -> context.getString(R.string.yes)
            0 -> context.getString(R.string.no)
            else -> context.getString(R.string.unknown)
        }

        private fun getFilter4DisplayValue(value: Int): String = when (value) {
            1 -> context.getString(R.string.yes)
            0 -> context.getString(R.string.no)
            else -> context.getString(R.string.unknown)
        }

        private fun getFilter5DisplayValue(value: String): String {
            return if (value.contains("+")) {
                value.split("+").joinToString("+") { part ->
                    when (part.trim()) {
                        "-1" -> context.getString(R.string.unknown)
                        "1" -> context.getString(R.string.tag_1p)
                        "2" -> context.getString(R.string.tag_cealus)
                        "3" -> context.getString(R.string.tag_futa)
                        "4" -> context.getString(R.string.tag_3p_or_more)
                        else -> part
                    }
                }
            } else {
                when (value) {
                    "-1" -> context.getString(R.string.unknown)
                    "1" -> context.getString(R.string.tag_1p)
                    "2" -> context.getString(R.string.tag_cealus)
                    "3" -> context.getString(R.string.tag_futa)
                    "4" -> context.getString(R.string.tag_3p_or_more)
                    else -> value
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data, parent, false)
        return ViewHolder(view, onItemClick, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    fun updateData(newData: List<FilterData>) {
        dataList = newData
        notifyDataSetChanged()
    }
}
