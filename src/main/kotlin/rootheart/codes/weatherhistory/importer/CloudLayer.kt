package rootheart.codes.weatherhistory.importer

data class CloudLayer (
    var cloudType: CloudType? = null,
    var height: Int? = null,
    var coverage: Int? = null
)
