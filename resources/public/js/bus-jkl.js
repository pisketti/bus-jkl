
function presentResults(data) {
    var transformer = resultFilter;
    //transformer = doNothing;
    $("#result").text(toStr(data, transformer));
}

function resultFilter(data) {
    //return _.pluck(data, "number");
    return _.map(data, function(line) {
        return _.pick(line, 'number', 'time');
    });
}

function doNothing(data) {
    return data;
}

function toStr(data, transformer) {
    return JSON.stringify(transformer(data), undefined, 4);
}

$( document ).ready(function() {

    //$(".lead").text("DOM ready.");

    $.ajax({
        type: "GET",
        dataType: "json",
        url: "http://localhost:3000/json",
        success: presentResults
    });

    //$("#info").text("After ajax call");
});
