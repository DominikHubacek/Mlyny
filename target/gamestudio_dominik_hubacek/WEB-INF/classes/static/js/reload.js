function updateDiv() {
    // $( "#reload" ).load(window.location.href + "#reload" );

    $.get(window.location.href, {},
        function (returnedHtml) {
            var parser = new DOMParser();
            var doc = parser.parseFromString(returnedHtml, "text/html");
            var markup = doc.getElementById('reload').outerHTML;
            // alert(doc.getElementById("game"));

            // alert(returnedHtml.getElementById('game'));
            // alert(returnedHtml.getElementById('stat'));
            // alert(returnedHtml.getElementById('game'));
            // alert(returnedHtml.getElementById('reload'));
            // alert(returnedHtml.document.getElementById('game'));

            $("#reload").html(markup);
        });
}
setInterval(updateDiv, 1000);
//
// function reloading(){
//     setInterval(function(){
//     updateDiv()
// }, 2000);}