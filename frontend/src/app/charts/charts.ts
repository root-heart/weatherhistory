import {DateTime} from "luxon";
import {DailyMeasurement, MonthlySummary, YearlySummary} from "../data-classes";

// export function getDateLabel(m: DailyMeasurement | MonthlySummary | YearlySummary): string {
//   // console.log(m)
//   if ("date" in m) {
//     return DateTime.fromFormat(m.date!, "yyyy-MM-dd").toFormat("dd.MM.")
//   } else if ("month" in m && "year" in m) {
//     return DateTime.fromObject({year: m.year, month: m.month}).toFormat("MMMM yyyy")
//   } else if ("year" in m) {
//     return DateTime.fromObject({year: m.year}).toFormat("yyyy")
//   }
//   return DateTime.fromObject({year: 1979, month: 11, day: 4}).toFormat("dd.MM.yyyy")
// }


export function getDateLabel(m: DailyMeasurement | MonthlySummary | YearlySummary): number {
    // console.log(m)
    if ("date" in m) {
        return DateTime.fromFormat(m.date!, "yyyy-MM-dd", {zone: "UTC"}).toJSDate().getTime()
    } else if ("month" in m && "year" in m) {
        return DateTime.fromObject({year: m.year, month: m.month}).toJSDate().getTime()
    } else if ("year" in m) {
        return DateTime.fromObject({year: m.year}).toJSDate().getTime()
    }
    return DateTime.fromObject({year: 1979, month: 11, day: 4}).toJSDate().getTime()
}
