import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {FilterService} from "../filter.service";
import {MeasurementTypes} from "../app.component";
import {
    faCalendarWeek,
    faCloud,
    faCloudShowersHeavy,
    faCloudSun,
    faSnowflake,
    faSquare,
    faSquareXmark,
    faSun
} from '@fortawesome/free-solid-svg-icons';
import * as Highcharts from 'highcharts';

import addMore from "highcharts/highcharts-more";
import {getDateLabel} from "../charts/charts";
import {formatDate, registerLocaleData} from "@angular/common";
import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';
import {DomUtil} from "leaflet";
import get = DomUtil.get;

addMore(Highcharts);
registerLocaleData(localeDe, 'de-DE', localeDeExtra);

@Component({
    selector: 'station-charts',
    templateUrl: './station-charts.component.html',
    styleUrls: ['./station-charts.component.scss'],
    // encapsulation: ViewEncapsulation.None
})
export class StationChartsComponent {
    Highcharts: typeof Highcharts = Highcharts
    bigChart?: Highcharts.Chart
    cloudinessChart?: Highcharts.Chart;
    windDirectionChart?: Highcharts.Chart;
    windDirectionChartOptions: Highcharts.Options = {
        chart: {styledMode: true, animation: false, zooming: {mouseWheel: {enabled: true}, type: "x"}},
        boost: {
            useGPUTranslations: true,
            // usePreAllocated: true
        },
        title: {text: undefined},
        xAxis: {
            type: 'datetime',
            labels: {
                formatter: v => new Date(v.value).toLocaleDateString('de-DE', {month: "short"})
            },
        },
        yAxis: [
            {title: {text: undefined}, min: 0, max: 360, tickInterval: 90, minorTickInterval: 45}
        ],
        tooltip: {
            shared: true,
            xDateFormat: "%d.%m.%Y",
            animation: false
        },
        plotOptions: {
            line: {animation: false},
            arearange: {animation: false},
            column: {animation: false},
            scatter: {marker: {symbol: 'circle'}}
        },
        legend: {
            enabled: false
        }
    }
    faSun = faSun
    faCloudSun = faCloudSun
    faCloud = faCloud
    faRain = faCloudShowersHeavy
    faSnow = faSnowflake
    faSquare = faSquare
    faSquareChecked = faSquareXmark
    faCalendar = faCalendarWeek
    measurementType?: MeasurementTypes

    private readonly airTemperatureAxisId = "temp";
    private readonly sunshineAxisId = "sunshine";
    private readonly precipitationAxisId = "precipitation";
    private readonly windSpeedAxisId = "windSpeed";
    private readonly windDirectionAxisId = "windDirection";
    private readonly humidityAxisId = "humidity";
    private readonly dewpointAxisId = "dewpoint";
    private readonly visibilityAxisId = "visibility";
    private readonly airPressureAxisId = "airPressure";
    private readonly cloudCoverageAxisId = "cloudCoverage";
    chartOptions: Highcharts.Options = {

        boost: {
            useGPUTranslations: true,
            // usePreAllocated: true
        },
        chart: {
            styledMode: true,
            animation: false,
            zooming: {mouseWheel: {enabled: true}, type: "x"},
        },
        // legend: { enabled: false},
        plotOptions: {
            line: {animation: false},
            arearange: {animation: false, states: {hover: {enabled: false}}},
            column: {animation: false},
            columnrange: {animation: false},
        },
        series: [],
        title: {text: undefined},
        tooltip: {
            // split: true,
            shared: true,
            xDateFormat: "%d.%m.%Y",
            animation: false,
            followPointer: true,
        },
        xAxis: {
            crosshair: true,
            type: 'datetime',
            labels: {
                formatter: v => new Date(v.value).toLocaleDateString('de-DE', {month: "short"})
            },
        },
        yAxis: [
            {
                id: this.airTemperatureAxisId,
                offset: 0,
                top: "0%",
                height: "18%",
                title: {text: "Temperatur °C"},
                reversedStacks: false,
                min: 0,
                softMax: 40
            },
            {
                id: this.precipitationAxisId,
                offset: 0,
                top: "0%",
                height: "18%",
                title: {text: "Niederschlag mm"},
                opposite: true,
                reversedStacks: false
            },

            {
                id: this.sunshineAxisId,
                offset: 0,
                top: "20%",
                height: "18%",
                title: {text: "Sonnenschein h"},
                reversedStacks: false
            },
            {
                id: this.cloudCoverageAxisId,
                offset: 0,
                top: "20%",
                height: "18%",
                title: {text: "Bedeckungsgrad"},
                reversedStacks: false,
                opposite: true
            },

            {
                id: this.windSpeedAxisId,
                offset: 0,
                top: "40%",
                height: "10%",
                title: {text: "Windgeschwindigkeit m/s"},
                reversedStacks: false
            },
            {
                id: this.windDirectionAxisId,
                offset: 0, top: "50%", height: "10%",
                title: {text: "Windrichtung"}, reversedStacks: false,
                min: 0, max: 360, tickInterval: 90
            },
            {
                id: this.humidityAxisId,
                offset: 0,
                top: "60%",
                height: "10%",
                title: {text: "Luftfeuchtigkeit %"},
                min: 0,
                max: 100,
                reversedStacks: false
            },
            {
                id: this.dewpointAxisId,
                offset: 0,
                top: "70%",
                height: "10%",
                title: {text: "Taupunkt °C"},
                reversedStacks: false,
                opposite: true
            },
            {
                id: this.airPressureAxisId,
                offset: 0,
                top: "80%",
                height: "10%",
                title: {text: "Luftdruck hPa"},
                reversedStacks: false
            },
            {
                id: this.visibilityAxisId,
                offset: 0,
                top: "90%",
                height: "10%",
                title: {text: "Sichtweite m"},
                reversedStacks: false
            },

        ],
    }
    private minMaxTemperatureSeries?: Highcharts.Series;
    private avgTemperatureSeries?: Highcharts.Series;
    private sunshineDurationSeries?: Highcharts.Series;
    private rainfallSeries?: Highcharts.Series
    private snowfallSeries?: Highcharts.Series
    private maxWindSpeedSeries?: Highcharts.Series
    private avgWindSpeedSeries?: Highcharts.Series
    private windDirectionSeries?: Highcharts.Series
    private minMaxHumiditySeries?: Highcharts.Series
    private avgHumiditySeries?: Highcharts.Series
    private minMaxDewPointTemperatureSeries?: Highcharts.Series
    private avgDewPointTemperatureSeries?: Highcharts.Series
    private minMaxAirPressureSeries?: Highcharts.Series
    private avgAirPressureSeries?: Highcharts.Series
    private minMaxVisibilitySeries?: Highcharts.Series
    private avgVisibilitySeries?: Highcharts.Series
    private cloudCoverageSeries: Highcharts.Series[] = new Array(9)

    constructor(public filterService: FilterService) {
        filterService.currentData.subscribe(data => {
                if (data) {
                    let detailedMeasurements = data.details.map(d => ({
                        date: getDateLabel(d),
                        measurements: d.measurements
                    }))

                    // TODO put into separate method
                    // air temperature
                    this.minMaxTemperatureSeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.airTemperatureCentigrade.min,
                            d.measurements?.airTemperatureCentigrade.max
                        ]), false)

                    this.avgTemperatureSeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.airTemperatureCentigrade.avg
                        ]), false)

                    // TODO put into separate method
                    // sunshine duration
                    this.sunshineDurationSeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            (d.measurements?.sunshineMinutes.sum || 0) / 60
                        ]), false)

                    // TODO put into separate method
                    // rain and snow
                    this.rainfallSeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.rainfallMillimeters.sum
                        ]), false)

                    this.snowfallSeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.snowfallMillimeters.sum
                        ]), false)

                    // TODO put into separate method
                    // wind speed
                    this.maxWindSpeedSeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.windSpeedMetersPerSecond.max
                        ]), false)

                    this.avgWindSpeedSeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.windSpeedMetersPerSecond.avg
                        ]), false)

                    // TODO put into separate method
                    // wind direction
                    let scatterData = detailedMeasurements
                        .map(d => ({
                            date: d.date,
                            hourlyWindSpeeds: d.measurements?.windDirectionDegrees
                        }))

                    let s1 = []
                    for (let sd of scatterData) {
                        if (sd.hourlyWindSpeeds) {
                            let min = sd.hourlyWindSpeeds.min
                            let max = sd.hourlyWindSpeeds.max
                            if (max > min) {
                                s1.push([sd.date, min, max])
                            } else {
                                s1.push([sd.date, min, 360])
                                s1.push([sd.date, 0, max])
                            }
                        }
                    }

                    this.windDirectionSeries?.setData(s1, false)

                    // TODO put into separate method
                    // humidity
                    this.minMaxHumiditySeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.humidityPercent.min,
                            d.measurements?.humidityPercent.max
                        ]), false)

                    this.avgHumiditySeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.humidityPercent.avg
                        ]), false)

                    // TODO put into separate method
                    // dew point temperature
                    this.minMaxDewPointTemperatureSeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.dewPointTemperatureCentigrade.min,
                            d.measurements?.dewPointTemperatureCentigrade.max
                        ]), false)


                    this.avgDewPointTemperatureSeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.dewPointTemperatureCentigrade.avg
                        ]), false)


                    // TODO put into separate method
                    // air pressure
                    this.minMaxAirPressureSeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.airPressureHectopascals.min,
                            d.measurements?.airPressureHectopascals.max
                        ]), false)

                    this.avgAirPressureSeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.airPressureHectopascals.avg
                        ]), false)

                    // TODO put into separate method
                    // visibility
                    this.minMaxVisibilitySeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.visibilityMeters.min,
                            d.measurements?.visibilityMeters.max
                        ]), false)

                    this.avgVisibilitySeries?.setData(
                        detailedMeasurements.map(d => [
                            d.date,
                            d.measurements?.visibilityMeters.avg
                        ]), false)

                    // TODO put into separate method
                    // cloudiness
                    for (let i = 0; i <= 8; i++) {
                        this.cloudCoverageSeries[i].setData(
                            detailedMeasurements.map(d => [
                                d.date, d.measurements?.cloudCoverageHistogram![i]]
                            ), false)
                    }
                }
                this.bigChart?.redraw(false)
            }
        )
    }

    chartCallback: Highcharts.ChartCallbackFunction = c => {
        this.bigChart = c

        // TODO put into separate method
        // air temperature
        this.minMaxTemperatureSeries = this.bigChart.addSeries({
            type: 'arearange',
            marker: {enabled: false},
            yAxis: this.airTemperatureAxisId,
            className: "minMaxTemperature",
            name: "Min-/Max-Temperatur",
            clip: false,

        })

        this.avgTemperatureSeries = this.bigChart.addSeries({
            type: 'line',
            marker: {enabled: false},
            yAxis: this.airTemperatureAxisId,
            className: "avgTemperature",
            name: "Durchschnittstemperatur",
            linkedTo: ":previous",
            clip: false,

        })

        // TODO put into separate method
        // cloud coverage
        for (let i = 0; i <= 8; i++) {
            this.cloudCoverageSeries[i] = this.bigChart.addSeries({
                type: "column",
                stack: 's',
                stacking: 'percent',
                pointPadding: 0,
                groupPadding: 0,
                borderRadius: 0,
                borderWidth: 0,
                yAxis: this.cloudCoverageAxisId,
                className: `cloudCoverage${i}`
            })
        }

        // TODO put into separate method
        // sunshine duration
        this.sunshineDurationSeries = this.bigChart.addSeries({
            type: "column",
            // step: "left",
            yAxis: this.sunshineAxisId,
            className: "sunshine",
        })

        // TODO put into separate method
        // rain and snow
        this.rainfallSeries = this.bigChart.addSeries({
            type: "column",
            borderRadius: 0,
            stack: this.precipitationAxisId,
            stacking: "normal",
            yAxis: this.precipitationAxisId,
            className: "rainfall"
        })

        this.snowfallSeries = this.bigChart.addSeries({
            type: "column",
            borderRadius: 0,
            stack: this.precipitationAxisId,
            stacking: "normal",
            yAxis: this.precipitationAxisId,
            className: "snowfall"
        })

        // TODO put into separate method
        // wind speed
        this.maxWindSpeedSeries = this.bigChart.addSeries({
            type: 'area',
            yAxis: this.windSpeedAxisId,
            className: "maxWindSpeed",
        })

        this.avgWindSpeedSeries = this.bigChart.addSeries({
            type: 'line',
            marker: {enabled: false},
            yAxis: this.windSpeedAxisId,
            className: "avgWindSpeed"
        })

        // TODO put into separate method
        // wind direction
        this.windDirectionSeries = this.bigChart.addSeries({
            type: "columnrange",
            grouping: false,
            yAxis: this.windDirectionAxisId,
            className: "windDirection"
        })

        // TODO put into separate method
        // humidity
        this.minMaxHumiditySeries = this.bigChart.addSeries({
            type: 'arearange',
            lineWidth: 3,
            marker: {enabled: false},
            states: {hover: {enabled: false}},
            yAxis: this.humidityAxisId,
            className: "minMaxHumidity"
        })

        this.avgHumiditySeries = this.bigChart.addSeries({
            type: 'line',
            marker: {enabled: false},
            yAxis: this.humidityAxisId,
            className: "avgHumidity"
        })

        // TODO put into separate method
        // dew point temperature
        this.minMaxDewPointTemperatureSeries = this.bigChart.addSeries({
            type: 'arearange',
            lineWidth: 3,
            marker: {enabled: false},
            states: {hover: {enabled: false}},
            yAxis: this.dewpointAxisId
        })

        this.avgDewPointTemperatureSeries = this.bigChart.addSeries({
            type: 'line',
            marker: {enabled: false},
            yAxis: this.dewpointAxisId
        })

        // TODO put into separate method
        // air pressure
        this.minMaxAirPressureSeries = this.bigChart.addSeries({
            type: 'arearange',
            lineWidth: 3,
            marker: {enabled: false},
            states: {hover: {enabled: false}},
            yAxis: this.airPressureAxisId
        })

        this.avgAirPressureSeries = this.bigChart.addSeries({
            type: 'line',
            marker: {enabled: false},
            yAxis: this.airPressureAxisId
        })

        // TODO put into separate method
        // visibility
        this.minMaxVisibilitySeries = this.bigChart.addSeries({
            type: 'arearange',
            lineWidth: 3,
            marker: {enabled: false},
            states: {hover: {enabled: false}},
            yAxis: this.visibilityAxisId
        })

        this.avgVisibilitySeries = this.bigChart.addSeries({
            type: 'line',
            marker: {enabled: false},
            yAxis: this.visibilityAxisId
        })
    }

    percentageOfCloudCoverage(coverageHistogram: number[] | undefined, coverageIndices: number[]): string {
        if (!coverageHistogram) {
            return ""
        }

        let part = 0
        let sum = 0
        for (let i = 0; i < coverageHistogram.length; i++) {
            if (coverageIndices.indexOf(i) !== -1) {
                part += coverageHistogram[i]
            }
            sum += coverageHistogram[i]
        }
        return (part / sum * 100).toFixed(1) + "%"
    }

    uniqueFilter(value: any, index: number, self: number[]) {
        return self.indexOf(value) === index;
    }

    numberComparator(a: number, b: number) {
        return a - b;
    }

    clearChart(chart: Highcharts.Chart) {
        while (chart.series.length > 0) {
            chart.series[0].remove()
        }
    }

    indexOfMax(arr: number[]) {
        if (arr.length === 0) {
            return -1;
        }

        let max = arr[0];
        let maxIndex = 0;

        for (let i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                maxIndex = i;
                max = arr[i];
            }
        }

        return maxIndex;
    }
}
