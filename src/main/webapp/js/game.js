function GameViewModel(user) {
    var self = this;
    self.ws = null;

    //General: controlling hide & show tablero and isLoading animation
    this.shouldShowBoard = ko.observable(false);
    this.isLoading = ko.observable(false);

    //jQuery dialogs 
    ko.bindingHandlers.modal = {
        init: function (element, valueAccessor) {
            $(element).modal({
                show: false,
                backdrop: 'static',
                keyboard: false
            });

            var value = valueAccessor();
            if (ko.isObservable(value)) {
                // Update 28/02/2018
                // Thank @HeyJude for fixing a bug on
                // double "hide.bs.modal" event firing.
                // Use "hidden.bs.modal" event to avoid
                // bootstrap running internal modal.hide() twice.
                $(element).on('hidden.bs.modal', function () {
                    value(false);
                });
            }

            // Update 13/07/2016
            // based on @Richard's finding,
            // don't need to destroy modal explicitly in latest bootstrap.
            // modal('destroy') doesn't exist in latest bootstrap.
            // ko.utils.domNodeDisposal.addDisposeCallback(element, function () {
            //    $(element).modal("destroy");
            // });

        },
        update: function (element, valueAccessor) {
            var value = valueAccessor();
            if (ko.utils.unwrapObservable(value)) {
                $(element).modal('show');
            } else {
                $(element).modal('hide');
            }
        }
    }

    this.showDialogLoser = ko.observable(false);
    this.showDialogWinner = ko.observable(false);
    this.showDialogCambioLetras = ko.observable(false);

    //CAMBIO DE LETRAS
    this.changeLetters = ko.observableArray();

    this.cambiarLetrasCancel = function () {
        self.showDialogCambioLetras(false);
    }

    this.cambiarLetrasConfirm = function () {
        self.showDialogCambioLetras(false);

        console.log(self.changeLetters());

        var mensaje = {
            type: "CAMBIO_LETRAS",
            idPartida: sessionStorage.getItem('idPartida'),
            letters: []
        };
    }

    this.submitEnd = function () {
        self.showDialogLoser(false);
        self.showDialogWinner(false);
        endMatch();
    }

    //User (Player 1) properties
    this.player1 = ko.observable(new Player(ko, user.userName, user.email, user.photo));

    //Player 2 properties
    this.player2 = ko.observable(new Player(ko));

    //Tablero
    this.tablero = ko.observable(new Tablero(ko));

    //Controller for Popups
    this.shouldShowPopup = ko.observable(false);


    //Movement history. Initially an empty array
    this.movementHistory = ko.observableArray();


    //Timer.
    //https://stackoverflow.com/questions/22080400/hours-minutes-seconds-knockout-countdown-timer
    self.timer = ko.observable(120);

    self.minutes = ko.computed(function () {
        return Math.floor(self.timer() / 60) % 60;
    }, self);

    self.seconds = ko.computed(function () {
        return addZero(self.timer() % 60);
    }, self);


    var timerController = null;
    var countdown = function () {
        timerController = setInterval(function () {
            var newTimer = self.timer() - 1;

            if (newTimer < 0) {
                self.timer(120);
                self.stopCountdown();
                self.abandonar();
                return 0;
            } else {
                self.timer(newTimer);
            }
        }, 1000);
    }

    this.launchCountdown = function () {
        self.timer(120);
        countdown();
    }

    this.stopCountdown = function () {
        self.timer(120);
        clearInterval(timerController);
        timerController = null;
    }
    //End of timer


    //Dragging, Dropping, and Sorting With observableArrays
    //http://www.knockmeout.net/2011/05/dragging-dropping-and-sorting-with.html
    //Sortable list
    /*
    ko.bindingHandlers.sortableList = {
        init: function (element, valueAccessor) {
            var list = valueAccessor();
            $(element).sortable({
                update: function (event, ui) {
                    //retrieve our actual data item
                    var item = ui.item.tmplItem().data;
                    //figure out its new position
                    var position = ko.utils.arrayIndexOf(ui.item.parent().children(), ui.item[0]);
                    //remove the item and add it back in the right spot
                    if (position >= 0) {
                        list.remove(item);
                        list.splice(position, 0, item);
                    }
                }
            });
        }
    };
    */


    //Draggable element
    ko.bindingHandlers.draggable = {
        init: function (element, valueAccessor, allBindings, data, bindingContext) {
            $(element).draggable({
                scroll: false,
                revert: "invalid",
                snap: ".scrabble-td"
                //grid: [40,40]
            });
        }
    };

    //Droppable element
    ko.bindingHandlers.droppable = {
        init: function (element, valueAccessor, allBindings, data, bindingContext) {
            $(element).droppable({

                accept: function (dropedElement) {
                    // comprueba si hay una ficha en esa casilla
                    if(data.letter != ''){
                        var row = ui.draggable.id.split(",")[1];
                        var column = ui.draggable.id.split(",")[2];
                        var bool=true;
                        self.tablero().casillasJugadas().forEach(function (item, index, object) {
                            if (item.row == row && item.column == column) {
                                bool=false;
                            }
                        });
                        return bool;
                    }else{
                        return true;
                    }
                },
                //start: function (event, ui) {},
                drop: function (event, ui) {
                    // Metodo para comparar si la ficha tiene el atributo onboard
                    // si tiene elatributo on board se revisa la posicion que tenia 
                    // y se elimina de casillas jugadas para añadir la nueva posicion
                    if (ui.draggable.hasClass('onboard')) {
                        var row = ui.draggable.id.split(",")[1];
                        var column = ui.draggable.id.split(",")[2];

                        self.tablero().casillasJugadas().forEach(function (item, index, object) {
                            if (item.row == row && item.column == column) {
                                self.tablero().casillasJugadas().splice(index, 1);
                            }
                        });

                        ui.draggable.id = 'over,' + data.row + ',' + data.column;
                    } else {
                        ui.draggable.id = 'over,' + data.row + ',' + data.column;
                    }
                    ui.draggable.removeClass('onboard');
                    ui.draggable.toggleClass('onboard');
                    if (event) {
                        //data: objeto Casilla sobre el que se suelta la ficha
                        //element: elemento HTML sobre el que se suelta la ficha (td)

                        //Para obtener la letra que se ha soltado sobre el tablero, utilizo event.toElement, con
                        //el que se tiene acceso a la imagen de la letra. A partir de ahí, proceso el atributo src
                        //para obtener la letra. 
                        var letter = event.toElement.src.split("/");
                        letter = letter[letter.length - 1].split(".")[0];


                        console.log("Has soltado la letra " + letter + " en fila:" + data.row + ", col:" + data.column)
                        // esto se pondrá al final data.letter = letter;
                        self.tablero().casillasJugadas().push(data);
                    }
                },
            });
        }
    };

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

    function endMatch() {
        self.shouldShowBoard(false);
        sessionStorage.removeItem("sessionStorage");
        self.stopCountdown();
        self.player2 = ko.observable();

    }

    function gameError(response) {
        console.log(response.responseText)
    }

    function gameOK(response) {
        self.ws = new WebSocket("ws://localhost:8080/wsServer");
        response = JSON.parse(response);

        //Guardar id partida en sessionStorage
        if (response.idPartida)
            sessionStorage.idPartida = response.idPartida;

        self.ws.onopen = function (event) {
            if (response.type == "PARTIDA LISTA") {
                var mensaje = {
                    type: "INICIAR PARTIDA",
                    idPartida: response.idPartida
                };
                self.ws.send(JSON.stringify(mensaje));
            }
        }
        self.ws.onerror = function (event) {
            console.log("on error");
        }
        self.ws.onclose = function (event) {
            console.log("on close");
        }
        self.ws.onmessage = function (event) {

            var jso = event.data;
            jso = JSON.parse(jso);
            console.log("Mensaje recibido del tipo: " + jso.type);
            console.log(JSON.stringify(jso))

            if (jso.type == "TEXTO") {
                console.log(jso.mensaje);
            } else if (jso.type == "START") {
                console.log("start")
                self.shouldShowBoard(true);
                self.isLoading(false);
                self.player1().turn(jso.turn)
                self.player2().turn(!jso.turn);
                self.player2().name(jso.opponent);
                self.player2().letterImg("assets/images/" + jso.opponent.charAt(0).toUpperCase() + ".svg");
                self.player2().letterTxt(jso.opponent.charAt(0).toUpperCase());

                //Binding letters
                var letters = jso.letters.split(' ');
                for (var i = 0; i < letters.length; i++) {
                    self.tablero().panel.push(letters[i]);
                }

                //Iniciar temporizador
                if (jso.turn) {
                    self.launchCountdown();
                }


            } else if (jso.type == "MOVEMENT") {
                if (jso.exceptions > 0) {

                } else {
                    self.player1().turn(false);
                    self.player2().turn(true);
                    self.player1().score(jso.score);
                    self.tablero().fichasRestantes(jso.availablePieces);
                    if (jso.movements.length > 0) {

                    } else {
                        var timestamp = new Date();
                        self.movementHistory.unshift("[" + addZero(timestamp.getHours()) + ":" + addZero(timestamp.getMinutes()) + ":" +
                            addZero(timestamp.getSeconds()) + "]\t" + self.player1().name() + " pasó su turno sin movimientos");
                    }

                    //Stop timer
                    self.stopCountdown();
                }

            }

            //El jugador rival ha hecho un movimiento
            else if (jso.type == "OPPONENT_MOVEMENT") {
                if (jso.exceptions > 0) {

                } else {
                    self.player1().turn(true);
                    self.player2().turn(false);
                    self.player2().score(jso.score);
                    self.tablero().fichasRestantes(jso.availablePieces);
                    if (jso.movements.length > 0) {

                    } else {
                        var timestamp = new Date();
                        self.movementHistory.unshift("[" + addZero(timestamp.getHours()) + ":" + addZero(timestamp.getMinutes()) + ":" +
                            addZero(timestamp.getSeconds()) + "]\t" + self.player2().name() + " pasó su turno sin movimientos");
                    }
                }

                //Launch timer
                self.launchCountdown();

                //Cambio de letras
            } else if (jso.type == "NEW_LETTERS") {
                console.log("new letters")

            }

            //Fin de la partida
            else if (jso.type == "MATCH_END") {
                console.log("match end")
                if (jso.winner) {
                    self.showDialogWinner(true);
                } else {
                    self.showDialogLoser(true);
                }

            }
        }
    }

    this.llamar = function () {
        console.log("llamar")
        $(".letters li").draggable("enable");
    }

    /**
     * Panel de botones: JUGAR
     */
    this.jugar = function () {
        if (self.player1().turn) {
            if (self.tablero().casillasJugadas().length > 0) {
                var mensaje = {
                    type: "MOVIMIENTO",
                    idPartida: sessionStorage.getItem('idPartida'),
                    jugada: []
                };

                self.tablero().casillasJugadas().forEach(function (element) {
                    var casilla = {
                        row: element.row,
                        column: element.column,
                        letter: element.letter,
                    }
                    mensaje['jugada'].push(casilla);
                });
                self.ws.send(JSON.stringify(mensaje));

                console.log(self.tablero().casillasJugadas())
            } else {
                //Error
                self.showLoserModal();
                console.log("no hay jugadas")
            }

        } else {
            console.log("no tienes el turno")
        }

    }

    //Panel de botones: JUGAR
    this.pasar = function () {
        if (self.player1().turn) {
            var mensaje = {
                type: "PASO_TURNO",
                idPartida: sessionStorage.getItem('idPartida'),
            };
            self.ws.send(JSON.stringify(mensaje));

        } else {
            console.log("no tienes el turno")
        }
    }

    //Panel de botones: CAMBIAR
    this.cambiar = function () {
        self.showDialogCambioLetras(true)
    }


    /**
     * Panel de botones: RENDIRSE
     */
    this.abandonar = function () {
        console.log("rendirse")
        var mensaje = {
            type: "ABANDONO",
            idPartida: sessionStorage.getItem('idPartida'),
        };
        self.ws.send(JSON.stringify(mensaje));
    }

}


/**
 * 
 */
class Player {

    /**
     * Constructor de la clase Player
     * @param ko 
     * @param name 
     * @param email 
     * @param photo 
     */
    constructor(ko, name = null, email = null, photo = null) {
        if (name) {
            this.name = ko.observable(name);
            this.letterImg = ko.observable("assets/images/" + name.charAt(0).toUpperCase() + ".svg");
            this.letterTxt = ko.observable(name.charAt(0).toUpperCase());
        } else {
            this.name = ko.observable();
            this.letterImg = ko.observable();
            this.letterTxt = ko.observable();
        }

        this.score = ko.observable(0);
        this.turn = ko.observable();
        this.email = (email ? ko.observable(email) : ko.observable());
        this.photo = ko.observable(photo ? photo : "assets/images/default-user-icon.jpg");
    }
}

/**
 * Clase Tablero
 * 
 */
class Tablero {
    constructor(ko) {

        //Construir tablero
        this.casillasNoKO = new Array();

        for (var i = 0; i < 15; i++) {
            this.casillasNoKO.push(new Array());
            for (var j = 0; j < 15; j++)
                this.casillasNoKO[i][j] = new Casilla(ko, this, i, j);
        }

        //Asignar casillas "Triple valor de palabra"
        var tp = [
            [0, 2],
            [0, 12],
            [2, 0],
            [2, 14],
            [12, 0],
            [12, 14],
            [14, 2],
            [14, 12]
        ];
        for (var i = 0; i < tp.length; i++) {
            var coords = tp[i];
            this.casillasNoKO[coords[0]][coords[1]].label("TP");
            this.casillasNoKO[coords[0]][coords[1]].clazz("scrabble-td triple-word");
        }

        //Asignar casillas "Triple valor de letra"
        var tl = [
            [0, 4],
            [0, 10],
            [1, 1],
            [1, 13],
            [2, 6],
            [2, 8],
            [3, 3],
            [3, 11],
            [4, 0],
            [4, 14],
            [5, 5],
            [5, 9],
            [6, 2],
            [6, 12],
            [8, 2],
            [8, 12],
            [9, 5],
            [9, 9],
            [10, 0],
            [10, 14],
            [11, 3],
            [11, 11],
            [12, 6],
            [12, 8],
            [13, 1],
            [13, 13],
            [14, 4],
            [14, 10]
        ];
        for (var i = 0; i < tl.length; i++) {
            var coords = tl[i];
            this.casillasNoKO[coords[0]][coords[1]].label("TL");
            this.casillasNoKO[coords[0]][coords[1]].clazz("scrabble-td triple-letter");
        }

        //Asignar casillas "Doble valor de palabra"
        var dp = [
            [1, 5],
            [1, 9],
            [3, 7],
            [5, 1],
            [5, 13],
            [7, 3],
            [7, 11],
            [9, 1],
            [9, 13],
            [11, 7],
            [13, 5],
            [13, 9]
        ];
        for (var i = 0; i < dp.length; i++) {
            var coords = dp[i];
            this.casillasNoKO[coords[0]][coords[1]].label("DP");
            this.casillasNoKO[coords[0]][coords[1]].clazz("scrabble-td double-word");
        }

        //Asignar casillas "Doble valor de letra"
        var dl = [
            [2, 2],
            [2, 12],
            [4, 6],
            [4, 8],
            [6, 4],
            [6, 10],
            [8, 4],
            [8, 10],
            [10, 6],
            [10, 8],
            [12, 2],
            [12, 12]
        ];
        for (var i = 0; i < dl.length; i++) {
            var coords = dl[i];
            this.casillasNoKO[coords[0]][coords[1]].label("DL");
            this.casillasNoKO[coords[0]][coords[1]].clazz("scrabble-td double-letter");
        }

        //Casilla central
        this.casillasNoKO[7][7].label("★");
        this.casillasNoKO[7][7].clazz("scrabble-td star");

        //Convertir array de casillas en observable
        this.casillas = ko.observableArray(this.casillasNoKO);
        this.casillasJugadas = ko.observableArray();

        //Panel de letras, inicialmente vacío
        this.panel = ko.observableArray([]);

        //Fichas disponibles en el servidor
        this.fichasRestantes = ko.observable(0);
    }
}

/**
 * Clase Casilla
 */
class Casilla {
    constructor(ko, tablero, row, column) {
        this.tablero = tablero;
        this.label = ko.observable('');
        this.letter = '';
        this.clazz = ko.observable("scrabble-td");
        this.row = row;
        this.column = column;
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


/**
 * UTILS
 */
function addZero(i) {
    if (i < 10) {
        i = "0" + i;
    }
    return i;
}