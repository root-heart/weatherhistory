package rootheart.codes.weatherhistory.importer

data class CloudLayer (
    val cloudType: CloudType,
    val height: Int,
    val coverage: Int
)
