import {ChartDataset, ChartOptions} from "chart.js";
import {DateTime} from "luxon";

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

export function getXScale(data: { firstDay: string }[], resolution: ChartResolution, options: ChartOptions, showAxis: boolean) {
    let format = ""
    if (resolution == "day") {
        format = "dd.MM."
    } else if (resolution == "month") {
        format = "MMM"
    }
    options.scales!.x! = {
        type: "category",
        labels: data.map(d => DateTime.fromFormat(d.firstDay, "yyyy-MM-dd").toFormat(format)),
        display: showAxis,
        ticks: {minRotation: 0, maxRotation: 0, sampleSize: 12},
    }
}