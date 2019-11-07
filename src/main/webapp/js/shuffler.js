var spacing = 0;
var offset = 40;
var count = 0;
var positions = [];

// First of all, lets absolutely position our elements

$("ul.letters li").css("float", "none").css("position", "absolute");

// As the size of the surrounding element will collapse, give it a fixed height:

$("ul.letters").css("height", "40px");
$("ul.letters li").each(function() {
  console.log($(this).attr('id'));
  if($(this).attr('id') == undefined){
    
  // One by one, create a position for each
  the_position = spacing + "px";

  // Add the position to an array
  positions.push(the_position);

  // Everything was absolutely positioned: so give the left property the correct distance
  $(this).css("left", the_position);

  // Store this letter's origianl position for reset
  $(this).attr("data-origin", the_position);

  // Increment the spacing by the specified amount from earlier
  spacing += offset;

  // Next letter, increase the count!
  count++;
}});

$("#shuffle").click(function() {
  
  // Duplicate array so we don't mess with the original here
  var shuffled_positions = positions.slice(0);

  // Use the cool fisherYates method of randomly ordering the new array (see later on)
  var shuffled_positions = fisherYates(shuffled_positions);

  // For each letter...
  $("ul.letters li").each(function() {
    if($(this).attr('id') == undefined){
    // Animate
    $(this).animate({
      // The left distance, to the first element in the array
      left: shuffled_positions[0]
    });

    // Remove the 'used' position from the array
    shuffled_positions.splice(0, 1);
  }});
});

// When you click restore...
$("#restore").click(function() {
  
  // For each letter
  $("ul.letters li").each(function() {
    if($(this).attr('id') == undefined){
    // Get its original position (set earlier and stored in the html)
    var pos = $(this).attr("data-origin");
    // Animate
    $(this).animate({
      // It's left distance
      left: pos
    });
  }});
});

//this is some cool algorithm that is recommended
function fisherYates(myArray) {
  var i = myArray.length;
  if (i == 0) return false;
  while (--i) {
    var j = Math.floor(Math.random() * (i + 1));
    var tempi = myArray[i];
    var tempj = myArray[j];
    myArray[i] = tempj;
    myArray[j] = tempi;
  }
  return myArray;
}
