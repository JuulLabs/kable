# ScanResultAndroidAdvertisement is @Parcelize; the annotation is referenced in bytecode but
# kotlin-parcelize-runtime is not a dependency, so silence the R8 (full mode) missing-class check.
-dontwarn kotlinx.parcelize.Parcelize
