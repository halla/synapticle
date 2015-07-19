var n_concurrent_items = 10;
var items_per_sec = 2.5;

words = [];
colors = [ "black", "darkblue", "darkred", "darkgreen", "darkolivegreen", "#555", "darkorange" ]
$.get('mm.json', function(data) {
  words = data;
});


divs = [];
//var printTimer;
//var cleanTimer;

$('document').ready(function() {
  //  var interval_new = 1000/items_per_sec;
  //  var interval_clean = interval_new * n_concurrent_items;
  //  printTimer = setInterval(printLoop, interval_new);
  //  cleanTimer = setInterval(cleanup, interval_clean);
    $("#textareaimport").click(importTextarea);    
    $("#screen").click(function() { $("#controls").toggle();  });
});

var importTextarea = function() {  
  words = $("#textareaimport").val().split('\n');  
  i = 0;
}

function genSize(div) { //TODO normal distribution might be a bit less boring than uniform distribution
    var add = getRandomInt(-6, 6);
    var s = (25 + add) + "px";
    div.css({ 'font-size': s });
}

function genColor(div) {
   var c = getRandomInt(0, colors.length -1);
   div.css({ color: colors[c] });
}

function genPosition(div) {
  
  var pos = function() {
    left_max = $(window).width() - 50; 
    top_max = $(window).height() - 50; 
    var left = getRandomInt(0, left_max);
    var top = getRandomInt(0, top_max);
    return { "left": left, "top": top }
  }
    
  for (var i = 0; i < 200; i++) {    
    var newPos = pos();    
    div.css(newPos);
    if (!doesOverlap(div)) {
      break;
    } else {
      	
    }
    
  }
}

function show(word) {
  w = $("<div>");
  w.addClass("item");
  w.text(word);
  $("#screen").append(w);    
  genSize(w);
  genPosition(w);  
  genColor(w);
  w.hide()
  
  w.show()
  w.fadeOut(7000);
  return w;
  
}

interval = 500;
timer = ''
function getInterval() {
    return interval;
}
function getRandomInt (min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}


var i = 0;
function printLoop() {
  
  if (i < words.length) {
	w = show(words[i]);
	divs.push(w);
	if($('#doRandomize').is(":checked")) {
	  i = getRandomInt(0, words.length-1);
	} else {	  
	  i++;
	}
  }
  
}

//TODO cleanup array
function cleanup() {
  for (var i = 0; i < divs.length; i++)  {    
      if (!divs[i].is(':visible')) {
	divs[i].remove();
      } else {
	divs = _.rest(divs, i);
	break;
      }
  }  
}


function doesOverlap(div) {  
  return _.some(divs, function(div2) { 
    var result =  overlaps(div, div2); 
    if (result == true) {
    }
    return result;
  });  
}

/*
 * http://stackoverflow.com/questions/4230029/jquery-javascript-collision-detection
 */
var overlaps = (function () {
    function getPositions( elem ) {
        var pos, width, height;
        pos = $( elem ).position();
        width = $( elem ).width();
        height = $( elem ).height();
        return [ [ pos.left, pos.left + width ], [ pos.top, pos.top + height ] ];
    }

    function comparePositions( p1, p2 ) {
        var r1, r2;
        r1 = p1[0] < p2[0] ? p1 : p2;
        r2 = p1[0] < p2[0] ? p2 : p1;
        return r1[1] > r2[0] || r1[0] === r2[0];
    }

    return function ( a, b ) {
        var pos1 = getPositions( a ),
            pos2 = getPositions( b );
        return comparePositions( pos1[0], pos2[0] ) && comparePositions( pos1[1], pos2[1] );
    };
})();
