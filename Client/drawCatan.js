function drawBoard(highlightSpaces=0, highlightLinks=false, center=null, init=false) {
	var canvas = $("#board")[0].getContext('2d');
	canvas.fillStyle = WATER_COLOR;
	canvas.fillRect(0, 0, 600, 600);
	updateSettlements(board.tiles);
	updateRoads(board.links);

	drawHexes(canvas, board.tiles, highlightSpaces, init, center);
	drawLinks(canvas, board.links, highlightLinks, center, highlightSpaces);
	drawPorts(canvas, board.ports);
	drawRobber(canvas, board.robber);
}

function updateRoads(list) {
	roads = [];
	for(var i = 0; i < list.length; i++) {
		var x = [];
		var y = [];
		x.push(xOffsetGlobal + list[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH / 4);
		x.push(xOffsetGlobal + list[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH * 3 / 4);
		x.push(xOffsetGlobal + list[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH);
		x.push(xOffsetGlobal + list[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH * 3 / 4);
		x.push(xOffsetGlobal + list[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH / 4);
		x.push(xOffsetGlobal + list[i].column * HEX_WIDTH * 3 / 4);
		
		y.push(yOffsetGlobal + list[i].diagonal * HEX_HEIGHT - list[i].column * HEX_HEIGHT / 2);
		y.push(yOffsetGlobal + list[i].diagonal * HEX_HEIGHT - list[i].column * HEX_HEIGHT / 2);
		y.push(yOffsetGlobal + list[i].diagonal * HEX_HEIGHT - list[i].column * HEX_HEIGHT / 2 + HEX_HEIGHT / 2);
		y.push(yOffsetGlobal + list[i].diagonal * HEX_HEIGHT - list[i].column * HEX_HEIGHT / 2 + HEX_HEIGHT);
		y.push(yOffsetGlobal + list[i].diagonal * HEX_HEIGHT - list[i].column * HEX_HEIGHT / 2 + HEX_HEIGHT);
		y.push(yOffsetGlobal + list[i].diagonal * HEX_HEIGHT - list[i].column * HEX_HEIGHT / 2 + HEX_HEIGHT / 2);
		
		var idx1 = list[i].position - 1;
		if(idx1 < 0) idx1 += 6;
		var idx2 = list[i].position;
		
		if(list[i].road !== -1) {
			roads.push({team: list[i].road, start: {x: x[idx1], y: y[idx1]}, stop: {x: x[idx2], y: y[idx2]}});
		}
	}
	
}

function updateSettlements(tiles) {
	settlements = [];
	for(var diag = 0; diag < tiles.length; diag++) {
		for(var col = 0; col < tiles[diag].length; col++) {
			if(tiles[diag][col] != null) {
				var spaces = tiles[diag][col].spaces;
				for(var i = 0; i < spaces.length; i++) {
					var space = spaces[i];
					if(space.building != null) {
						var team = space.building.player.team;
						var x = xOffsetGlobal + col * HEX_WIDTH * 3 / 4 + HEX_WIDTH / 4;
						var y = yOffsetGlobal + diag * HEX_HEIGHT - col * HEX_HEIGHT / 2;
						
						if(i >= 1) {
							x += HEX_WIDTH / 2;
						}
						if(i >= 2) {
							x += HEX_WIDTH / 4;
							y += HEX_HEIGHT / 2;
						}
						if(i >= 3) {
							x -= HEX_WIDTH / 4;
							y += HEX_HEIGHT / 2;
						}
						if(i >= 4) {
							x -= HEX_WIDTH / 2;
						}
						if(i >= 5) {
							x -= HEX_WIDTH / 4;
							y -= HEX_HEIGHT / 2;
						}
						
						settlements.push({diagonal: diag, column: col, x: x, y: y, team: team});
					}
				}
			}
		}
	}
}

function drawHexes(canvas, tiles, highlightSpaces=0, init=false, center) {
	hexes = [];
	for(var i = 0; i < tiles.length; i++) {
		var row = [];
		for(var j = 0; j < tiles[i].length; j++) {
			if(tiles[i][j] != null) {
				if(tiles[i][j].resource === "Wheat") {
					canvas.fillStyle = WHEAT_COLOR;
				} else if(tiles[i][j].resource === "Sheep") {
					canvas.fillStyle = SHEEP_COLOR;
				} else if(tiles[i][j].resource === "Stone") {
					canvas.fillStyle = STONE_COLOR;
				} else if(tiles[i][j].resource === "Brick") {
					canvas.fillStyle = BRICK_COLOR;
				} else if(tiles[i][j].resource === "Wood") {
					canvas.fillStyle = WOOD_COLOR;
				} else if(tiles[i][j].resource === "None") {
					canvas.fillStyle = DESERT_COLOR;
				} 
				var xOffset = j * HEX_WIDTH * 3 / 4;
				var yOffset = i * HEX_HEIGHT - j * HEX_HEIGHT / 2;
				var points = [];
				
				canvas.beginPath();
				var x = xOffset + xOffsetGlobal + HEX_WIDTH / 4;
				var y = yOffset + yOffsetGlobal;
				points.push([x, y]);
				canvas.moveTo(x, y);
				x += HEX_WIDTH / 2;
				points.push([x, y]);
				canvas.lineTo(x, y);
				x += HEX_WIDTH / 4;
				y += HEX_HEIGHT / 2;
				points.push([x, y]);
				canvas.lineTo(x, y);
				x -= HEX_WIDTH / 4;
				y += HEX_HEIGHT / 2;
				points.push([x, y]);
				canvas.lineTo(x, y);
				x -= HEX_WIDTH / 2;
				points.push([x, y]);
				canvas.lineTo(x, y);
				x -= HEX_WIDTH / 4;
				y -= HEX_HEIGHT / 2;
				points.push([x, y]);
				canvas.lineTo(x, y);
				
				canvas.closePath();
				canvas.fill();
				
				canvas.lineWidth = 1;
				canvas.strokeStyle = "#000";
				canvas.beginPath();
				x = xOffset + xOffsetGlobal + HEX_WIDTH / 4;
				y = yOffset + yOffsetGlobal;
				canvas.moveTo(x, y);
				x += HEX_WIDTH / 2;
				canvas.lineTo(x, y);
				x += HEX_WIDTH / 4;
				y += HEX_HEIGHT / 2;
				canvas.lineTo(x, y);
				x -= HEX_WIDTH / 4;
				y += HEX_HEIGHT / 2;
				canvas.lineTo(x, y);
				x -= HEX_WIDTH / 2;
				canvas.lineTo(x, y);
				x -= HEX_WIDTH / 4;
				y -= HEX_HEIGHT / 2;
				canvas.lineTo(x, y);
				
				canvas.closePath();
				canvas.stroke();
				var hex = {points: points, first: [xOffset + xOffsetGlobal, yOffset + yOffsetGlobal], last: [xOffset + HEX_WIDTH + xOffsetGlobal, yOffset + HEX_HEIGHT + yOffsetGlobal], diagonal: i, column: j};
				
				row.push(hex);
				
				drawSpaces(canvas, tiles[i][j], j, i, highlightSpaces, init, center);
				
				if(tiles[i][j].resource !== "None") {
					var horizontalOffset = xOffset + xOffsetGlobal + HEX_WIDTH / 2;
					var verticalOffset = yOffset + yOffsetGlobal + HEX_HEIGHT / 2;
					
					canvas.fillStyle = NUMBER_BG_COLOR;
					canvas.beginPath();
					canvas.arc(horizontalOffset, verticalOffset, 20, 0, 2 * Math.PI);
					canvas.fill();
					
					
					canvas.font = "20px Arial";
					if(tiles[i][j].roll === 6 || tiles[i][j].roll === 8) {
						canvas.fillStyle = RED_NUMBER_COLOR;
					} else {
						canvas.fillStyle = NORMAL_NUMBER_COLOR;
					}
					var textWidth = canvas.measureText(tiles[i][j].roll).width;
					canvas.fillText(tiles[i][j].roll, horizontalOffset - textWidth / 2, verticalOffset + 7);
				}
			} else {
				row.push(null);
			}
		}
		hexes.push(row);
	}
}

function drawSpaces(canvas, tile, column, diagonal, highlightSpaces, init, center) {
	var spaces = tile.spaces;
	for(var i = 0; i < spaces.length; i++) {
		var space = spaces[i];
		var x = xOffsetGlobal + column * HEX_WIDTH * 3 / 4 + HEX_WIDTH / 4;
		var y = yOffsetGlobal + diagonal * HEX_HEIGHT - column * HEX_HEIGHT / 2;
		
		if(i >= 1) {
			x += HEX_WIDTH / 2;
		}
		if(i >= 2) {
			x += HEX_WIDTH / 4;
			y += HEX_HEIGHT / 2;
		}
		if(i >= 3) {
			x -= HEX_WIDTH / 4;
			y += HEX_HEIGHT / 2;
		}
		if(i >= 4) {
			x -= HEX_WIDTH / 2;
		}
		if(i >= 5) {
			x -= HEX_WIDTH / 4;
			y -= HEX_HEIGHT / 2;
		}
		
		
		if(highlightSpaces === 2 && space.building != null && isValidCity({x: x, y: y})) {
			canvas.fillStyle = "#000";
			canvas.beginPath();
			canvas.arc(x, y, 15, 0, 2 * Math.PI);
			canvas.closePath();
			canvas.fill();
		} else if(highlightSpaces === 3 && center != null && isValidSteal({x: x, y: y}, center)) {
			canvas.fillStyle = "#000";
			canvas.beginPath();
			canvas.arc(x, y, 15, 0, 2 * Math.PI);
			canvas.closePath();
			canvas.fill();
		} else if(space.building != null || (highlightSpaces === 1 && isValidSettlement({x: x, y: y}, true))) {
			if(space.building == null) {
				canvas.fillStyle = "#000";
				canvas.beginPath();
				canvas.arc(x, y, 15, 0, 2 * Math.PI);
			} else {
				var team = space.building.player.team;
				canvas.fillStyle = teams[team].hexColor;
				canvas.beginPath();
				if(space.building.type === "city") {
					canvas.arc(x, y, 10, 0, 2 * Math.PI);
				} else {
					canvas.moveTo(x - 5, y - 5);
					canvas.lineTo(x + 5, y - 5);
					canvas.lineTo(x + 5, y + 5);
					canvas.lineTo(x - 5, y + 5);
					canvas.lineTo(x - 5, y - 5);
				}
			}
			
			canvas.closePath();
			canvas.fill();
			canvas.beginPath();
			if(space.building != null) {
				if(space.building.type === "city") {
					canvas.arc(x, y, 10, 0, 2 * Math.PI);
				} else {
					canvas.moveTo(x - 5, y - 5);
					canvas.lineTo(x + 5, y - 5);
					canvas.lineTo(x + 5, y + 5);
					canvas.lineTo(x - 5, y + 5);
					canvas.lineTo(x - 5, y - 5);
				}
			}
			canvas.closePath();
			canvas.stroke();
		}
	}
}

function drawLinks(canvas, links, highlight=false, center=null, highlightSpaces) {
	var list = links;
	if(center != null && highlightSpaces !== 3) {
		list = [];
		var point = center.hex.points[center.space];
		for(var i = 0; i < links.length; i++) {
			if(links[i].road !== -1 )
				list.push(links[i]);
			else {
				var x = [];
				var y = [];
				x.push(xOffsetGlobal + links[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH / 4);
				x.push(xOffsetGlobal + links[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH * 3 / 4);
				x.push(xOffsetGlobal + links[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH);
				x.push(xOffsetGlobal + links[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH * 3 / 4);
				x.push(xOffsetGlobal + links[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH / 4);
				x.push(xOffsetGlobal + links[i].column * HEX_WIDTH * 3 / 4);
				
				y.push(yOffsetGlobal + links[i].diagonal * HEX_HEIGHT - links[i].column * HEX_HEIGHT / 2);
				y.push(yOffsetGlobal + links[i].diagonal * HEX_HEIGHT - links[i].column * HEX_HEIGHT / 2);
				y.push(yOffsetGlobal + links[i].diagonal * HEX_HEIGHT - links[i].column * HEX_HEIGHT / 2 + HEX_HEIGHT / 2);
				y.push(yOffsetGlobal + links[i].diagonal * HEX_HEIGHT - links[i].column * HEX_HEIGHT / 2 + HEX_HEIGHT);
				y.push(yOffsetGlobal + links[i].diagonal * HEX_HEIGHT - links[i].column * HEX_HEIGHT / 2 + HEX_HEIGHT);
				y.push(yOffsetGlobal + links[i].diagonal * HEX_HEIGHT - links[i].column * HEX_HEIGHT / 2 + HEX_HEIGHT / 2);
				
				var idx1 = links[i].position - 1;
				if(idx1 < 0) idx1 += 6;
				var idx2 = links[i].position;
				
				var d = dist(x[idx1], y[idx1], point[0], point[1]);
				if(d <= 2) {
					list.push(links[i]);
				}
				d = dist(x[idx2], y[idx2], point[0], point[1]);
				if(d <= 2) {
					list.push(links[i]);
				}
			}
		}
	}
	for(var i = 0; i < list.length; i++) {
		var x = [];
		var y = [];
		x.push(xOffsetGlobal + list[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH / 4);
		x.push(xOffsetGlobal + list[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH * 3 / 4);
		x.push(xOffsetGlobal + list[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH);
		x.push(xOffsetGlobal + list[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH * 3 / 4);
		x.push(xOffsetGlobal + list[i].column * HEX_WIDTH * 3 / 4 + HEX_WIDTH / 4);
		x.push(xOffsetGlobal + list[i].column * HEX_WIDTH * 3 / 4);
		
		y.push(yOffsetGlobal + list[i].diagonal * HEX_HEIGHT - list[i].column * HEX_HEIGHT / 2);
		y.push(yOffsetGlobal + list[i].diagonal * HEX_HEIGHT - list[i].column * HEX_HEIGHT / 2);
		y.push(yOffsetGlobal + list[i].diagonal * HEX_HEIGHT - list[i].column * HEX_HEIGHT / 2 + HEX_HEIGHT / 2);
		y.push(yOffsetGlobal + list[i].diagonal * HEX_HEIGHT - list[i].column * HEX_HEIGHT / 2 + HEX_HEIGHT);
		y.push(yOffsetGlobal + list[i].diagonal * HEX_HEIGHT - list[i].column * HEX_HEIGHT / 2 + HEX_HEIGHT);
		y.push(yOffsetGlobal + list[i].diagonal * HEX_HEIGHT - list[i].column * HEX_HEIGHT / 2 + HEX_HEIGHT / 2);
		
		var idx1 = list[i].position - 1;
		if(idx1 < 0) idx1 += 6;
		var idx2 = list[i].position;
		canvas.strokeStyle = "#000";
		if(list[i].road !== -1) {
			canvas.strokeStyle = teams[list[i].road].hexColor;
		}
		if(list[i].road !== -1 || (highlight && (center != null || isValidRoad({x: x[idx1], y: y[idx1]}, {x: x[idx2], y: y[idx2]})))) {
			canvas.lineWidth = 5;
			canvas.beginPath();
			canvas.moveTo(x[idx1], y[idx1]);
			canvas.lineTo(x[idx2], y[idx2]);
			canvas.closePath();
			canvas.stroke();
		}
	}
}

function drawPorts(canvas, ports) {
	for(var i = 0; i < ports.length; i++) {
		var port = ports[i];
		
		if(port != null && port.link != null) {
			var lin = port.link;
			var x = [];
			var y = [];
			x.push(xOffsetGlobal + lin.column * HEX_WIDTH * 3 / 4 + HEX_WIDTH / 4);
			x.push(xOffsetGlobal + lin.column * HEX_WIDTH * 3 / 4 + HEX_WIDTH * 3 / 4);
			x.push(xOffsetGlobal + lin.column * HEX_WIDTH * 3 / 4 + HEX_WIDTH);
			x.push(xOffsetGlobal + lin.column * HEX_WIDTH * 3 / 4 + HEX_WIDTH * 3 / 4);
			x.push(xOffsetGlobal + lin.column * HEX_WIDTH * 3 / 4 + HEX_WIDTH / 4);
			x.push(xOffsetGlobal + lin.column * HEX_WIDTH * 3 / 4);
			
			y.push(yOffsetGlobal + lin.diagonal * HEX_HEIGHT - lin.column * HEX_HEIGHT / 2);
			y.push(yOffsetGlobal + lin.diagonal * HEX_HEIGHT - lin.column * HEX_HEIGHT / 2);
			y.push(yOffsetGlobal + lin.diagonal * HEX_HEIGHT - lin.column * HEX_HEIGHT / 2 + HEX_HEIGHT / 2);
			y.push(yOffsetGlobal + lin.diagonal * HEX_HEIGHT - lin.column * HEX_HEIGHT / 2 + HEX_HEIGHT);
			y.push(yOffsetGlobal + lin.diagonal * HEX_HEIGHT - lin.column * HEX_HEIGHT / 2 + HEX_HEIGHT);
			y.push(yOffsetGlobal + lin.diagonal * HEX_HEIGHT - lin.column * HEX_HEIGHT / 2 + HEX_HEIGHT / 2);
			
			var idx1 = lin.position - 1;
			if(idx1 < 0) idx1 += 6;
			var idx2 = lin.position;
			
			var mX = (x[idx1] + x[idx2]) / 2;
			var mY = (y[idx1] + y[idx2]) / 2;
			var slope = -(x[idx1] - x[idx2]) / (y[idx1] - y[idx2]);
			var dx = HEX_SIDE / Math.sqrt(1 + slope * slope);
			if(lin.position === 5 || lin.position === 0) {
				dx *= -1;
			}
			var dy = slope * dx;
			
			var portX = mX + dx;
			var portY = mY + dy;
			if(slope === Infinity) {
				portX = mX;
				portY = mY - HEX_SIDE;
			} else if(slope === -1 * Infinity) {
				portX = mX;
				portY = mY + HEX_SIDE;
			}
			
			canvas.lineWidth = 1;
			canvas.strokeStyle = "#000";
			
			canvas.beginPath();
			canvas.moveTo(x[idx1], y[idx1]);
			canvas.lineTo(portX, portY);
			canvas.moveTo(x[idx1], y[idx1]);
			canvas.closePath();
			canvas.stroke();
			
			canvas.beginPath();
			canvas.moveTo(x[idx2], y[idx2]);
			canvas.lineTo(portX, portY);
			canvas.moveTo(x[idx2], y[idx2]);
			canvas.closePath();
			canvas.stroke();
			
			if(port.type === "Wheat") {
				canvas.fillStyle = WHEAT_COLOR;
			} else if(port.type === "Sheep") {
				canvas.fillStyle = SHEEP_COLOR;
			} else if(port.type === "Stone") {
				canvas.fillStyle = STONE_COLOR;
			} else if(port.type === "Brick") {
				canvas.fillStyle = BRICK_COLOR;
			} else if(port.type === "Wood") {
				canvas.fillStyle = WOOD_COLOR;
			} else if(port.type === "All") {
				canvas.fillStyle = ALL_PORT_COLOR;
			}
			canvas.beginPath();
			canvas.moveTo(portX, portY);
			canvas.arc(portX, portY, 10, 0, Math.PI * 2);
			canvas.closePath();
			canvas.fill();
			canvas.moveTo(portX, portY);
			canvas.beginPath();
			canvas.arc(portX, portY, 10, 0, Math.PI * 2);
			canvas.closePath();
			canvas.stroke();
		}
	}
}

function drawRobber(canvas, robber) {
	var xOffset = robber.column * HEX_WIDTH * 3 / 4;
	var yOffset = robber.diagonal * HEX_HEIGHT - robber.column * HEX_HEIGHT / 2;
	var x = xOffset + xOffsetGlobal + HEX_WIDTH / 2;
	var y = yOffset + yOffsetGlobal + HEX_HEIGHT / 2;
	
	canvas.fillStyle = ROBBER_COLOR;
	canvas.beginPath();
	canvas.arc(x, y, 20, 0, Math.PI * 2);
	canvas.closePath();
	canvas.fill();
}
