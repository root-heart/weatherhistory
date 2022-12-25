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
        // parsing: true,
        elements: {point: {radius: 0}},
        scales: {},
        // datasets: {
        //     bar: {
        //         categoryPercentage: 1,
        //         barPercentage: 1
        //     }
        // },
        plugins: {
            legend: {display: false,},
            tooltip: {enabled: true,},
            // datalabels: {
            //     color: "#ddd",
            //     textStrokeWidth: 3,
            //     textStrokeColor: "black",
            //     align: ctx => {
            //         let max = Math.max.apply(null, ctx.dataset.data as number[])
            //         let min = Math.min.apply(null, ctx.dataset.data as number[])
            //         let span = max - min
            //         let value = ctx.dataset.data[ctx.dataIndex] as number
            //         if (value - min > min + 0.8 * span) {
            //             return "bottom"
            //             // } else if (value - min < min + 0.2 * span) {
            //             //     return "top"
            //             // } else {
            //             //     return "center"
            //         }
            //         return "top";
            //     },
            //     anchor: "end", // TODO
            //     // anchor: ctx =>  ctx.datasetIndex == 0 ? "start" : "end",
            //     textShadowColor: "black",
            //     textShadowBlur: 1,
            //     font: {size: 16, weight: "bold"},
            // }
        }
    }
}
