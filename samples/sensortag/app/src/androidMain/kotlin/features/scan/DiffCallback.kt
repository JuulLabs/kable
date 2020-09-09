package com.juul.sensortag.features.scan

import androidx.recyclerview.widget.DiffUtil
import com.juul.kable.Advertisement

class DiffCallback(
    private val oldAdvertisements: List<Advertisement>,
    private val newAdvertisements: List<Advertisement>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldAdvertisements.size

    override fun getNewListSize(): Int = newAdvertisements.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean = oldAdvertisements[oldItemPosition].address == newAdvertisements[newItemPosition].address

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean = oldAdvertisements[oldItemPosition].rssi == newAdvertisements[newItemPosition].rssi
}
