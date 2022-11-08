export class ChartArea {
    top: number = NaN
    bottom: number = NaN
    height: number = NaN
    dataPoints: DataPoint[][] = []
    zeroYCoordinage: number = 0
}

export class DataPoint {
    x: number = NaN
    y: number = NaN
}
