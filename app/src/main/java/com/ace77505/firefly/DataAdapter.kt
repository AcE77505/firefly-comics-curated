// DataAdapter.kt
package com.ace77505.firefly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DataAdapter(
    private var dataList: List<FilterData> = emptyList(),
    private val onItemClick: (FilterData) -> Unit
) : RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, private val onItemClick: (FilterData) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

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
            tvId.text = "ID: ${data.id}"
            tvTitle.text = data.title
            tvFilter1.text = "ai: ${getFilter1DisplayValue(data.filter1)}"
            tvFilter2.text = "修正: ${getFilter2DisplayValue(data.filter2)}"
            tvFilter3.text = "全彩: ${getFilter3DisplayValue(data.filter3)}"
            tvFilter4.text = "纯爱: ${getFilter4DisplayValue(data.filter4)}"
            tvFilter5.text = "tag: ${getFilter5DisplayValue(data.filter5)}"
            tvUpdateDate.text = "更新: ${data.updateDate}"
        }

        private fun getFilter1DisplayValue(value: Int): String = when (value) {
            1 -> "是"
            0 -> "否"
            else -> "未知"
        }

        private fun getFilter2DisplayValue(value: String): String {
            return if (value.contains("+")) {
                value.split("+").joinToString("+") { part ->
                    when (part.trim()) {
                        "-1" -> "未知"
                        "0" -> "无修正"
                        "1" -> "黑线"
                        "2" -> "薄模糊"
                        "3" -> "厚模糊"
                        "4" -> "白色"
                        else -> part
                    }
                }
            } else {
                when (value) {
                    "-1" -> "未知"
                    "0" -> "无修正"
                    "1" -> "黑线"
                    "2" -> "薄模糊"
                    "3" -> "厚模糊"
                    "4" -> "白色"
                    else -> value
                }
            }
        }

        private fun getFilter3DisplayValue(value: Int): String = when (value) {
            1 -> "是"
            0 -> "否"
            else -> "未知"
        }

        private fun getFilter4DisplayValue(value: Int): String = when (value) {
            1 -> "是"
            0 -> "否"
            else -> "未知"
        }

        private fun getFilter5DisplayValue(value: String): String {
            return if (value.contains("+")) {
                value.split("+").joinToString("+") { part ->
                    when (part.trim()) {
                        "-1" -> "未知"
                        "1" -> "单人"
                        "2" -> "男主"
                        "3" -> "扶她"
                        "4" -> "多人"
                        else -> part
                    }
                }
            } else {
                when (value) {
                    "-1" -> "未知"
                    "1" -> "单人"
                    "2" -> "男主"
                    "3" -> "扶她"
                    "4" -> "多人"
                    else -> value
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data, parent, false)
        return ViewHolder(view, onItemClick)
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