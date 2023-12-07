import * as Highcharts from 'highcharts';
import addMore from "highcharts/highcharts-more";

addMore(Highcharts);

export abstract class ChartComponentBase {
    Highcharts: typeof Highcharts = Highcharts;
    chart?: Highcharts.Chart;
    chartOptions: Highcharts.Options = {
        chart: {styledMode: true, animation: false, zooming: {mouseWheel: {enabled: true}, type: "x"}},
        legend: {enabled: false},
        title: {text: undefined},
        tooltip: {
            shared: true,
            xDateFormat: "%d.%m.%Y",
            animation: false
        },
        plotOptions: {
            line: {animation: false},
            arearange: {animation: false},
            column: {animation: false}

        },
        xAxis: {
            id: "xAxis",
            crosshair: true,
            type: 'datetime',
            labels: {
                formatter: v => new Date(v.value).toLocaleDateString('de-DE', {day: "numeric", month: "short"})
            },
        },
        yAxis: {
            id: "yAxis",
            title: {text: undefined},
            reversedStacks: false
        },
    }
    chartCallback: Highcharts.ChartCallbackFunction = c => {
        this.chart = c
        this.createSeries(c)
    }

    protected abstract createSeries(chart: Highcharts.Chart): void

}
