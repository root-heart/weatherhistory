import {Directive, ElementRef} from "@angular/core";
import {Chart, ChartConfiguration, ChartData, ChartDataset, ChartOptions, LegendItem, TooltipItem} from "chart.js";

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
            maintainAspectRatio: false,
            responsive: true,
            animation: false,
            interaction: {
                mode: 'index',
                intersect: false
            },
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
                        displayFormats: {day: "DD.MMMM", month: "M", hour: "H"},
                    }
                }
                break;
            case "monthly":
                options.scales!.x = {
                    type: "category"
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
