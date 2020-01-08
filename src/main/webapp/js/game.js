function GameViewModel(user) {
    var self = this;
    self.ws = null;

    //General: controlling hide & show tablero and isLoading animation
    this.shouldShowBoard = ko.observable(false);
    this.isLoadingGame = ko.observable(false);
    this.isLoadingMsg = ko.observable();

    /***********************************
     * Data bindings
     ***********************************/

    //User (Player 1) properties
    this.player1 = ko.observable(new Player(ko, user.userName, user.email, user.photo));

    //Player 2 properties
    this.player2 = ko.observable(new Player(ko));

    //Tablero
    this.tablero = ko.observable(new Tablero(ko));
    console.log("tablero:")
    console.log(self.tablero().casillas())


    //Controller for Popups
    this.shouldShowPopup = ko.observable(false);

    //Movement history. Initially an empty array
    this.movementHistory = ko.observableArray();


    /***********************************
     * jQuery dialogs and modals
     ***********************************/
    ko.bindingHandlers.modal = {
        init: function (element, valueAccessor) {
            $(element).modal({
                show: false,
                backdrop: 'static',
                keyboard: false
            });

            var value = valueAccessor();
            if (ko.isObservable(value)) {
                $(element).on('hidden.bs.modal', function () {
                    value(false);
                });
            }
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
    this.showDialogProfileImage = ko.observable(false);

    this.closeModals = function () {
        self.showDialogLoser(false);
        self.showDialogWinner(false);
        self.showMatchesDialog(false);
        self.showDialogConfirmarJugada(false);
        self.showDialogCambioLetras(false);
        self.showDialogProfileImage(false);
    }

    this.displayDialogWinner = function () {
        self.closeModals();
        self.stopCountdown();
        self.showDialogWinner(true);
    }

    this.displayDialogLoser = function () {
        self.closeModals();
        self.stopCountdown();
        self.showDialogLoser(true);
    }

    /***********************************
     * Notificaciones
     ***********************************/
    this.notificationClass = ko.observable();
    this.notificationMessage = ko.observable();
    this.showNotification = ko.observable(false);
    this.displayNotification = function (clazz, message) {
        //Restart a CSS Animation
        //https://css-tricks.com/restart-css-animation/

        var el = document.getElementById('notification');
        el.style.animation = 'none';
        el.offsetHeight; /* trigger reflow */
        el.style.animation = null;

        self.notificationClass(clazz);
        self.notificationMessage(message);
        self.showNotification(true)
    }

    this.closeNotification = function () {
        self.showNotification(false);
    }

    /***********************************
     * Solicitar historial de partidas
     ***********************************/
    this.showMatchesDialog = ko.observable(false);
    this.closeMatchesDialog = function () {
        self.showMatchesDialog(false);
    }
    this.getMatches = function () {
        
        var data = {
            url: "/matches",
            type: "get",
            success: getMatchesOK,
            error: getMatchesError
        };
        $.ajax(data);
        self.showMatchesDialog(true);
    }

    function getMatchesOK (response) {
        console.log(response)
    }

    function getMatchesError(response) {
        console.log("Error getting match")
    }

    /***********************************
     * Cambiar imagen de perfil
     ***********************************/

    //Drag and drop no implementado:
    //https://www.smashingmagazine.com/2018/01/drag-drop-file-uploader-vanilla-js/
    //https://codepen.io/safrazik/pen/uIrwC

    this.selectProfileImage = function () {
        self.closeModals();
        self.showDialogProfileImage(true);
    }

    //base 64 encoded file
    this.photoEncoded = ko.observable();
    this.photoUrl = ko.observable();

    //https://stackoverflow.com/questions/27958047/file-upload-using-knockout-js
    this.fileUpload = function (data, e) {
        var file = e.target.files[0];
        self.photoUrl(file.name)
        var reader = new FileReader();

        reader.onloadend = function (onloadend_e) {
            var result = reader.result;
            self.photoEncoded(result);
            console.log(result)
        };

        if (file) {
            reader.readAsDataURL(file);
        }
    };

    this.updateImage = function () {
        self.closeModals();


        if (self.photoEncoded()) {
            //Send image to server
            var info = {
                base64Image: self.photoEncoded(),
            };

            var data = {
                data: info,
                url: "/updatePhoto",
                type: "post",
                success: updatePhotoOK,
                error: updatePhotoError
            };
            $.ajax(data);
        } else {
            self.displayNotification("notification-error", "No ha seleccionado ninguna imagen");
        }
    }

    function updatePhotoOK(response) {
        self.player1().photo(self.photoEncoded())
        self.photoEncoded(null)
        self.photoUrl(null)
        self.displayNotification("notification-success", "Foto de perfil actualizada");
    }

    function updatePhotoError(response) {
        self.displayNotification("notification-error", "No se pudo actualizar la foto de perfil");
        self.photoEncoded(null)
        self.photoUrl(null)
    }


    /***********************************
     * Cambio de letras
     ***********************************/

    //Este array contiene las letras que se marcan para ser cambiadas 
    //cuando se abre el popup para seleccionar letras. Lo que se guarda en él no
    //es la letra, sino la posición de la letra dentro del panel. Lo hacemos
    //así para evitar problemas cuando hay varias letras iguales y solo se quiere 
    //cambiar una de ellas. 
    this.lettersToChange = ko.observableArray();

    this.cambiarLetrasCancel = function () {
        self.showDialogCambioLetras(false);
        self.lettersToChange.removeAll();
    }

    this.cambiarLetrasConfirm = function () {
        self.showDialogCambioLetras(false);
        var letters = [];
        self.lettersToChange().forEach(function (ficha) {
            letters.push(ficha.letter());
        });

        console.log("letras a cambiar:");
        console.log(letters)

        var mensaje = {
            type: "CAMBIO_LETRAS",
            idPartida: sessionStorage.getItem('idPartida'),
            letters: letters
        };
        self.ws.send(JSON.stringify(mensaje));
    }


    /***********************************
     * Timer
     ***********************************/
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
                //self.abandonar();
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

    /***********************************
     * Dragging and Dropping
     ***********************************/
    //https://jqueryui.com/draggable/
    //https://jqueryui.com/droppable/
    //http://www.knockmeout.net/2011/05/dragging-dropping-and-sorting-with.html
    this.fichaEnMovimiento = ko.observable();

    //Draggable element
    ko.bindingHandlers.draggable = {
        init: function (element, valueAccessor, allBindings, data, bindingContext) {
            $(element).draggable({
                scroll: false,
                revert: "invalid",
                snap: ".scrabble-td",

                //Evento lanzado antes de que un jugador mueva una ficha.
                //data es una ficha. Le ponemos su atributo casilla a null. Esto signfica
                //que al levantar una ficha (drag) deja de estar asignada a una casilla, si lo estaba.
                //Si la ficha estaba en el panel, no tiene ningún efecto, porque casilla ya era null
                //data.casilla(null);
                start: function () {
                    if (data.casilla() !== null) {

                        //La quitamos de casillas jugadas
                        self.tablero().casillasJugadas.remove(function (casillaJugada) {
                            return ((casillaJugada.row == data.casilla().row) && (casillaJugada.column == data.casilla().column));
                        });
                        data.casilla().ficha(null);
                        data.casilla(null);
                    }
                    self.fichaEnMovimiento(data);
                }
                //grid: [40,40]
            });
        }
    };


    //Droppable element
    ko.bindingHandlers.droppable = {
        init: function (element, valueAccessor, allBindings, data, bindingContext) {
            $(element).droppable({

                accept: function (dropedElement) {
                    // comprueba si hay una ficha en esa casilla con id temporal
                    /*
                    if (data.letter != '') {
                        var row = ui.draggable.id.split(",")[1];
                        var column = ui.draggable.id.split(",")[2];
                        var bool = true;
                        self.tablero().casillasJugadas().forEach(function (item, index, object) {
                            if (item.row == row && item.column == column) {
                                bool = false;
                            }
                        });
                        return bool;
                    } else {
                        return true;
                    }
                    */
                    if (data.fixed()) {
                        return false;
                    }
                    return (data.ficha() === null);

                },
                //start: function (event, ui) {},
                drop: function (event, ui) {
                    /*
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
                    */
                    ui.draggable.removeClass('onboard');
                    ui.draggable.toggleClass('onboard');

                    //data: objeto Casilla sobre el que se suelta la ficha
                    //element: elemento HTML sobre el que se suelta la ficha (td)
                    var letter = self.fichaEnMovimiento().letter();
                    console.log("Has soltado la letra " + letter + " en fila:" + data.row + ", col:" + data.column);
                    self.fichaEnMovimiento().casilla(data);
                    data.ficha(self.fichaEnMovimiento());
                    self.fichaEnMovimiento(null);
                    self.tablero().casillasJugadas().push(data);
                },
            });
        }
    };

    /***********************************
     * Sortable
     ***********************************/
    //Permite reordenar las fichas en el panel 
    //utilizando drag and drop
    //https://jqueryui.com/sortable/
    //http://www.knockmeout.net/2011/05/dragging-dropping-and-sorting-with.html
    ko.bindingHandlers.sortable = {
        init: function (element, valueAccessor) {
            var list = valueAccessor();
            $(element).sortable({
                update: function (event, ui) {
                    console.log("update")
                    //retrieve our actual data item
                    var item = ui.item.tmplItem().data;
                    //figure out its new position
                    var position = ko.utils.arrayIndexOf(ui.item.parent().children(), ui.item[0]);
                    //remove the item and add it back in the right spot
                    if (position >= 0) {
                        list.remove(item);
                        list.splice(position, 0, item);
                    }
                    ui.item.remove();
                }
            });
        }
    };

    /***********************************
     * Loading circle animation
     ***********************************/
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

    /***********************************
     * Logout controller
     ***********************************/
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

    /***********************************
     * Create and join games
     ***********************************/
    this.createGame = function () {
        console.log("createGame")
        var data = {
            data: {},
            url: "/createMatch",
            type: "post",
            success: gameOK,
            error: gameError
        };
        self.isLoadingGame(true);
        self.isLoadingMsg("Creando partida y esperando oponente")
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
        self.isLoadingGame(true);
        self.isLoadingMsg("Buscando partidas disponibles")
        $.ajax(data);
    }

    function gameError(response) {
        self.isLoadingGame(false);
        var mensaje = "Error desconocido"
        if (response.responseJSON && response.responseJSON.message) {
            mensaje = response.responseJSON.message
        }
        self.displayNotification("notification-error", mensaje);
    }


    /***********************************
     * Finalizar partida
     ***********************************/
    this.submitEnd = function () {
        self.closeModals();
        endMatch();
    }

    function endMatch() {
        self.lettersToChange([]);
        self.movementHistory([]);
        self.tablero(new Tablero(ko));
        self.shouldShowBoard(false);
        sessionStorage.removeItem("idPartida");
        self.stopCountdown();
        self.player2 = ko.observable(new Player(ko));
    }


    /***********************************
     * Crear ws y controlar mensajes entrantes
     ***********************************/
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
            self.displayNotification("notification-error", `on error`);
        }

        self.ws.onclose = function (event) {
            self.displayNotification("notification-warning", `Websocket cerrado`);
            endMatch();
        }


        self.ws.onmessage = function (event) {
            var jso = event.data;
            jso = JSON.parse(jso);
            console.log("Mensaje recibido del tipo: " + jso.type);
            console.log(JSON.stringify(jso))

            //Inicio de partida 
            if (jso.type == "START") {
                console.log("start")
                self.shouldShowBoard(true);
                self.isLoadingGame(false);
                self.player1().turn(jso.turn)
                self.player2().turn(!jso.turn);
                self.player2().name(jso.opponent);
                self.player2().letterImg("assets/images/" + jso.opponent.charAt(0).toUpperCase() + ".svg");
                self.player2().letterTxt(jso.opponent.charAt(0).toUpperCase());

                //Binding letters
                var letters = jso.letters.split(' ');
                for (var i = 0; i < letters.length; i++) {
                    //self.tablero().panel.push(letters[i]);
                    self.tablero().panel.push(new Ficha(ko, letters[i], false));
                }

                //Define available pieces
                self.tablero().availablePieces(jso.availablePieces);

                //Iniciar temporizador
                if (jso.turn) {
                    self.launchCountdown();
                }
            }

            //Respuesta del servidor al movimiento de un jugador
            else if (jso.type == "RESULT") {
                if (jso.exceptions && jso.exceptions.length > 0) {
                    if (jso.exceptions.length == 1) {
                        self.displayNotification("notification-warning", jso.exceptions[0]);
                    } else {
                        var problems = "";
                        jso.exceptions.forEach(function (problem) {
                            problems += problem + ". ";
                        });
                        self.displayNotification("notification-warning", "Esta jugada tiene algunos problemas: " + problems);
                    }

                } else if (jso.invalid && jso.invalid.length > 0) {
                    var jsoInvalids = jso.invalid;
                    var strInvalids = '';
                    jsoInvalids.forEach(function (invalid) {
                        strInvalids += invalid.sequence + ", ";
                    });
                    strInvalids = strInvalids.slice(0, -2);

                    self.displayNotification("notification-warning", `Algunas de las palabras no están aceptadas: ${strInvalids}`);
                } else {
                    console.log("todas validas")
                    //Preparar dialógo confirmar jugada
                    var score = 0;
                    self.resultadoJugada(new ResultadoJugada(ko));

                    var jsoValids = jso.valid;
                    jsoValids.forEach(function (valid) {
                        self.resultadoJugada().palabras.push(`Jugarás '${valid.sequence}' por ${valid.points} puntos`);
                        score += valid.points;
                    });
                    self.resultadoJugada().puntosTotal(score);


                    //Mostrar diálogo
                    self.showDialogConfirmarJugada(true);
                }

            }

            //El jugador ha hecho un movimiento y ha sido confirmado
            else if (jso.type == "MOVEMENT") {
                console.log("movimiento: ")
                console.log(JSON.stringify(jso))
                if (jso.exceptions && jso.exceptions.length > 0) {
                    if (jso.exceptions.length == 1) {
                        self.displayNotification("notification-warning", jso.exceptions[0]);
                    } else {
                        var problems = "";
                        jso.exceptions.forEach(function (problem) {
                            problems += problem + ". ";
                        });
                        self.displayNotification("notification-warning", "Esta jugada tiene algunos problemas: " + problems);
                    }
                } else {
                    self.player1().turn(false);
                    self.player2().turn(true);
                    self.player1().score(jso.score);
                    self.tablero().availablePieces(jso.availablePieces);
                    if (jso.valid && jso.valid.length > 0) {
                        var timestamp = new Date();
                        var jugadas = jso.valid;
                        jugadas.forEach(function (jugada) {
                            self.movementHistory.unshift("[" + addZero(timestamp.getHours()) + ":" + addZero(timestamp.getMinutes()) + ":" +
                                addZero(timestamp.getSeconds()) + "]\t" + self.player1().name() + ` jugó ${jugada.sequence} por ${jugada.points} puntos`);
                        });

                        casillasPermanentes(jso.valid);

                        //Vaciar casillas jugadas y quitar del panel
                        for (var i = 0; i < self.tablero().casillasJugadas().length; i++) {
                            self.tablero().panel.remove(self.tablero().casillasJugadas()[i].ficha());
                            console.log(self.tablero().casillasJugadas()[i].letter());
                            console.log("panel con " + self.tablero().panel().length + " letras")
                        }
                        self.tablero().casillasJugadas.removeAll();

                        //Binding letters new letters
                        var letters = jso.letters.split(' ');
                        for (var i = 0; i < letters.length; i++) {
                            //self.tablero().panel.push(letters[i]);
                            console.log("nueva letra: " + letters[i])
                            self.tablero().panel.push(new Ficha(ko, letters[i], false));
                        }
                        console.log("Panel después de actualizar index:")
                        console.log(self.tablero().panel())

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
                console.log("movimiento del rival: ")
                console.log(JSON.stringify(jso))
                if (jso.exceptions && jso.exceptions.length > 0) {

                } else {
                    self.player1().turn(true);
                    self.player2().turn(false);
                    self.player2().score(jso.score);
                    self.tablero().availablePieces(jso.availablePieces);
                    if (jso.valid && jso.valid.length > 0) {
                        var timestamp = new Date();
                        var jugadas = jso.valid;
                        jugadas.forEach(function (jugada) {
                            self.movementHistory.unshift("[" + addZero(timestamp.getHours()) + ":" + addZero(timestamp.getMinutes()) + ":" +
                                addZero(timestamp.getSeconds()) + "]\t" + self.player2().name() + ` jugó ${jugada.sequence} por ${jugada.points} puntos`);
                        });
                        casillasPermanentes(jso.valid);
                    } else {
                        var timestamp = new Date();
                        self.movementHistory.unshift("[" + addZero(timestamp.getHours()) + ":" + addZero(timestamp.getMinutes()) + ":" +
                            addZero(timestamp.getSeconds()) + "]\t" + self.player2().name() + " pasó su turno sin movimientos");

                    }
                }

                //Launch timer
                self.launchCountdown();

                //Cambio de letras
            }

            //El jugador recibe nuevas letras después de solicitarlo
            else if (jso.type == "NEW_LETTERS") {
                var newLetters = jso.letters;

                if (newLetters && newLetters.length > 0) {
                    newLetters = newLetters.split(" ");
                    for (var i = 0; i < newLetters.length; i++) {
                        self.lettersToChange()[i].letter(newLetters[i]);
                    }
                    self.displayNotification("notification-success", `Recibidas ${newLetters.length} nuevas letras: ` + jso.letters);
                } else {
                    self.displayNotification("notification-error", `No se pudo realizar el cambio de letras. Puede que la partida no disponga de letras restantes suficientes`);
                }

                self.lettersToChange.removeAll();

            }

            //Fin de la partida
            else if (jso.type == "MATCH_END") {
                console.log("match end")
                if (jso.winner) {
                    self.displayDialogWinner();
                } else {
                    self.displayDialogLoser();
                }

            }
        }
    }

    /***********************************
     * CONFIRMAR UNA JUGADA
     ***********************************/
    this.showDialogConfirmarJugada = ko.observable(false);
    this.resultadoJugada = ko.observable();

    this.cancelarJugada = function () {
        self.showDialogConfirmarJugada(false);
    }

    this.confirmarJugada = function () {
        var mensaje = {
            type: "CONFIRMAR_JUGADA",
            idPartida: sessionStorage.getItem('idPartida'),
        };
        self.ws.send(JSON.stringify(mensaje));
        self.showDialogConfirmarJugada(false);
    }

    function casillasPermanentes(jugadas) {
        jugadas.forEach(function (jugada) {
            var squares = jugada.square;
            squares.forEach(function (square) {
                self.tablero().casillas()[square.row][square.col].fixed(true);
                self.tablero().casillas()[square.row][square.col].letter(square.letter);
            });
        });

    };


    /***********************************
     * CONTROLES DEL JUEGO
     ***********************************/

    /**
     * Panel de botones: Llamar
     * Las letras que se han colocado en el tablero vuelven de nuevo al panel
     */
    this.llamar = function () {
        //Borrar casillas jugadas y quitarlas del tablero
        self.tablero().casillasJugadas.remove(function (casillaJugada) {
            casillaJugada.ficha().casilla(null);
            casillaJugada.ficha(null);
            return true;
        });

        $(".letters li").animate({
            top: "0px",
            left: "0px"
        });
    }


    /**
     * Panel de botones: MEZCLAR
     * Si el jugador desea que su cliente reeordene aleatoriamente las letras que 
     * tiene en su panel
     */
    this.mezclar = function () {
        //Ver que elementos son ordenables y su posición en el panel
        var elementosOrdenables = [];
        var posicionesOrdenables = [];
        for (var i = 0; i < self.tablero().panel().length; i++) {
            if (self.tablero().panel()[i].casilla() === null) {
                elementosOrdenables.push(self.tablero().panel()[i]);
                posicionesOrdenables.push(i);
            }
        }
        //Ordenar aleatoriamente los elementos ordenables
        posicionesOrdenables = shuffleArray(posicionesOrdenables);

        elementosOrdenables.forEach(function (fichaOrdenable) {
            var index = posicionesOrdenables.pop();
            self.tablero().panel.replace(self.tablero().panel()[index], fichaOrdenable);

        });
    }


    /**
     * Panel de botones: JUGAR.
     * El servidor valida si todas las palabras que se puedan formar son correctas.
     * Si alguno no es correcta, se muestra al usuario un mensaje de error, pero el turno
     * sigue siendo de este jugador.
     * Si todas las palabras son correctas, el servidor informa de la puntuación que se conseguirá
     * y le solicita al cliente confirmación de la jugada.
     */
    this.jugar = function () {
        if (self.player1().turn) {
            if (self.tablero().casillasJugadas().length > 0) {
                var mensaje = {
                    type: "MOVIMIENTO",
                    idPartida: sessionStorage.getItem('idPartida'),
                    jugada: []
                };
                console.log(self.tablero().casillasJugadas())
                self.tablero().casillasJugadas().forEach(function (element) {
                    console.log("(" + element.row + ", " + element.column);
                    console.log("letra: " + element.ficha().letter());
                    var casilla = {
                        row: element.row,
                        col: element.column,
                        letter: element.ficha().letter(),
                    }
                    mensaje['jugada'].push(casilla);
                });
                console.log("Jugada: ")
                console.log(JSON.stringify(mensaje))
                self.ws.send(JSON.stringify(mensaje));

                console.log(self.tablero().casillasJugadas())
            } else {
                console.log("no hay movimiento")
                self.displayNotification("notification-warning", `No has hecho ningún movimiento`);
            }

        } else {
            self.displayNotification("notification-warning", "No tienes el turno");
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

    /**
     * Panel de botones: PASAR
     * Pasar el turno al siguiente jugador
     */
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

    /**
     * Panel de botones: CAMBIAR LETRAS
     * 
     */
    this.cambiar = function () {
        self.showDialogCambioLetras(true)
    }

    /**
     * Panel de botones: RENDIRSE.
     * Si un jugador se rinde se le da la partida por perdida.
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
        this.availablePieces = ko.observable(95);
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
        //Indica si la casilla está definida por una
        //letra confirmada por el servidor
        this.fixed = ko.observable(false);
        this.clazz = ko.observable("scrabble-td");
        this.row = row;
        this.column = column;
        this.ficha = ko.observable(null);
    }
}

/**
 * Clase Ficha
 */
class Ficha {
    constructor(ko, letra, inTablero) {
        //Index es necesario para hacer el binding con el elemento HTML en cambio de letras 
        //this.index = ko.observable(index);
        this.inTablero = ko.observable(inTablero);
        this.letter = ko.observable(letra);
        this.casilla = ko.observable(null);
    }
}

/**
 * Clase ResultadoJugada
 */
class ResultadoJugada {
    constructor(ko) {
        this.puntosTotal = ko.observable(0);
        this.palabras = ko.observableArray();
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


/***********************************
 * UTILS
 ***********************************/
function addZero(i) {
    if (i < 10) {
        i = "0" + i;
    }
    return i;
}

/**
 * https://stackoverflow.com/questions/2450954/how-to-randomize-shuffle-a-javascript-array
 * 
 * Fisher-Yates (aka Knuth) Shuffle
 */
function shuffleArray(array) {
    var currentIndex = array.length,
        temporaryValue, randomIndex;

    // While there remain elements to shuffle...
    while (0 !== currentIndex) {

        // Pick a remaining element...
        randomIndex = Math.floor(Math.random() * currentIndex);
        currentIndex -= 1;

        // And swap it with the current element.
        temporaryValue = array[currentIndex];
        array[currentIndex] = array[randomIndex];
        array[randomIndex] = temporaryValue;
    }

    return array;
}