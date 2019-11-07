

$('.letters li').draggable({
	revert:"invalid",
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
	accept: ".ui-draggable",
	drop: function(event, ui){
		console.log("Has soltado la letra en " + $(this).attr("id"))
		ui.draggable.attr('id',   $(this).attr("id"));
		ui.draggable.toggleClass('onboard');
	}
});

$("#check").click(function() {
	var list=[];
$(".onboard").each(function() {
    if($(this).attr('id') != undefined){
		list.push({letter: $("img", $(this)).first().attr("alt") , coor: $(this).attr('id')});
		console.log($(this).attr('id'));
		console.log($list);
		buscarVecinos($(this).attr('id'));
	}
});


});

function buscarVecinos($param) {
	var valores=$param.split("-")[1].split(",");
	var fila=valores[0];
	var columna=valores[1];
	for (i = -1; i < 2; ++i) {
		for (j = -1; j < 2; ++j) {
			
			$(".onboard").each(function() {
				if($(this).attr('id') != undefined){
					
					var valoresleidos=$(this).attr('id');
					console.log("original"+("grid-"+(i+parseInt(fila))+","+(j+parseInt(columna))));
					console.log("leido"+ valoresleidos);
					if(valoresleidos == ("grid-"+(i+fila)+","+(j+columna)) ){
						console.log("vecinos"+ $("img", $(this)).first().attr("alt"));
						console.log("aaaaaaaaaaaa");
					}
				}
			});
		}
		// do something with `substr[i]`
	}
	/*for (i = 0; i < list.length; ++i) {
		coor=$list[i].coor;
		// do something with `substr[i]`
	}*/

 }
 
  
/*$("#check").click(function() {
	var list=[];
$(".onboard").each(function() {
    if($(this).attr('id') != undefined){
		list.push(letter: $(this).attr('innerHTML') , coor: )
		console.log($(this).attr('id'));
	}
});
});*/