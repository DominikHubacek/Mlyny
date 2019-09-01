function send() {
    var radios = document.getElementsByName('rating');


    for (var i = 0, length = radios.length; i < length; i++) {
        if (radios[i].checked) {
            // do whatever you want with the checked radio
            alert(radios[i].value);

            //
            // private String player;
            // private String game;
            // private int rating;
            // private Date ratedon;
            var plr;
            if (plr2 != null) {
                plr = plr2;
            }
            if (plr1 != null) {
                plr = plr1
            }
            if (tmp == true) {
                plr = plr2;
            }

            // var today = new Date();
            // var date = today.getFullYear()+'-'+(today.getMonth()+1)+'-'+today.getDate();

            let rating = {
                game: "Mlyny",
                player: plr,
                rating: radios[i].value
            };
            console.log(rating);
            process(rating);
            break;
        }
    }
}


function pair() {
    window.location.href = serverURL + '/mlyny/display/unpair';
}

function newGame() {
    window.location.href = serverURL + '/mlyny/new';
}

function disable() {
    window.location.href = serverURL + '/mlyny/disable';
}

function enable() {
    window.location.href = serverURL + '/mlyny/enable';
}

function process(values) {
    let request = new XMLHttpRequest();
    let url = serverURL + "rest/rating";
    request.onreadystatechange = function () {
    };
    request.open("POST", url);
    request.setRequestHeader('Content-Type', 'application/json');
    request.send(JSON.stringify(values));
}
