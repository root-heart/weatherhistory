import {Directive, ElementRef} from "@angular/core";
import {Chart, ChartConfiguration, ChartData, ChartDataset, ChartOptions, LegendItem, TooltipItem} from "chart.js";
import ChartDataLabels from 'chartjs-plugin-datalabels';

export type MeasurementDataSet = ChartDataset & {
    showTooltip?: boolean,
    showLegend?: boolean,
    tooltipValueFormatter?: any
}

export type BaseRecord = {
    day: Date,
    month: number
}

export type ChartResolution = "daily" | "monthly"

@Directive()
export abstract class BaseChart<T extends BaseRecord> {
    protected numberFormat = new Intl.NumberFormat('de-DE', {minimumFractionDigits: 1, maximumFractionDigits: 1});
    private chart?: Chart;
    protected resolution?: ChartResolution

    protected constructor() {
        Chart.register(ChartDataLabels);
    }

    public setData(data: Array<T>, resolution: ChartResolution): void {
        this.resolution = resolution
        let labels = this.getLabels(data);
        let dataSets = this.getDataSets(data);
        this.drawChart(labels, dataSets);
    }


    protected abstract getDataSets(data: Array<T>): Array<ChartDataset>;

    protected abstract getCanvas(): ElementRef | undefined;

    protected drawChart(labels: Date[], dataSets: Array<MeasurementDataSet>): void {
        if (this.chart) {
            this.chart.destroy();
        }

        let canvas = this.getCanvas();
        if (!canvas) {
            return;
        }
        let context = <CanvasRenderingContext2D>canvas.nativeElement.getContext('2d');

        let options: ChartOptions = {
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
            scales: {
                y: {max: this.getMaxY(), beginAtZero: false}
            },
            bar: {
                datasets: {
                    categoryPercentage: 0.8,
                    barPercentage: 1
                }
            },
            plugins: {
                legend: {display: false,},
                tooltip: {
                    filter: this.showTooltip,
                    callbacks: {label: this.formatTooltipLabel}
                },
                datalabels: {
                    color: "#ddd",
                    textStrokeWidth: 3,
                    textStrokeColor: "black",
                    align: ctx => {
                        let max = Math.max.apply(null, ctx.dataset.data as number[])
                        let min = Math.min.apply(null, ctx.dataset.data as number[])
                        let span = max - min
                        let value = ctx.dataset.data[ctx.dataIndex] as number
                        if (value - min > min + 0.8 * span) {
                            return "bottom"
                            // } else if (value - min < min + 0.2 * span) {
                            //     return "top"
                            // } else {
                            //     return "center"
                        }
                        return "top";
                    },
                    anchor: "end", // TODO
                    // anchor: ctx =>  ctx.datasetIndex == 0 ? "start" : "end",
                    textShadowColor: "black",
                    textShadowBlur: 1,
                    font: {size: 16, weight: "bold"},
                }
            }
        };

        switch (this.resolution) {
            case "daily":
                // xscale.afterBuildTicks = (axis: { ticks: any[]; }) => {
                //     axis.ticks = axis.ticks.filter(t => new Date(t.value).getDate() === 15)
                // }
                options.scales!.x = {
                    type: "time",
                    time: {
                        unit: "day",
                        displayFormats: {day: "DD.MM.", month: "M", hour: "H"},
                    },
                    ticks: {
                        minRotation: 0, maxRotation: 0, sampleSize: 3
                    }
                }
                break;
            case "monthly":
                options.scales!.x = {
                    type: "category",
                    ticks: {
                        minRotation: 0, maxRotation: 0, sampleSize: 3
                    }
                }
                break;
        }

        let config: ChartConfiguration = {
            type: "line",
            options: options,
            data: {
                labels: labels,
                datasets: dataSets
            }
        };

        this.chart = new Chart(context, config);
    }

    protected getLabels(data: Array<T>): Array<any> {
        return data.map(item => this.resolution == "daily" ? new Date(item.day) : item.month);
    }

    protected getMaxY(): number | undefined {
        return undefined
    }

    private showLegend(legendItem: LegendItem, chartData: ChartData): boolean {
        let x = <MeasurementDataSet>chartData.datasets[legendItem.datasetIndex!]
        return x.showLegend || false
    }

    private showTooltip(tooltipItem: TooltipItem<any>): boolean {
        let x = <MeasurementDataSet>tooltipItem.dataset
        return x.showTooltip || false
    }

    private formatTooltipLabel(tooltipItem: TooltipItem<any>): string {
        let label = tooltipItem.dataset.label + ": ";
        if (tooltipItem.dataset.tooltipValueFormatter) {
            label += tooltipItem.dataset.tooltipValueFormatter(tooltipItem.raw);
        } else {
            label += tooltipItem.formattedValue;
        }
        return label;
    }


}
