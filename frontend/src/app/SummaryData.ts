export type Measurement = {
    maxTemperature?: number,
    rainfall?: number,
    maxVisibility?: number,
    snowfall?: number,
    minDewPointTemperature?: number,
    avgAirPressure?: number,
    maxAirPressure?: number,
    cloudCoverage?: number[],
    minAirPressure?: number,
    avgDewPointTemperature?: number,
    avgHumidity?: number,
    maxWindspeed?: number,
    maxDewPointTemperature?: number,
    minHumidity?: number,
    sunshineDuration?: number,
    avgWindspeed?: number,
    firstDay: Date,
    minTemperature?: number,
    minVisibility?: number,
    avgVisibility?: number,
    maxHumidity?: number,
    avgTemperature?: number

}

export type SummaryData = {
    summary: Measurement,
    details: Measurement[]
}


function abc() {
    let s: SummaryData = {
        summary: {
            firstDay: new Date(2022, 1, 1),
            cloudCoverage: [1]
        },
        details: [
            {firstDay: new Date(0)}
        ]
    }
}
