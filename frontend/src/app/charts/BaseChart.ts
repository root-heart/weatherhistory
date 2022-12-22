import {ChartDataset, ChartOptions} from "chart.js";

export type MeasurementDataSet = ChartDataset & {
    showTooltip?: boolean,
    showLegend?: boolean,
    tooltipValueFormatter?: any
}

export type ChartResolution = "daily" | "monthly"

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
