import {Directive, ElementRef} from "@angular/core";
import {SummaryList} from "./SummaryService";
import {formatDate} from "@angular/common";
import {
    Chart,
    ChartConfiguration,
    ChartData,
    ChartDataset,
    ChartOptions,
    LegendItem, Scale,
    TooltipItem
} from "chart.js";
import {tick} from "@angular/core/testing";

export type MeasurementDataSet = ChartDataset & {
    showTooltip?: boolean,
    showLegend?: boolean,
    tooltipValueFormatter?: any
}

@Directive()
export abstract class BaseChart {
    protected numberFormat = new Intl.NumberFormat('de-DE', {minimumFractionDigits: 1, maximumFractionDigits: 1});
    private chart?: Chart;

    protected constructor() {
    }

    public setData(summaryList: SummaryList): void {
        let labels = this.getLabels(summaryList);
        let dataSets = this.getDataSets(summaryList);
        this.drawChart(labels, dataSets);
    }


    protected abstract getDataSets(summaryList: SummaryList): Array<MeasurementDataSet>;

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
                mode: 'index'
            },
            scales: {
                x: {
                    type: 'time',
                    time: {
                        unit: 'day',
                        displayFormats: {
                            day: "MMMM",
                            locale: "de-DE",
                            month: "MMMM",
                        },
                        round: 'day',

                    },
                    ticks: {
                        autoSkip: false,
                        // source: 'labels',
                        // callback: (tickValue, index, ticks) => {
                        //     console.log(ticks[index])
                        //     let date = new Date(ticks[index].value)
                        //     if (date.getDate() === 1) {
                        //         ticks[index].major = true
                        //         // return tickValue
                        //     // } else {
                        //         // return undefined
                        //     }
                        //     return "12"
                        // },
                        // labelOffset: 20,
                        major: {enabled: true}
                    },
                    grid: {
                        offset: true,
                        display: false,
                        drawTicks: true
                    },
                    afterBuildTicks: (axis) => {
                        axis.ticks = axis.ticks.filter(t => new Date(t.value).getDate() === 15)
                    }
                }
            },
            plugins: {
                legend: {
                    display: false,
                },
                tooltip: {
                    filter: this.showTooltip,
                    callbacks: {label: this.formatTooltipLabel}
                },

            }
        };

        let config: ChartConfiguration = {
            type: "line",
            options: options,
            data: {
                labels: labels,
                datasets: dataSets
            }
        };

        console.log(config)

        this.chart = new Chart(context, config);

    }

    private getLabels(summaryList: SummaryList): Array<Date> {
        return summaryList.map((item, index) => {
            let date = new Date(item.firstDay);
            // date.setDate(1)
            return date
        }).filter(value => value !== null)
            .map(value => value!);

    }

    protected getScales(): any {

    }

    private showLegend(legendItem: LegendItem, chartData: ChartData): boolean {
        let x = <MeasurementDataSet>chartData.datasets[legendItem.datasetIndex]
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
