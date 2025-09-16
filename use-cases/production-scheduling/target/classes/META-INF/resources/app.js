var autoRefreshIntervalId = null;

// 시간 기반 Zoom 옵션
const zoomMin = 2 * 1000 * 60 * 60 * 24 // 2 day in milliseconds
const zoomMax = 4 * 7 * 1000 * 60 * 60 * 24 // 4 weeks in milliseconds

const byCrewPanel = document.getElementById("byCrewPanel");
const byCrewTimelineOptions = {
    timeAxis: {scale: "hour", step: 6},
    orientation: {axis: "top"},
    stack: false,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: zoomMin,
    zoomMax: zoomMax
};
var byCrewGroupDataSet = new vis.DataSet();
var byCrewItemDataSet = new vis.DataSet();
var byCrewTimeline = new vis.Timeline(byCrewPanel, byCrewItemDataSet, byCrewGroupDataSet, byCrewTimelineOptions);

const byJobPanel = document.getElementById("byJobPanel");
const byJobTimelineOptions = {
    timeAxis: {scale: "hour", step: 6},
    orientation: {axis: "top"},
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: zoomMin,
    zoomMax: zoomMax
};
var byJobGroupDataSet = new vis.DataSet();
var byJobItemDataSet = new vis.DataSet();
var byJobTimeline = new vis.Timeline(byJobPanel, byJobItemDataSet, byJobGroupDataSet, byJobTimelineOptions);

$(document).ready(function () {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    });
    // Extend jQuery to support $.put() and $.delete()
    jQuery.each(["put", "delete"], function (i, method) {
        jQuery[method] = function (url, data, callback, type) {
            if (jQuery.isFunction(data)) {
                type = type || callback;
                callback = data;
                data = undefined;
            }
            return jQuery.ajax({
                url: url,
                type: method,
                dataType: type,
                data: data,
                success: callback
            });
        };
    });

    $("#refreshButton").click(function () {
        refreshSchedule();
    });
    $("#solveButton").click(function () {
        solve();
    });
    $("#stopSolvingButton").click(function () {
        stopSolving();
    });

    $("#byCrewPanelTab").on('shown.bs.tab', function (event) {
        byCrewTimeline.redraw();
    })
    $("#byJobPanelTab").on('shown.bs.tab', function (event) {
        byJobTimeline.redraw();
    })

    refreshSchedule();
});

function refreshSchedule() {
    // ===================================================================================
    // CHANGED: Mock Data를 제거하고 다시 서버 API를 호출하는 로직으로 복원합니다.
    // ===================================================================================
    $.getJSON("/schedule", function (schedule) {
        refreshSolvingButtons(schedule.solverStatus != null && schedule.solverStatus !== "NOT_SOLVING");
        $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));

        const unassignedJobs = $("#unassignedJobs");
        unassignedJobs.children().remove();
        var unassignedJobsCount = 0;
        byCrewGroupDataSet.clear();
        byJobGroupDataSet.clear();
        byCrewItemDataSet.clear();
        byJobItemDataSet.clear();

        $.each(schedule.crewList, (index, crew) => {
            byCrewGroupDataSet.add({id : crew.id, content: crew.name});
        });

        $.each(schedule.jobList, (index, job) => {
            const jobGroupElement = $(`<div/>`)
              .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
              .append($(`<p class="card-text ml-2 mb-0"/>`).text(`${job.durationInHours} hours`));
            byJobGroupDataSet.add({
                id : job.id,
                content: jobGroupElement.html()
            });
            byJobItemDataSet.add({
                  id: job.id + "_readyToIdealEnd", group: job.id,
                  start: job.readyDateTime, end: job.idealEndDateTime,
                  type: "background",
                  style: "background-color: #8AE23433"
            });
            byJobItemDataSet.add({
                  id: job.id + "_idealEndToDue", group: job.id,
                  start: job.idealEndDateTime, end: job.dueDateTime,
                  type: "background",
                  style: "background-color: #FCAF3E33"
            });

            if (job.crew == null || job.startDateTime == null) {
                unassignedJobsCount++;
                const unassignedJobElement = $(`<div class="card-body p-2"/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
                    .append($(`<p class="card-text ml-2 mb-0"/>`).text(`${job.durationInHours} hours`))
                    .append($(`<p class="card-text ml-2 mb-0"/>`).text(`Ready: ${job.readyDateTime}`))
                    .append($(`<p class="card-text ml-2 mb-0"/>`).text(`Due: ${job.dueDateTime}`));
                const byJobJobElement = $(`<div/>`)
                  .append($(`<h5 class="card-title mb-1"/>`).text(`Unassigned`));
                $.each(job.tagSet, (index, tag) => {
                    const color = pickColor(tag);
                    unassignedJobElement.append($(`<span class="badge mr-1" style="background-color: ${color}"/>`).text(tag));
                    byJobJobElement.append($(`<span class="badge mr-1" style="background-color: ${color}"/>`).text(tag));
                });
                unassignedJobs.append($(`<div class="card"/>`).append(unassignedJobElement));
                byJobItemDataSet.add({
                    id : job.id, group: job.id,
                    content: byJobJobElement.html(),
                    start: job.readyDateTime, end: JSJoda.LocalDateTime.parse(job.readyDateTime).plusHours(job.durationInHours).toString(),
                    style: "background-color: #EF292999"
                });
            } else {
                const byCrewJobElement = $(`<div/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
                    .append($(`<p class="card-text ml-2 mb-0"/>`).text(`${job.durationInHours} hours`));
                const byJobJobElement = $(`<div/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(job.crew.name));
                $.each(job.tagSet, (index, tag) => {
                    const color = pickColor(tag);
                    byCrewJobElement.append($(`<span class="badge mr-1" style="background-color: ${color}"/>`).text(tag));
                    byJobJobElement.append($(`<span class="badge mr-1" style="background-color: ${color}"/>`).text(tag));
                });
                byCrewItemDataSet.add({
                    id : job.id, group: job.crew.id,
                    content: byCrewJobElement.html(),
                    start: job.startDateTime, end: job.endDateTime
                });
                byJobItemDataSet.add({
                    id : job.id, group: job.id,
                    content: byJobJobElement.html(),
                    start: job.startDateTime, end: job.endDateTime
                });
            }
        });
        if (unassignedJobsCount === 0) {
            unassignedJobs.append($(`<p/>`).text(`There are no unassigned jobs.`));
        }
        
        // 서버에서 받은 WorkCalendar 정보로 윈도우 설정
        byCrewTimeline.setWindow(schedule.workCalendar.fromDate, schedule.workCalendar.toDate);
        byJobTimeline.setWindow(schedule.workCalendar.fromDate, schedule.workCalendar.toDate);
    });
}

// NEW: 최적화(Solver) 관련 함수들 복원
function solve() {
    $.post("/schedule/solve", function () {
        refreshSolvingButtons(true);
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Start solving failed.", xhr);
    });
}

function refreshSolvingButtons(solving) {
    if (solving) {
        $("#solveButton").hide();
        $("#stopSolvingButton").show();
        if (autoRefreshIntervalId == null) {
            autoRefreshIntervalId = setInterval(refreshSchedule, 2000);
        }
    } else {
        $("#solveButton").show();
        $("#stopSolvingButton").hide();
        if (autoRefreshIntervalId != null) {
            clearInterval(autoRefreshIntervalId);
            autoRefreshIntervalId = null;
        }
    }
}

function stopSolving() {
    $.post("/schedule/stopSolving", function () {
        refreshSolvingButtons(false);
        refreshSchedule();
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Stop solving failed.", xhr);
    });
}

function showError(title, xhr) {
    const serverErrorMessage = !xhr.responseJSON ? `${xhr.status}: ${xhr.statusText}` : xhr.responseJSON.message;
    console.error(title + "\n" + serverErrorMessage);
    const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 30rem"/>`)
      .append($(`<div class="toast-header bg-danger">
                 <strong class="mr-auto text-dark">Error</strong>
                 <button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">
                   <span aria-hidden="true">&times;</span>
                 </button>
               </div>`))
      .append($(`<div class="toast-body"/>`)
        .append($(`<p/>`).text(title))
        .append($(`<pre/>`)
          .append($(`<code/>`).text(serverErrorMessage))
        )
      );
    $("#notificationPanel").append(notification);
    notification.toast({delay: 30000});
    notification.toast('show');
}

// ... TangoColorFactory 등 나머지 코드는 동일하게 유지됩니다 ...
const SEQUENCE_1 = [0x8AE234, 0xFCE94F, 0x729FCF, 0xE9B96E, 0xAD7FA8];
const SEQUENCE_2 = [0x73D216, 0xEDD400, 0x3465A4, 0xC17D11, 0x75507B];
var colorMap = new Map;
var nextColorCount = 0;
function pickColor(object) {
    let color = colorMap[object];
    if (color !== undefined) {
        return color;
    }
    color = nextColor();
    colorMap[object] = color;
    return color;
}
function nextColor() {
    let color;
    let colorIndex = nextColorCount % SEQUENCE_1.length;
    let shadeIndex = Math.floor(nextColorCount / SEQUENCE_1.length);
    if (shadeIndex === 0) {
        color = SEQUENCE_1[colorIndex];
    } else if (shadeIndex === 1) {
        color = SEQUENCE_2[colorIndex];
    } else {
        shadeIndex -= 3;
        let floorColor = SEQUENCE_2[colorIndex];
        let ceilColor = SEQUENCE_1[colorIndex];
        let base = Math.floor((shadeIndex / 2) + 1);
        let divisor = 2;
        while (base >= divisor) {
            divisor *= 2;
        }
        base = (base * 2) - divisor + 1;
        let shadePercentage = base / divisor;
        color = buildPercentageColor(floorColor, ceilColor, shadePercentage);
    }
    nextColorCount++;
    return "#" + color.toString(16);
}
function buildPercentageColor(floorColor, ceilColor, shadePercentage) {
    let red = (floorColor & 0xFF0000) + Math.floor(shadePercentage * ((ceilColor & 0xFF0000) - (floorColor & 0xFF0000))) & 0xFF0000;
    let green = (floorColor & 0x00FF00) + Math.floor(shadePercentage * ((ceilColor & 0x00FF00) - (floorColor & 0x00FF00))) & 0x00FF00;
    let blue = (floorColor & 0x0000FF) + Math.floor(shadePercentage * ((ceilColor & 0x00FF00) - (floorColor & 0x0000FF))) & 0x0000FF;
    return red | green | blue;
}