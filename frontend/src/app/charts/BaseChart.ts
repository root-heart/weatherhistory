import {ChartDataset, ChartOptions} from "chart.js";
import {DateTime} from "luxon";
import {DailyMeasurement, MonthlySummary, YearlySummary} from "../data-classes";

export type MeasurementDataSet = ChartDataset & {
    showTooltip?: boolean,
    showLegend?: boolean,
    tooltipValueFormatter?: any
}

export type ChartResolution = "day" | "month" | "year"

export function getDefaultChartOptions(): ChartOptions {
    return {
        normalized: true,
        maintainAspectRatio: false,
        responsive: true,
        animation: false,
        interaction: {
            mode: 'index',
            intersect: false
        },
        elements: {point: {radius: 0}},
        scales: {},
        plugins: {
            legend: {display: false,},
            tooltip: {enabled: true,},
        }
    }
}

export function getXScale(data: { dateLabel: string }[], resolution: ChartResolution, options: ChartOptions, showAxis: boolean) {
    // console.log(data.map(d => d.dateLabel))

    options.scales!.x! = {
        type: "category",
        // labels: data.map(d => d.firstDay),
        labels: data.map(d => d.dateLabel),
        display: showAxis,
        ticks: {minRotation: 0, maxRotation: 0, sampleSize: 12},
    }
}

export function getDateLabel(m: DailyMeasurement | MonthlySummary | YearlySummary): string {
    // console.log(m)
    if ("date" in m) {
        return DateTime.fromFormat(m.date!, "yyyy-MM-dd").toFormat("dd.MM.")
    } else if ("month" in m && "year" in m) {
        return DateTime.fromObject({year: m.year, month: m.month}).toFormat("MMMM yyyy")
    } else if ("year" in m) {
        return DateTime.fromObject({year: m.year}).toFormat("yyyy")
    } else
        return DateTime.fromObject({year: 1979, month: 11, day: 11}).toFormat("dd.MM.yyyy")
}