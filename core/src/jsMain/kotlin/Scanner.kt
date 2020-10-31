package com.juul.kable

import kotlinx.coroutines.flow.Flow

/**
 * Only available on Chrome 79+ with "Experimental Web Platform features" enabled via:
 * chrome://flags/#enable-experimental-web-platform-features
 *
 * See also: [Chrome Platform Status: Web Bluetooth Scanning](https://www.chromestatus.com/feature/5346724402954240)
 */
public class JsScanner : Scanner {

    public override val peripherals: Flow<Advertisement> = TODO()
}
