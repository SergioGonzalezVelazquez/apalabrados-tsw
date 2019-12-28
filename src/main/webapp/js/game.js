function GameViewModel(user) {
    var self = this;
    self.ws = null;

    //General: controlling hide & show tablero and isLoading animation
    this.shouldShowBoard = ko.observable(false);
    this.isLoading = ko.observable(false);

    //User (Player 1) properties
    this.player1 = ko.observable(new Player(ko, user.userName, user.email, user.photo));

    /*
    this.userEmail = ko.observable(user.email);
    this.userName = ko.observable(user.userName);
    this.userPhoto = ko.observable(user.photo ? user.photo : "assets/images/default-user-icon.jpg");
    this.p1Turn = ko.observable();
    this.p1Score = ko.observable();
    this.p1LetterImg = ko.observable("assets/images/" + user.userName.charAt(0).toUpperCase() + ".svg");
    this.p1LetterTxt = ko.observable(user.userName.charAt(0).toUpperCase());
    */

    //Player 2 properties
    this.player2 = ko.observable(new Player(ko));
    /*
    this.p2Name = ko.observable();
    this.p2LetterImg = ko.observable();
    this.p2LetterTxt = ko.observable();
    this.p2Score = ko.observable();
    this.p2Turn = ko.observable();
    */


    //Tablero
    this.tablero = ko.observable(new Tablero(ko));

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
                    console.log("accept")
                    //console.log(bindingContext.$data)
                    return true;
                },

                drop: function (event, ui) {

                    console.log("Has soltado la letra en ")
                    console.log(bindingContext.$data)


                },
            });
        }
    };


    /*
    //Draggable element
    ko.bindingHandlers.draggable = {
        init: function (element, valueAccessor) {
            var dragElement = $(element);
            var dragOptions = {
                helper: 'clone',
                revert: true,
                revertDuration: 0,
                start: function () {
                    _dragged = ko.utils.unwrapObservable(valueAccessor().value);
                },
                cursor: 'default'
            };
            dragElement.draggable(dragOptions).disableSelection();
            console.log("draggable")
        }
    };

    //Droppable element
    ko.bindingHandlers.droppable = {
        init: function (element, valueAccessor) {
            var dropElement = $(element);
            var dropOptions = {
                drop: function (event, ui) {
                    valueAccessor().value(_dragged);
                }
            };
            dropElement.droppable(dropOptions);
            console.log("droppable")
        }
    };
    */
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
                    self.shouldShowBoard(true);
                    self.isLoading(false);
                    /*
                    self.p1Turn(jso.turn);
                    self.p2Turn(!jso.turn);
                    self.p2Name(jso.opponent);
                    self.p2LetterImg("assets/images/" + jso.opponent.charAt(0).toUpperCase() + ".svg");
                    self.p2LetterTxt(jso.opponent.charAt(0).toUpperCase());
                    */
                    console.log(jso)
                    self.player1().turn(jso.Turn)
                    self.player2().turn(!jso.turn);
                    self.player2().name(jso.opponent);
                    self.player2().letterImg("assets/images/" + jso.opponent.charAt(0).toUpperCase() + ".svg");
                    self.player2().letterTxt(jso.opponent.charAt(0).toUpperCase());

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



    //Panel de botones: JUGAR
    this.jugar = function () {}

    //Panel de botones: JUGAR
    this.pasar = function () {}

    //Panel de botones: CAMBIAR
    this.cambiar = function () {}

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
        this.turn = ko.observable(false);
        this.email = (email ? ko.observable(email) : ko.observable());
        this.photo = ko.observable(photo ? photo : "assets/images/default-user-icon.jpg");
    }

    setTurn(turn) {
        this.turn = turn;
        console.log("turno definido")
    }

    setName(name) {
        this.name = name;
        this.letterImg = ko.observable("assets/images/" + name.charAt(0).toUpperCase() + ".svg");
        this.letterTxt = ko.observable(name.charAt(0).toUpperCase());
        console.log("name cambiado A " + name)
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
        this.casillasJugadas = [];

        //Panel de letras, inicialmente vacío
        this.panel = ko.observableArray(['S', 'T', 'I', 'N', 'A', 'C', 'T']);
    }
}

/**
 * Clase Casilla
 */
class Casilla {
    constructor(ko, tablero, row, column) {
        this.tablero = tablero;
        this.label = ko.observable('');
        this.letter = ko.observable('');
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