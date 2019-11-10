function GameViewModel(user) {
    var self = this;

    //General
    this.readyMatch = ko.observable(false);
    this.isLoading = ko.observable(false);

    //User (Player 1) properties
    this.userEmail = ko.observable(user.email);
    this.userName = ko.observable(user.userName);
    this.userPhoto = ko.observable(user.photo ? user.photo : "assets/images/default-user-icon.jpg");
    this.p1Turn = ko.observable();
    this.p1Score = ko.observable();
    this.p1LetterImg = ko.observable("assets/images/" + user.userName.charAt(0).toUpperCase() + ".svg");
    this.p1LetterTxt = ko.observable(user.userName.charAt(0).toUpperCase());

    //Player 2 properties
    this.p2Name = ko.observable();
    this.p2LetterImg = ko.observable();
    this.p2LetterTxt = ko.observable();
    this.p2Score = ko.observable();


    //Letters
    this.l1Img = ko.observable();
    this.l2Img = ko.observable();
    this.l3Img = ko.observable();
    this.l4Img = ko.observable();
    this.l5Img = ko.observable();
    this.l6Img = ko.observable();
    this.l7Img = ko.observable();
    this.l1Txt = ko.observable();
    this.l2Txt = ko.observable();
    this.l3Txt = ko.observable();
    this.l4Txt = ko.observable();
    this.l5Txt = ko.observable();
    this.l6Txt = ko.observable();
    this.l7Txt = ko.observable();

    //Loading circle code
    var displayValue = function (element, valueAccessor) {
        var value = ko.utils.unwrapObservable(valueAccessor());
        var isCurrentlyVisible = !(element.style.display == "none");
        if (value && !isCurrentlyVisible)
            element.style.display = "";
        else if ((!value) && isCurrentlyVisible)
            element.style.display = "none";
    };

    ko.bindingHandlers['loading-animation'] = {

        'init': function (element, valueAccessor) {
            $(element)
                .append(
                    '<div class="circle circle1 circle1-1"><div class="circle circle1 circle2-1"><div class="circle circle1 circle3-1"></div></div></div>');

            displayValue(element, valueAccessor);
        },
        'update': function (element, valueAccessor) {
            displayValue(element, valueAccessor);
        }
    };


    //Logout controller
    this.logout = function () {
        var data = {
            data: {},
            url: "/logout",
            type: "post",
            success: logoutOK,
            error: logoutError
        };
        $.ajax(data);
    }

    function logoutOK(response) {
        sessionStorage.clear();
        window.location = "http://localhost:8080/index.html";
        console.log("Logout OK")
    }

    function logoutError(response) {
        console.log("Logout ERROR")
    }

    //Create and join games
    this.createGame = function () {
        console.log("createGame")
        var data = {
            data: {},
            url: "/createMatch",
            type: "post",
            success: gameOK,
            error: gameError
        };
        this.isLoading(true);
        $.ajax(data);
    }

    this.joinGame = function () {
        var data = {
            data: {},
            url: "/joinMatch",
            type: "post",
            success: gameOK,
            error: gameError
        };
        $.ajax(data);
    }

    function gameError(response) {
        console.log(response.responseText)
    }

    function gameOK(response) {
        var ws = new WebSocket("ws://localhost:8080/wsServer");
        ws.onopen = function (event) {
            response = JSON.parse(response);
            console.log(response.type)
            if (response.type == "PARTIDA LISTA") {
                var mensaje = {
                    type: "INICIAR PARTIDA",
                    idPartida: response.idPartida
                };
                ws.send(JSON.stringify(mensaje));
            }
            ws.onerror = function (event) {
                console.log("on error");
            }
            ws.onclose = function (event) {
                console.log("on close");
            }
            ws.onmessage = function (event) {

                var jso = event.data;
                jso = JSON.parse(jso);
                console.log("Mensaje recibido de tipo: " + jso.type);

                if (jso.type == "TEXTO") {
                    console.log(jso.mensaje);
                } else if (jso.type == "START") {
                    console.log("start")
                    self.readyMatch(true);
                    self.isLoading(false);
                    self.p1Turn(jso.turn);
                    self.p2Name(jso.opponent);
                    self.p2LetterImg("assets/images/" + jso.opponent.charAt(0).toUpperCase() + ".svg");
                    self.p2LetterTxt(jso.opponent.charAt(0).toUpperCase());
                    console.log((jso.turn ? "Tienes " : "No tienes") + " el turno. Tus letras son: " + jso.letters);
                    
                    //Binding letters
                    var letters = jso.letters.split(' ');
                    self.l1Img("assets/images/" + letters[0].toUpperCase() + ".svg");
                    self.l2Img("assets/images/" + letters[1].toUpperCase() + ".svg");
                    self.l3Img("assets/images/" + letters[2].toUpperCase() + ".svg");
                    self.l4Img("assets/images/" + letters[3].toUpperCase() + ".svg");
                    self.l5Img("assets/images/" + letters[4].toUpperCase() + ".svg");
                    self.l6Img("assets/images/" + letters[5].toUpperCase() + ".svg");
                    self.l7Img("assets/images/" + letters[6].toUpperCase() + ".svg");

                    self.l1Txt(letters[0].toUpperCase());
                    self.l2Txt(letters[1].toUpperCase());
                    self.l3Txt(letters[2].toUpperCase());
                    self.l4Txt(letters[3].toUpperCase());
                    self.l5Txt(letters[4].toUpperCase());
                    self.l6Txt(letters[5].toUpperCase());
                    self.l7Txt(letters[6].toUpperCase());
                }

            }
        }
    }

}

//initializeNavbar();
user = sessionStorage.getItem('user')
if (!user) {
    window.location = "http://localhost:8080/index.html";
} else {
    var game = new GameViewModel(JSON.parse(user));
    ko.applyBindings(game);
}