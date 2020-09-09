package com.juul.sensortag.features.scan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.juul.sensortag.databinding.ScanItemBinding
import com.juul.kable.Advertisement
import java.lang.Long.parseLong

class ScanAdapter(
    private val listener: (Advertisement) -> Unit
) : RecyclerView.Adapter<ScanItemViewBinder>() {

    private val advertisements = mutableListOf<Advertisement>()

    fun update(newList: List<Advertisement>) {
        if (newList.isEmpty()) {
            advertisements.clear()
            notifyDataSetChanged()
        } else {
            val result = DiffUtil.calculateDiff(DiffCallback(advertisements, newList), false)
            advertisements.clear()
            advertisements.addAll(newList)
            result.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanItemViewBinder {
        val binding = ScanItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScanItemViewBinder(binding)
    }

    override fun onBindViewHolder(binder: ScanItemViewBinder, position: Int) =
        binder.bind(advertisements[position], listener)

    override fun getItemCount(): Int = advertisements.size

    override fun getItemId(position: Int): Long = advertisements[position].id
}

class ScanItemViewBinder(
    private val binding: ScanItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        advertisement: Advertisement,
        listener: (Advertisement) -> Unit
    ) = with(binding) {
        deviceName.text = advertisement.name ?: "<unknown>"
        macAddress.text = advertisement.address
        rssi.text = "${advertisement.rssi} dBm"

        root.setOnClickListener { listener.invoke(advertisement) }
    }
}

private val Advertisement.id: Long
    get() {
        require(address.isNotBlank())
        return parseLong(address.replace(":", ""), 16)
    }

