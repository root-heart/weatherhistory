package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.model.CloudType

data class CloudLayer (
    var cloudType: CloudType? = null,
    var height: Int? = null,
    var coverage: Int? = null
)
