package rootheart.codes.weatherhistory.importer.records

import java.math.BigDecimal

data class HourlyMoistureRecord(
    var absoluteHumidity: BigDecimal? = null,
    var relativeHumidityPercent: BigDecimal? = null,
    var vaporPressureHectopascals: BigDecimal? = null,
    var dewPointTemperatureCentigrade: BigDecimal? = null,
    var wetBulbTemperatureCentigrade: BigDecimal? = null,
    var airPressureHectopascals: BigDecimal? = null,
    var airTemperatureAtTwoMetersHeightCentigrade: BigDecimal? = null,
) : BaseRecord()
