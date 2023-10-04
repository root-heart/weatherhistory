import {Injectable} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class MeasurementService {

}

export class MonthMeasurementsJson {
    constructor(public year: number,
                public month: number,
                public hourlyTemperatures: Array<number>,
                public hourlyDewPoint: Array<number>,
                public hourlyRainfall: Array<number>,
                public hourlySunshineDuration: Array<number>,
                public hourlyCloudCoverage: Array<number>,
                public hourlyWindDirection: Array<number>,
                public hourlyWindSpeed: Array<number>,
                public hourlyAirPressure: Array<number>) {
    }
}

export class YearMeasurementsJson {
    constructor(public year: number,
                public dailyTemperatures: Array<number>,
                public dailyDewPoint: Array<number>,
                public dailyRainfall: Array<number>,
                public dailySunshineDuration: Array<number>,
                public dailyCloudCoverage: Array<number>,
                public dailyWindDirection: Array<number>,
                public dailyWindSpeed: Array<number>,
                public dailyAirPressure: Array<number>) {
    }
}

