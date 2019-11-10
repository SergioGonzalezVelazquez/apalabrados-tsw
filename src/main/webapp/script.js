$('.letters li').draggable({
	revert: "invalid",
	snap: ".scrabble-td"
	//grid: [40,40]
});

/*$('.letters li').droppable({
	drop: function(event, ui){
		console.log($(this));
		var valor= $(this)[0].innerHTML;
		if(valor==undefined){
			$(this)[0].innerHTML=ui.draggable[0].innerHTML;
		}else{
			$(this)[0].innerHTML=ui.draggable[0].innerHTML;
			ui.draggable[0].innerHTML=valor;
		}*/

/*var x = ui.draggable.attr('e-color');
var y = ui.draggable.attr('c-color');*/

/*console.log(ui.draggable[0].innerHTML);
	}
});
$('#board td').draggable({
	revert:true
});*/
$('#board td').droppable({
	
	accept: function(dropedElement){
		var newId="over"+$(this).attr("id");
		// Comprueba si existe una pieza en esa posición
		// falta revisar si existe la posición no está pillando el elemento por id
		if($("#" + newId).length == 0 && dropedElement.attr('class').search("played") == -1){
			
			return true;
		}else{
			return false;
		}},
		
	drop: function (event, ui) {
		var newId="over"+$(this).attr("id");
		
		console.log("Has soltado la letra en " + $(this).attr("id"));
		ui.draggable.removeAttr('id');
		ui.draggable.attr('id', newId);
		ui.draggable.removeClass('onboard');
		ui.draggable.toggleClass('onboard');

		
		
	}
});

$("#check").click(function () {
	var onboardList = {};
	var playedList = {};
	/* Relleno las dos listas con la información de las fichas nuevas */
	$(".onboard").each(function () {
		onboardList[$(this).attr('id')] = $("img", $(this)).first().attr("alt");
	});

	/* Relleno las dos listas con la información de las fichas  ya colocadas*/
	$(".played").each(function () {
		playedList[$(this).attr('id')] = $("img", $(this)).first().attr("alt");
	});
	if(Object.keys(onboardList).length >0){
		var valid = checkColocation(onboardList,playedList);
		console.log(valid);

		if (valid) {
			/* Comprueba las palabras formadas */
			var wordsAndPoints = checkWordsAndPoint(onboardList,playedList);
			console.log(wordsAndPoints);
			/* Cambia las piezas jugadas por las oscuras y añade clase played*/
			$(".onboard").each(function () {
				var letra = $("img", $(this)).first().attr("alt");
				var texto = "<img src=\"assets/images/dark/" + letra + "_dark.svg\" alt=\"" + letra + "\">";
				$(this).removeClass('onboard');
				$(this).toggleClass('played');
				$(this)[0].innerHTML = texto;
	
			});
		}else{
			console.log("Piezas mal colocadas")
		}
	}
	
});
/* Esta funcion separa las filas y columnas de cada coordenada y compara si estan en la misma fila o columna
Ademas comprueba si estan en la misma fila pero hay alguna separación entre fichas*/
function checkColocation(onboardList, playedList) {

	var allCoor = Object.keys(onboardList).slice();
	var rowCoor = [];
	var columnCoor = [];
	for (var i = 0; i < allCoor.length; i++) {
		var rawCoor = allCoor[i].split("-")[1].split(",");
		var row = rawCoor[0];
		var column = rawCoor[1];
		rowCoor.push(row);
		columnCoor.push(column);
	}
	rowCoor.sort();
	columnCoor.sort();
	console.log(rowCoor);
	console.log(columnCoor);
	/* Estos metodos comparan si todos los valores del array son iguales y devuelve true o false */
	var validRow = (rowCoor.every(
		function (value, _, array) {
			return array[0] === value;
		}
	));
	var firstValue=0;
	var lastValue=0;
	var position="";
	if(validRow){
		firstValue=columnCoor[0];
		lastValue= columnCoor[columnCoor.length-1];
		var diferencia= lastValue-firstValue;
		for(var i=0; i<diferencia; i++){
			position=(("overgrid-" + (parseInt(rowCoor[0])) + "," + (i + parseInt(columnCoor[0]))));
			if(onboardList[position] == undefined && playedList[position] ==undefined){
				validRow=false;
			}
				
		}
	}
	var validColumn = (columnCoor.every(
		function (value, _, array) {
			return array[0] === value;
		}
	));
	if(validColumn){
		firstValue=rowCoor[0];
		lastValue= rowCoor[rowCoor.length-1];
		var diferencia= lastValue-firstValue;
		for(var i=0; i<diferencia; i++){
			position=(("overgrid-" + (i + parseInt(rowCoor[0]))+ "," +(columnCoor[0])));
			if(onboardList[position] == undefined && playedList[position] ==undefined){
				validColumn=false;
			}
				
		}
	}
	if (validRow || validColumn) {
		return true
	} else {
		return false
	}

}

// Busca las coordenadas de la letra
// Letterid es son las coordenadas de la letra
function checkWordsAndPoint(onboardList, playedList) {

	var wordsEdited = findWords(onboardList, playedList);
	Object.keys(wordsEdited).forEach(function (key) {
		if (key.length == 1) {
			delete wordsEdited[key];
		}

	});

	return wordsEdited;



}

function findWords(onboardList, playedList) {
	var totalWords = {};

	for (var i = 0; i < Object.keys(onboardList).length; ++i) {
		/* Almaceno los datos de cada letra para mandarlo como array junto con la lista de palabras*/

		var letterArray = [];
		var coor = Object.keys(onboardList)[i];
		var letter = onboardList[Object.keys(onboardList)[i]];
		letterArray = [coor, letter];
		var horizontalWord = horizontalSearch(letterArray, onboardList, playedList);
		totalWords[horizontalWord[0]] = horizontalWord[1];
		var verticalWord = verticalSearch(letterArray, onboardList, playedList);
		totalWords[verticalWord[0]] = verticalWord[1];
	}
	return totalWords;
}

function horizontalSearch(letterArray, onboardList, playedList) {
	var rawCoor = letterArray[0].split("-")[1].split(",");
	var row = rawCoor[0];
	var column = rawCoor[1];
	var word = "";
	var finished = false;
	var points = 0;
	/* se inicializa i en 0 y luego cambia a -1 para no contarse a si misma dos veces*/
	var i = 0;
	var direction = true;
	while (finished == false) {

		var position = ("overgrid-" + row + "," + (i + parseInt(column)));
		if (onboardList[position] != undefined) {
			/* Falta comprobar puntos que da*/
			if (direction == true) {
				word = word + onboardList[position];
			} else {
				word = onboardList[position] + word;
			}
		}
		if (playedList[position] != undefined) {
			/* Falta comprobar puntos que da*/
			if (direction == true) {
				word = word + playedList[position];
			} else {
				word = playedList[position] + word;
			}
		}
		if (onboardList[position] == undefined && playedList[position] == undefined && direction) {
			i = 0;
			direction = false;
		}

		if (onboardList[position] == undefined && playedList[position] == undefined && !direction && i != 0) {
			finished = true;
		}
		if (direction) {
			i++;
		} else {
			i--;
		}
	}

	return [word, points];
}

function verticalSearch(letterArray, onboardList, playedList) {
	var rawCoor = letterArray[0].split("-")[1].split(",");
	var row = rawCoor[0];
	var column = rawCoor[1];
	var word = "";
	var finished = false;
	var points = 0;
	/* se inicializa i en 0 y luego cambia a -1 para no contarse a si misma dos veces*/
	var i = 0;
	var direction = true;
	while (finished == false) {

		var position = ("overgrid-" + (i + parseInt(row)) + "," + (column));
		if (onboardList[position] != undefined) {
			/* Falta comprobar puntos que da*/
			if (direction == true) {
				word = word + onboardList[position];
			} else {
				word = onboardList[position] + word;
			}
		}
		if (playedList[position] != undefined) {
			/* Falta comprobar puntos que da*/
			if (direction == true) {
				word = word + playedList[position];
			} else {
				word = playedList[position] + word;
			}
		}
		if (onboardList[position] == undefined && playedList[position] == undefined && direction) {
			i = 0;
			direction = false;
		}

		if (onboardList[position] == undefined && playedList[position] == undefined && !direction && i != 0) {
			finished = true;
		}
		if (direction) {
			i++;
		} else {
			i--;
		}
	}
	console.log([word, points]);
	return [word, points];
}

function checkWords(list) {
	var listaInversa = list.slice();
	var candidates = [];
	var palabra1 = "";
	var palabra2 = "";
	var formada = false;
	$.each(list, function () {
		if ($(this)[0]["vecinos"].length == 1) {
			if (!formada) {
				palabra1 = palabra1 + $(this)[0]["letter"];
				formada = true;
			} else {
				palabra2 = palabra2 + $(this)[0]["letter"];
			}
		}
	});


}