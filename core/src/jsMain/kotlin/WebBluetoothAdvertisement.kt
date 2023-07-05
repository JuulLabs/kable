package com.juul.kable

import com.benasher44.uuid.Uuid
import org.khronos.webgl.DataView

public interface WebBluetoothAdvertisement : Advertisement {
    public fun serviceDataAsDataView(uuid: Uuid): DataView?
    public fun manufacturerDataAsDataView(companyIdentifierCode: Int): DataView?
}
