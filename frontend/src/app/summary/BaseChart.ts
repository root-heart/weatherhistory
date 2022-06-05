import {Directive, ElementRef} from "@angular/core";
import {SummaryList} from "./SummaryService";
import {formatDate} from "@angular/common";
import {Chart, ChartData, ChartDataset, LegendItem, TooltipItem} from "chart.js";

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

    protected drawChart(labels: string[], dataSets: Array<MeasurementDataSet>): void {
        if (this.chart) {
            this.chart.destroy();
        }

        let canvas = this.getCanvas();
        if (!canvas) {
            return;
        }
        let context = <CanvasRenderingContext2D>canvas.nativeElement.getContext('2d');

        this.chart = new Chart(context,
            {
                type: "bar",
                options: {
                    maintainAspectRatio: false,
                    responsive: true,
                    animation: false,
                    scales: this.getYScales(),
                    interaction: {
                        mode: 'index'
                    },
                    plugins: {
                        legend: {
                            display: true,
                            position: 'top',
                            align: 'center',
                            labels: {filter: this.showLegend}
                        },
                        tooltip: {
                            filter: this.showTooltip,
                            callbacks: {label: this.formatTooltipLabel}
                        }
                    }
                },
                data: {
                    labels: labels,
                    datasets: dataSets
                }
            });
    }

    protected abstract getYScales(): any;

    private getScales() {
        return {
            // xAxisVisible: {
            // stacked: true,
            // },
            // xAxisInvisible: {
            //     display: false,
            //     stacked: true
            // },

            // yAxisHours: {
            //     //display: false,
            //     title: {
            //         display: true,
            //         text: 'Stunden'
            //     },
            //     stacked: true,
            //     min: 0,
            //     max: 800,
            //     stack: 'stack'
            // },
            // yAxisPercent: {
            //     //display: false,
            //     title: {},
            //     stacked: true,
            //     min: 0,
            //     max: 100,
            //     stack: 'stack'
            // },
            // yAxisMillimeters: {
            //     //display: false,
            //     title: {
            //         display: true,
            //         text: 'Millimeter'
            //     },
            //     min: 0,
            //     max: 400,
            //     stack: 'stack'
            // }
        };
    }

    private getLabels(summaryList: SummaryList): string[] {
        let lastYear: number | null = null;
        return summaryList.map((item, index) => {
            if (item.intervalType == 'MONTH') {
                let firstDay = new Date(item.firstDay);
                if (lastYear == null || firstDay.getFullYear() != lastYear) {
                    lastYear = firstDay.getFullYear();
                    return formatDate(firstDay, 'MMMM yyyy', 'de-DE');
                } else {
                    return formatDate(firstDay, 'MMMM', 'de-DE');
                }
            } else if (item.intervalType == 'SEASON') {
                let firstDay = new Date(item.firstDay);
                let firstMonth = firstDay.getMonth();
                let seasonName = 'unknown';
                switch (firstMonth) {
                    case 2:
                    case 3:
                    case 4:
                        seasonName = 'Spring ' + firstDay.getFullYear();
                        break;
                    case 5:
                    case 6:
                    case 7:
                        seasonName = 'Summer ' + firstDay.getFullYear();
                        break;
                    case 8:
                    case 9:
                    case 10:
                        seasonName = 'Fall ' + firstDay.getFullYear();
                        break
                    default:
                        seasonName = 'Winter ' + firstDay.getFullYear() + '/' + (firstDay.getFullYear() + 1);
                }
                return seasonName;
            } else if (item.intervalType == 'YEAR') {
                let firstDay = new Date(item.firstDay);
                return firstDay.getFullYear().toString();
            } else if (item.intervalType == 'DECADE') {
                let firstDay = new Date(item.firstDay);
                let lastDay = new Date(item.lastDay);
                return firstDay.getFullYear().toString() + ' - ' + lastDay.getFullYear().toString();
            }
            return item.intervalType
        });
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