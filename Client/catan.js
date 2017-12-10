var socket;
var state = 0;
var team = 0;

var board = {};
var playerData = [];
var hexes = [];
var devCards = [];

var xOffsetGlobal = 0;
var yOffsetGlobal = 0;

var lastClick = {x: -1, y: -1};
var settlements = [];
var roads = [];

var gameList = [];
var showTable = true;
var updateList = null;
var onInput = null;
var inputArg = null;

$(document).ready(function () {
	socket = new WebSocket(address);
	
	socket.onmessage = function (event) {
		var msg = event.data;
		var primaryProtocol = msg.charAt(0);
		var secondaryProtocol = msg.charAt(1);
		var protocol = parseInt(primaryProtocol);
		if (isNumber(secondaryProtocol)) {
			protocol = protocol * 10 + parseInt(secondaryProtocol);
			msg = msg.substring(2);
		} else {
			msg = msg.substring(1);
		}
		if (protocol === GAMESTATE_BOARD) {
			board = JSON.parse(msg);
			drawBoard();
		} else if (protocol === GAMESTATE_PLAYERS) {
			updatePlayers(JSON.parse(msg));
		} else if (protocol === INFORMATION_CONSOLE) {
			showMessage(msg);
		} else if (protocol === GAMESTATE_HAND) {
			updateHand(JSON.parse(msg));
		} else if (protocol === ROBBER) {
			placeRobber(JSON.parse(msg));
		} else if (protocol === TURN) {
			doTurn();
		} else if (protocol === GAMESTATE_DEV_CARDS) {
			devCards = JSON.parse(msg);
			updateDevCards(devCards);
		} else if (protocol === PLACE) {
			placeInitial();
		} else if (protocol === INFORMATION_TEAM) {
			team = parseInt(msg);
		} else if (protocol === RESPONSE * 10 + TRADE) {
			selectOffer(JSON.parse(msg));
		} else if (protocol === TRADE) {
			acceptOffer(JSON.parse(msg));
		} else if (protocol === ROBBER_DISCARD) {
			discard(parseInt(msg));
		} else if (protocol === GAMESTATE_LOBBY) {
			updateLobby(JSON.parse(msg));
		} else if (protocol === GAMESTATE_GAME_LIST && showTable) {
			showGameList(JSON.parse(msg));
		} else {
			console.log(event.data);
		}
	};
	
	socket.onopen = function (event) {
		socket.send(INFORMATION_USERNAME + "" + username);
	};
	
	socket.onclose = function (ev) {
		showMessage("You have been disconnected from the game.");
	};
	
	var b = $("#board");
	xOffsetGlobal = b[0].width / 2 - 4 * HEX_WIDTH / 2;
	yOffsetGlobal = b[0].height / 2 - 5 * HEX_HEIGHT / 2;
	
	buildGameList([]);
	updateList = setInterval(updateGameList, 1000);
});

function updateGameList() {
	socket.send(GAMESTATE_GAME_LIST);
}

function showGameList(data) {
	var table = $("#game-list");
	for (var i = 0; i < data.length; i++) {
		var game = data[i];
		if (gameList.length === 0 || game.id > gameList[gameList.length - 1].id) {
			var html = "<tr>";
			html += "<td>" + game.name + "</td>";
			html += "<td>";
			for (var j = 0; j < game.participants.length; j++) {
				if (j !== 0) html += ", ";
				html += game.participants[j];
			}
			html += "</td>";
			html += "<td>" + game.winner + "</td>";
			if (game.winner === "In progress") {
				html += "<td><input type='submit' value='Join' name='" + game.name + "' class='btn btn-primary btn-sm' /></td>";
			} else {
				html += "<td></td>";
			}
			html += "</tr>";
			table.prepend(html);
			gameList.push(game);
		}
	}
	$("#form").find("input[type=submit]").click(function () {
		$("input[type=submit]", $(this).parents("form")).removeAttr("clicked");
		$(this).attr("clicked", "true");
	});
}

function buildGameList(data) {
	var html = "<table class='table table-sm'>";
	html += "<thead>";
	html += "<tr><th>Game name</th><th>Participants</th><th>Winner</th><th>Actions</th></tr>";
	html += "</thead>";
	html += "<tbody id='game-list'>";
	for (var i = 0; i < data.length; i++) {
		var game = data[i];
		html += "<tr>";
		html += "<td>" + game.name + "</td>";
		html += "<td>";
		for (var j = 0; j < game.participants.length; j++) {
			if (j !== 0) html += ", ";
			html += game.participants[j];
		}
		html += "</td>";
		html += "<td>" + game.winner + "</td>";
		if (game.winner === "In progress") {
			html += "<td><input type='submit' value='Join' name='" + game.name + "' class='btn btn-primary btn-sm' /></td>";
		} else {
			html += "<td></td>";
		}
		html += "</tr>";
	}
	html += "<tr>";
	html += "<td colspan='3'><input type='text' name='name' placeholder='Game name' class='form-control' /></td>"
	html += "<td><input type='submit' value='New game' class='btn btn-primary' /></td>";
	html += "</tr>";
	html += "</tbody></table>";
	var onsubmit = function (e) {
		e.preventDefault();
		var clicked = $("input[type=submit][clicked=true]");
		var val = clicked.attr('value');
		var name = clicked.attr('name');
		
		var data = $("#form").find(":input").serializeArray();
		if (val === "Join") {
			socket.send(GAME_JOIN + "" + name);
			showTable = false;
			clearInterval(updateList);
		} else if (val === "New game") {
			newGame(data[0].value);
			showTable = false;
			clearInterval(updateList);
		}
	};
	updateForm(html, onsubmit);
}

function newGame(name) {
	var form = $("#form");
	var html = "<table class='table table-sm'>";
	html += "<tbody>"
	html += "<tr><td><input type='submit' class='btn btn-sm btn-secondary' name='action' value='-' /></td>";
	html += "<td><span id='player-amt'>3</span></td>";
	html += "<td><input type='submit' class='btn btn-sm btn-secondary' name='action' value='+' /></td></tr>";
	html += "<tr><td><input type='submit' class='btn btn-primary' value='Submit' /></td></tr>";
	html += "</tbody>";
	html += "</table>";
	
	var onsubmit = function (ev, n=name) {
		ev.preventDefault();
		var clicked = $("input[type=submit][clicked=true]");
		var val = clicked.attr('value');
		var counter = $("#player-amt");
		
		if (val === "Submit") {
			var amt = counter.html();
			socket.send(GAME_START + "" + amt + "" + n);
			updateForm("", null);
		} else {
			var change = 0;
			if (val === '+') change = 1;
			else change = -1;
			var count = parseInt(counter.html());
			count += change;
			if (count >= 3 && count <= 4)
				counter.html("" + count);
		}
	};
	updateForm(html, onsubmit);
}

function updateLobby(lobby) {
	if ($("#lobby-table").length === 0) {
		buildLobby(lobby);
		return;
	}
	for (var i = 0; i < lobby.length; i++) {
		var player = lobby[i];
		$("#" + i + "-name").html(player.name);
		if (player.ready) {
			$("#" + i + "-ready").html("check");
		} else {
			$("#" + i + "-ready").html("clear");
		}
	}
}

function buildLobby(lobby) {
	var html = "<table id='lobby-table' class='table table-sm'>";
	html += "<thead>";
	html += "   <tr>";
	html += "       <th>Player</th><th>Ready</th>"
	html += "   </tr>";
	html += "</thead>";
	html += "<tbody>";
	for (var i = 0; i < lobby.length; i++) {
		var player = lobby[i];
		html += "<tr>";
		html += "   <td id='" + i + "-name'>" + player.name + "</td>";
		if (player.ready) {
			html += "<td><i id='" + i + "-ready' class='material-icons'>check</i></td>";
		} else {
			html += "<td><i id='" + i + "-ready' class='material-icons'>clear</i></td>";
		}
		html += "</tr>";
	}
	html += "</tbody>";
	html += "</table>";
	html += "<input type='submit' name='action' class='btn btn-primary' value='Toggle Ready' />";
	
	var onsubmit = function (ev) {
		ev.preventDefault();
		socket.send("" + READY);
	};
	
	updateForm(html, onsubmit);
}

function isNumber(c) {
	return c >= '0' && c <= '9';
}

function discard(amount) {
	showMessage("You must choose " + amount + " cards to discard.");
	var html = "<table class='table table-sm'>";
	html += "<thead>";
	html += "<tr><th>Action</th><th colspan='5'>Resource</th></tr>";
	html += "</thead><tbody>";
	html += "<tr>";
	html += "	<td>Discard</td>";
	html += "	<td><img src='" + imageDirectory + "wood.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "sheep.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "wheat.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "stone.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "brick.png' class='resource-icon' /></td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td></td>";
	html += "	<td><input type='submit' resource='wood' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='sheep' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='wheat' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='stone' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='brick' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td></td>";
	html += "	<td id='wood-give-amt' class='text-center' >0</td>";
	html += "	<td id='sheep-give-amt' class='text-center'>0</td>";
	html += "	<td id='wheat-give-amt' class='text-center'>0</td>";
	html += "	<td id='stone-give-amt' class='text-center'>0</td>";
	html += "	<td id='brick-give-amt' class='text-center'>0</td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td></td>";
	html += "	<td><input type='submit' resource='wood' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='sheep' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='wheat' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='stone' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='brick' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "</tr>";
	html += "<tr><td><input type='submit' class='btn btn-sm btn-primary' name='action' value='Submit' /></td></tr>";
	html += "</tbody></table>";
	
	var onsubmit = function (e) {
		e.preventDefault();
		var clicked = $("input[type=submit][clicked=true]");
		var val = clicked.attr('value');
		var res = clicked.attr('resource');
		var counter;
		if (clicked.hasClass("give")) {
			counter = $("#" + res + "-give-amt");
		} else if (clicked.hasClass("take")) {
			counter = $("#" + res + "-take-amt");
		}
		if (counter != null) {
			var change = 0;
			if (val === '+') change = 1;
			else change = -1;
			var count = parseInt(counter.html());
			count += change;
			if (count >= 0)
				counter.html("" + count);
		} else if (val === 'Submit') {
			var params = "";
			params += $("#wood-give-amt").html() + " ";
			params += $("#sheep-give-amt").html() + " ";
			params += $("#wheat-give-amt").html() + " ";
			params += $("#stone-give-amt").html() + " ";
			params += $("#brick-give-amt").html();
			socket.send(RESPONSE + "" + ROBBER_DISCARD + "" + params);
			updateForm("", null);
		}
	};
	
	updateForm(html, onsubmit);
}

function yearOfPlenty() {
	showMessage("Choose 2 cards to take from the bank.");
	var html = "<table class='table table-sm'>";
	html += "<thead>";
	html += "<tr><th>Action</th><th colspan='5'>Resource</th></tr>";
	html += "</thead><tbody>";
	html += "<tr>";
	html += "	<td>Discard</td>";
	html += "	<td><img src='" + imageDirectory + "wood.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "sheep.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "wheat.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "stone.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "brick.png' class='resource-icon' /></td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td></td>";
	html += "	<td><input type='submit' resource='wood' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='sheep' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='wheat' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='stone' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='brick' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td></td>";
	html += "	<td id='wood-give-amt' class='text-center' >0</td>";
	html += "	<td id='sheep-give-amt' class='text-center'>0</td>";
	html += "	<td id='wheat-give-amt' class='text-center'>0</td>";
	html += "	<td id='stone-give-amt' class='text-center'>0</td>";
	html += "	<td id='brick-give-amt' class='text-center'>0</td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td></td>";
	html += "	<td><input type='submit' resource='wood' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='sheep' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='wheat' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='stone' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='brick' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "</tr>";
	html += "<tr><td><input type='submit' class='btn btn-sm btn-primary' name='action' value='Submit' /></td></tr>";
	html += "</tbody></table>";
	
	var onsubmit = function (e) {
		e.preventDefault();
		var clicked = $("input[type=submit][clicked=true]");
		var val = clicked.attr('value');
		var res = clicked.attr('resource');
		var counter;
		if (clicked.hasClass("give")) {
			counter = $("#" + res + "-give-amt");
		} else if (clicked.hasClass("take")) {
			counter = $("#" + res + "-take-amt");
		}
		if (counter != null) {
			var change = 0;
			if (val === '+') change = 1;
			else change = -1;
			var count = parseInt(counter.html());
			count += change;
			if (count >= 0)
				counter.html("" + count);
		} else if (val === 'Submit') {
			var params = "";
			params += $("#wood-give-amt").html() + " ";
			params += $("#sheep-give-amt").html() + " ";
			params += $("#wheat-give-amt").html() + " ";
			params += $("#stone-give-amt").html() + " ";
			params += $("#brick-give-amt").html();
			socket.send(RESPONSE + "" + TURN_YOP + "" + params);
			updateForm("", null);
		}
	};
	
	updateForm(html, onsubmit);
}

function doTurn() {
	var devCards = countDevCards();
	var html = "";
	html += "<table class='table'><thead><tr><th>Action</th><th>Requirements</th></tr></thead><tbody>";
	html += "<tr>";
	html += "	<td><input type='submit' class='btn btn-primary' name='action' value='Build Settlement' /></td>";
	html += "	<td><img src='" + imageDirectory + "brick.png' class='resource-icon'/><img src='" + imageDirectory + "wheat.png' class='resource-icon'/><img src='" + imageDirectory + "wood.png' class='resource-icon'/><img src='" + imageDirectory + "sheep.png' class='resource-icon'/></td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td><input type='submit' class='btn btn-primary' name='action' value='Build City' /></td>";
	html += "	<td><img src='" + imageDirectory + "stone.png' class='resource-icon'/><img src='" + imageDirectory + "stone.png' class='resource-icon'/><img src='" + imageDirectory + "stone.png' class='resource-icon'/><img src='" + imageDirectory + "wheat.png' class='resource-icon'/><img src='" + imageDirectory + "wheat.png' class='resource-icon'/></td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td><input type='submit' class='btn btn-primary' name='action' value='Build Road' /></td>";
	html += "	<td><img src='" + imageDirectory + "brick.png' class='resource-icon'/><img src='" + imageDirectory + "wood.png' class='resource-icon'/></td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td><input type='submit' class='btn btn-primary' name='action' value='Buy Development Card' /></td>";
	html += "	<td><img src='" + imageDirectory + "stone.png' class='resource-icon'/><img src='" + imageDirectory + "wheat.png' class='resource-icon'/><img src='" + imageDirectory + "sheep.png' class='resource-icon'/></td>";
	html += "</tr>";
	html += "<tr><td colspan='2'>";
	if (devCards.knight.playable > 0)
		html += "	<input type='submit' class='btn btn-primary' name='action' value='Play Knight' /> ";
	if (devCards.monopoly.playable > 0)
		html += "	<input type='submit' class='btn btn-primary' name='action' value='Play Monopoly' /> ";
	if (devCards.yop.playable > 0)
		html += "	<input type='submit' class='btn btn-primary' name='action' value='Play Year of Plenty' /> ";
	if (devCards.rb.playable > 0)
		html += "	<input type='submit' class='btn btn-primary' name='action' value='Play Road Building' /> ";
	html += "</td></tr>";
	html += "<tr>";
	html += "<td colspan='2'><input type='submit' class='btn btn-primary' name='action' value='Trade with Players' /> ";
	html += "<input type='submit' class='btn btn-primary' name='action' value='Trade with Bank' /></td>";
	html += "</tr>";
	html += "<tr><td colspan='2'>";
	html += "	<input type='submit' class='btn btn-primary' name='action' value='End Turn' /> ";
	html += "</td></tr>";
	html += "</tbody></table>";
	
	var onsubmit = function (e) {
		e.preventDefault();
		var val = $("input[type=submit][clicked=true]").val();
		if (val === "Build Settlement") {
			buildSettlement();
		} else if (val === "Build City") {
			buildCity();
		} else if (val === "Build Road") {
			buildRoad();
		} else if (val === "Buy Development Card") {
			socket.send(RESPONSE + "" + TURN_DEV_CARD);
		} else if (val === "Play Knight") {
			socket.send(RESPONSE + "" + TURN_KNIGHT);
		} else if (val === "Play Monopoly") {
			doMonopoly();
		} else if (val === "Play Year of Plenty") {
			yearOfPlenty();
		} else if (val === "Play Road Building") {
			roadBuliding();
		} else if (val === "Trade with Players") {
			playerTrade();
		} else if (val === "Trade with Bank") {
			bankTrade();
		} else if (val === "End Turn") {
			socket.send(RESPONSE + "");
			updateForm("", null);
		}
	};
	
	updateForm(html, onsubmit);
}

function roadBuliding() {
	showMessage("You may now place a road. Click where you would like to place your road.");
	drawBoard(0, true);
	onInput = function (x, y) {
		var pos = calculateLinkClicked(x, y);
		if (pos != null) {
			setRoad(pos.hex.diagonal, pos.hex.column, pos.link);
			showMessage("You may now place a road. Click where you would like to place your road.");
			drawBoard(0, true);
			var a = pos.hex.diagonal + " " + pos.hex.column + " " + pos.link;
			onInput = function (x, y, r=a) {
				var pos = calculateLinkClicked(x, y);
				if (pos != null) {
					socket.send(RESPONSE + "" + TURN_RB + r + " " + pos.hex.diagonal + " " + pos.hex.column + " " + pos.link);
					onInput = null;
				}
			};
		}
	};
}

function setRoad(diag, col, pos) {
	for (var i = 0; i < board.links.length; i++) {
		var link = board.links[i];
		if (isSameRoad({diag: diag, col: col, pos: pos}, {
				diag: link.diagonal,
				col: link.column,
				pos: link.position
			})) {
			link.road = team;
			break;
		}
	}
}

function isSameRoad(first, second) {
	var x = [];
	var y = [];
	x.push(HEX_WIDTH / 4);
	x.push(HEX_WIDTH * 3 / 4);
	x.push(HEX_WIDTH);
	x.push(HEX_WIDTH * 3 / 4);
	x.push(HEX_WIDTH / 4);
	x.push(0);
	
	y.push(0);
	y.push(0);
	y.push(HEX_HEIGHT / 2);
	y.push(HEX_HEIGHT);
	y.push(HEX_HEIGHT);
	y.push(HEX_HEIGHT / 2);
	
	first.idx1 = first.pos - 1;
	if (first.idx1 < 0) first.idx1 += 6;
	first.idx2 = first.pos;
	
	second.idx1 = second.pos - 1;
	if (second.idx1 < 0) second.idx1 += 6;
	second.idx2 = second.pos;
	
	first.offset = {
		x: xOffsetGlobal + first.col * HEX_WIDTH * 3 / 4,
		y: yOffsetGlobal + first.diag * HEX_HEIGHT - first.col * HEX_HEIGHT / 2
	};
	second.offset = {
		x: xOffsetGlobal + second.col * HEX_WIDTH * 3 / 4,
		y: yOffsetGlobal + second.diag * HEX_HEIGHT - second.col * HEX_HEIGHT / 2
	};
	
	first.start = {x: first.offset.x + x[first.idx1], y: first.offset.y + y[first.idx1]};
	first.stop = {x: first.offset.x + x[first.idx2], y: first.offset.y + y[first.idx2]};
	second.start = {x: second.offset.x + x[second.idx1], y: second.offset.y + y[second.idx1]};
	second.stop = {x: second.offset.x + x[second.idx2], y: second.offset.y + y[second.idx2]};
	
	if (first.start.x === second.start.x && first.start.y === second.start.y && first.stop.x === second.stop.x && first.stop.y === second.stop.y)
		return true;
	else if (first.start.x === second.stop.x && first.start.y === second.stop.y && first.stop.x === second.start.x && first.stop.y === second.start.y)
		return true;
	return false;
}

function buildTradeMenu(data=null) {
	var amounts = [];
	if (data != null) {
		amounts = data.trade;
	} else amounts = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
	var html = "<table class='table table-sm'>";
	html += "<thead>";
	html += "<tr><th>Action</th><th colspan='5'>Resource</th></tr>";
	html += "</thead><tbody>";
	html += "<tr>";
	html += "	<td>Give</td>";
	html += "	<td><img src='" + imageDirectory + "wood.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "sheep.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "wheat.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "stone.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "brick.png' class='resource-icon' /></td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td></td>";
	html += "	<td><input type='submit' resource='wood' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='sheep' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='wheat' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='stone' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='brick' class='btn btn-sm btn-secondary give' name='action' value='+'/></td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td></td>";
	html += "	<td id='wood-give-amt' class='text-center' >" + amounts[5] + "</td>";
	html += "	<td id='sheep-give-amt' class='text-center'>" + amounts[6] + "</td>";
	html += "	<td id='wheat-give-amt' class='text-center'>" + amounts[7] + "</td>";
	html += "	<td id='stone-give-amt' class='text-center'>" + amounts[8] + "</td>";
	html += "	<td id='brick-give-amt' class='text-center'>" + amounts[9] + "</td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td></td>";
	html += "	<td><input type='submit' resource='wood' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='sheep' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='wheat' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='stone' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='brick' class='btn btn-sm btn-secondary give' name='action' value='-'/></td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td>Take</td>";
	html += "	<td><img src='" + imageDirectory + "wood.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "sheep.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "wheat.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "stone.png' class='resource-icon' /></td>";
	html += "	<td><img src='" + imageDirectory + "brick.png' class='resource-icon' /></td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td></td>";
	html += "	<td><input type='submit' resource='wood' class='btn btn-sm btn-secondary take' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='sheep' class='btn btn-sm btn-secondary take' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='wheat' class='btn btn-sm btn-secondary take' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='stone' class='btn btn-sm btn-secondary take' name='action' value='+'/></td>";
	html += "	<td><input type='submit' resource='brick' class='btn btn-sm btn-secondary take' name='action' value='+'/></td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td></td>";
	html += "	<td id='wood-take-amt' class='text-center' >" + amounts[0] + "</td>";
	html += "	<td id='sheep-take-amt' class='text-center'>" + amounts[1] + "</td>";
	html += "	<td id='wheat-take-amt' class='text-center'>" + amounts[2] + "</td>";
	html += "	<td id='stone-take-amt' class='text-center'>" + amounts[3] + "</td>";
	html += "	<td id='brick-take-amt' class='text-center'>" + amounts[4] + "</td>";
	html += "</tr>";
	html += "<tr>";
	html += "	<td></td>";
	html += "	<td><input type='submit' resource='wood' class='btn btn-sm btn-secondary take' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='sheep' class='btn btn-sm btn-secondary take' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='wheat' class='btn btn-sm btn-secondary take' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='stone' class='btn btn-sm btn-secondary take' name='action' value='-'/></td>";
	html += "	<td><input type='submit' resource='brick' class='btn btn-sm btn-secondary take' name='action' value='-'/></td>";
	html += "</tr>";
	html += "<tr><td colspan='2'>";
	if (data == null) {
		html += "<input type='submit' class='btn btn-primary btn-sm' name='action' value='Submit' />";
	} else {
		html += "<input type='submit' class='btn btn-primary btn-sm' name='action' value='Resend' /> ";
		html += "<input type='submit' class='btn btn-primary btn-sm' name='action' value='Reject' />";
	}
	html += "</td></tr>";
	html += "</tbody></table>";
	return html;
}

function acceptOffer(data) {
	var html = buildTradeMenu(data);
	
	var onsubmit = function (e) {
		e.preventDefault();
		var clicked = $("input[type=submit][clicked=true]");
		var val = clicked.attr('value');
		var res = clicked.attr('resource');
		var counter;
		if (clicked.hasClass("give")) {
			counter = $("#" + res + "-give-amt");
		} else if (clicked.hasClass("take")) {
			counter = $("#" + res + "-take-amt");
		}
		if (counter != null) {
			var change = 0;
			if (val === '+') change = 1;
			else change = -1;
			var count = parseInt(counter.html());
			count += change;
			if (count >= 0)
				counter.html("" + count);
		} else if (val === 'Resend') {
			var params = "";
			params += $("#wood-take-amt").html() + " ";
			params += $("#sheep-take-amt").html() + " ";
			params += $("#wheat-take-amt").html() + " ";
			params += $("#stone-take-amt").html() + " ";
			params += $("#brick-take-amt").html() + " ";
			params += $("#wood-give-amt").html() + " ";
			params += $("#sheep-give-amt").html() + " ";
			params += $("#wheat-give-amt").html() + " ";
			params += $("#stone-give-amt").html() + " ";
			params += $("#brick-give-amt").html();
			socket.send(RESPONSE + " " + TRADE + params);
			updateForm("", null);
		} else if (val === 'Reject') {
			socket.send(RESPONSE + " " + TRADE + "reject");
			updateForm("", null);
		}
	};
	
	updateForm(html, onsubmit);
}

function selectOffer(responses) {
	var playerId = 0;
	var html = "";
	html += "<div id='accordion' role='tablist'>";
	for (var i = 0; i < responses.length; i++) {
		if (playerId === team) playerId++;
		html += "<div class='card'>";
		html += "	<div class='card-header' rol='tab' id='response-" + i + "-header'>";
		html += "		<h5 class='mb-0'>";
		html += "			<a data-toggle='collapse' href='#response-" + i + "-collapse' aria-expanded='false' aria-controls='response-" + i + "-collapse'>";
		var allow = false;
		if (responses[i].length === 0) {
			html += "Rejected - ";
			responses[i] = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
		} else {
			allow = true;
			html += "Accepted/Modified - ";
		}
		html += playerData[playerId].name;
		html += "			</a>";
		html += "		</h5>";
		html += "	</div>";
		html += "	<div id='response-" + i + "-collapse' class='collapse' role='tabpanel' aria-labelledby='response-" + i + "-header' data-parent='#accordion'>";
		html += "		<div class='card-body'>";
		html += "			<table class='table table-sm'>";
		html += "				<tbody>";
		html += "					<tr>";
		html += "						<td>You give:</td>";
		html += "						<td><img class='resource-icon' src='" + imageDirectory + "wood.png' /> " + responses[i][0] + "</td>";
		html += "						<td><img class='resource-icon' src='" + imageDirectory + "sheep.png' /> " + responses[i][1] + "</td>";
		html += "						<td><img class='resource-icon' src='" + imageDirectory + "wheat.png' /> " + responses[i][2] + "</td>";
		html += "						<td><img class='resource-icon' src='" + imageDirectory + "stone.png' /> " + responses[i][3] + "</td>";
		html += "						<td><img class='resource-icon' src='" + imageDirectory + "brick.png' /> " + responses[i][4] + "</td>";
		html += "					</tr>";
		html += "					<tr>";
		html += "						<td>You take:</td>";
		html += "						<td><img class='resource-icon' src='" + imageDirectory + "wood.png' /> " + responses[i][5] + "</td>";
		html += "						<td><img class='resource-icon' src='" + imageDirectory + "sheep.png' /> " + responses[i][6] + "</td>";
		html += "						<td><img class='resource-icon' src='" + imageDirectory + "wheat.png' /> " + responses[i][7] + "</td>";
		html += "						<td><img class='resource-icon' src='" + imageDirectory + "stone.png' /> " + responses[i][8] + "</td>";
		html += "						<td><img class='resource-icon' src='" + imageDirectory + "brick.png' /> " + responses[i][9] + "</td>";
		html += "					</tr>";
		if (allow) {
			html += "				<tr>";
			html += "					<td><input type='submit' tradeId='" + i + "' name='action' class='btn btn-primary' value='Accept' /></td>";
			html += "				</tr>";
		}
		html += "				</tbody>";
		html += "			</table><br />";
		html += "		</div>";
		html += "	</div>";
		html += "</div>";
		playerId++;
	}
	html += "</div><br />";
	html += "<input type='submit' name='action' class='btn btn-primary' value='Cancel' />";
	
	var onsubmit = function (e) {
		e.preventDefault();
		var clicked = $("input[type=submit][clicked=true]");
		var val = clicked.attr('value');
		var tradeId = clicked.attr('tradeId');
		if (tradeId == null) tradeId = -1;
		socket.send(RESPONSE + "" + TRADE + "" + tradeId);
		doTurn();
	};
	updateForm(html, onsubmit);
}

function playerTrade() {
	var html = buildTradeMenu();
	
	var onsubmit = function (e) {
		e.preventDefault();
		var clicked = $("input[type=submit][clicked=true]");
		var val = clicked.attr('value');
		var res = clicked.attr('resource');
		var counter;
		if (clicked.hasClass("give")) {
			counter = $("#" + res + "-give-amt");
		} else if (clicked.hasClass("take")) {
			counter = $("#" + res + "-take-amt");
		}
		if (counter != null) {
			var change = 0;
			if (val === '+') change = 1;
			else change = -1;
			var count = parseInt(counter.html());
			count += change;
			if (count >= 0)
				counter.html("" + count);
		} else if (val === 'Submit') {
			var params = "";
			params += $("#wood-give-amt").html() + " ";
			params += $("#sheep-give-amt").html() + " ";
			params += $("#wheat-give-amt").html() + " ";
			params += $("#stone-give-amt").html() + " ";
			params += $("#brick-give-amt").html() + " ";
			params += $("#wood-take-amt").html() + " ";
			params += $("#sheep-take-amt").html() + " ";
			params += $("#wheat-take-amt").html() + " ";
			params += $("#stone-take-amt").html() + " ";
			params += $("#brick-take-amt").html();
			socket.send(RESPONSE + "" + TRADE_PLAYERS + "" + params);
			updateForm("Waiting for all players to respond to offer...", null);
		}
	};
	
	updateForm(html, onsubmit);
}

function bankTrade() {
	var html = buildTradeMenu();
	
	var onsubmit = function (e) {
		e.preventDefault();
		var clicked = $("input[type=submit][clicked=true]");
		var val = clicked.attr('value');
		var res = clicked.attr('resource');
		var counter;
		if (clicked.hasClass("give")) {
			counter = $("#" + res + "-give-amt");
		} else if (clicked.hasClass("take")) {
			counter = $("#" + res + "-take-amt");
		}
		if (counter != null) {
			var change = 0;
			if (val === '+') change = 1;
			else change = -1;
			var count = parseInt(counter.html());
			count += change;
			if (count >= 0)
				counter.html("" + count);
		} else if (val === 'Submit') {
			var params = "";
			params += $("#wood-give-amt").html() + " ";
			params += $("#sheep-give-amt").html() + " ";
			params += $("#wheat-give-amt").html() + " ";
			params += $("#stone-give-amt").html() + " ";
			params += $("#brick-give-amt").html() + " ";
			params += $("#wood-take-amt").html() + " ";
			params += $("#sheep-take-amt").html() + " ";
			params += $("#wheat-take-amt").html() + " ";
			params += $("#stone-take-amt").html() + " ";
			params += $("#brick-take-amt").html();
			socket.send(RESPONSE + "" + TRADE_BANK + "" + params);
			doTurn();
		}
	};
	
	updateForm(html, onsubmit);
}

function placeInitial() {
	showMessage("You may now place a settlement. Click where you would like to place your settlement.");
	inputArg = board;
	drawBoard(1, false, null, true);
	onInput = function (x, y) {
		var pos = calculateSpaceClicked(x, y);
		if (pos != null && isValidSettlement({x: x, y: y}, true)) {
			socket.send(RESPONSE + "" + PLACE_SETTLEMENT + pos.hex.diagonal + " " + pos.hex.column + " " + pos.space);
			showMessage("You may now place a road. Click where you would like to place your road.");
			inputArg = board;
			drawBoard(0, true, pos);
			onInput = function (x, y) {
				var pos = calculateLinkClicked(x, y);
				if (pos != null) {
					drawBoard();
					socket.send(RESPONSE + "" + PLACE_ROAD + pos.hex.diagonal + " " + pos.hex.column + " " + pos.link);
					onInput = null;
				}
			};
		}
	};
}

function buildCity() {
	showMessage("You may now place a city. Click where you would like to place your city.");
	inputArg = board;
	drawBoard(2);
	onInput = function (x, y) {
		var pos = calculateSpaceClicked(x, y);
		if (pos != null) {
			drawBoard();
			socket.send(RESPONSE + "" + TURN_CITY + "" + pos.hex.diagonal + " " + pos.hex.column + " " + pos.space);
			onInput = null;
		}
	}
}

function buildSettlement() {
	showMessage("You may now place a settlement. Click where you would like to place your settlement.");
	inputArg = board;
	drawBoard(1);
	onInput = function (x, y) {
		var pos = calculateSpaceClicked(x, y);
		if (pos != null) {
			drawBoard();
			socket.send(RESPONSE + "" + TURN_SETTLEMENT + "" + pos.hex.diagonal + " " + pos.hex.column + " " + pos.space);
			onInput = null;
		}
	};
}

function buildRoad() {
	showMessage("You may now place a road. Click where you would like to place your road.");
	inputArg = board;
	drawBoard(0, true);
	onInput = function (x, y) {
		var pos = calculateLinkClicked(x, y);
		if (pos != null) {
			drawBoard();
			socket.send(RESPONSE + "" + TURN_ROAD + "" + pos.hex.diagonal + " " + pos.hex.column + " " + pos.link);
			onInput = null;
		}
	};
}

function isValidSteal(position, hex) {
	for (var i = 0; i < settlements.length; i++) {
		var settlement = settlements[i];
		if (settlement.team !== team) {
			var d1 = dist(position.x, position.y, settlement.x, settlement.y);
			var d2 = dist((hex.first[0] + hex.last[0]) / 2, (hex.first[1] + hex.last[1]) / 2, settlement.x, settlement.y);
			if (d1 < 15 && d2 < HEX_WIDTH * 2.0 / 3) {
				return true;
			}
		}
	}
	return false;
}

function isValidSettlement(position, init) {
	var twoSpaces = true;
	var connected = false;
	for (var i = 0; i < settlements.length; i++) {
		var settlement = settlements[i];
		var d = dist(position.x, position.y, settlement.x, settlement.y);
		if (d <= HEX_SIDE + 20) {
			return false;
		}
	}
	if (!init) {
		for (var j = 0; j < roads.length; j++) {
			var road = roads[j];
			if (road.team === team) {
				d = dist(position.x, position.y, road.start.x, road.start.y);
				if (d <= 10) {
					connected = true;
					break;
				}
				d = dist(position.x, position.y, road.stop.x, road.stop.y);
				if (d <= 10) {
					connected = true;
					break;
				}
			}
		}
	}
	return twoSpaces && (connected || init);
}

function isValidCity(position) {
	for (var i = 0; i < settlements.length; i++) {
		var settlement = settlements[i];
		if (settlement.team === team) {
			var d = dist(position.x, position.y, settlement.x, settlement.y);
			if (d <= 20) {
				return true;
			}
		}
	}
	return false;
}

function isValidRoad(start, stop) {
	for (var i = 0; i < roads.length; i++) {
		road = roads[i];
		if (road.team === team) {
			var d = dist(start.x, start.y, road.start.x, road.start.y);
			if (d <= 10) {
				return true;
			}
			d = dist(start.x, start.y, road.stop.x, road.stop.y);
			if (d <= 10) {
				return true;
			}
			d = dist(stop.x, stop.y, road.start.x, road.start.y);
			if (d <= 10) {
				return true;
			}
			d = dist(stop.x, stop.y, road.stop.x, road.stop.y);
			if (d <= 10) {
				return true;
			}
		}
	}
	for (var i = 0; i < settlements.length; i++) {
		var settlement = settlements[i];
		var d = dist(start.x, start.y, settlement.x, settlement.y);
		if (d <= 2 && settlement.team === team)
			return true;
		d = dist(stop.x, stop.y, settlement.x, settlement.y);
		if (d <= 2 && settlement.team === team)
			return true;
	}
	return false;
}

function doMonopoly() {
	var html = "<p>Which resource would you like to monopolize?</p>";
	html += "<input type='image' src='" + imageDirectory + "wood.png' value='wood' class='resource-icon-lg' /> ";
	html += "<input type='image' src='" + imageDirectory + "sheep.png' value='sheep' class='resource-icon-lg' /> ";
	html += "<input type='image' src='" + imageDirectory + "wheat.png' value='wheat' class='resource-icon-lg' /> ";
	html += "<input type='image' src='" + imageDirectory + "stone.png' value='stone' class='resource-icon-lg' /> ";
	html += "<input type='image' src='" + imageDirectory + "brick.png' value='brick' class='resource-icon-lg' /> ";
	html += "<br /><input type='submit' class='btn btn-primary' value='Cancel' />";
	var onsubmit = function (e) {
		e.preventDefault();
		var val = $("input[type=image][clicked=true]").val();
		var cancel = ($("input[type=submit][clicked=true]").val() != null);
		
		if (!cancel) {
			socket.send(RESPONSE + "" + TURN_MONOPOLY + " " + val);
		}
		doTurn();
	};
	
	updateForm(html, onsubmit);
}

function updateForm(html, onsubmit) {
	var form = $("#form");
	if (html != null) {
		form.html(html);
	}
	form.find("input[type=submit]").click(function () {
		$("input[type=submit]", $(this).parents("form")).removeAttr("clicked");
		$(this).attr("clicked", "true");
	});
	form.find("input[type=image]").click(function () {
		$("input[type=image]", $(this).parents("form")).removeAttr("clicked");
		$(this).attr("clicked", "true");
	});
	form.unbind('submit');
	form.on('submit', onsubmit);
}

function placeRobber(board) {
	showMessage("You can now move the robber. The robber may not be placed on the tile that it is already located on.");
	inputArg = board;
	onInput = function (x, y) {
		coordinate = calculateHexClicked(x, y);
		var board = inputArg;
		if (coordinate.diagonal === board.robber.diagonal && coordinate.column === board.robber.column) {
			showMessage("You must place the robber on a tile that is not already located on.");
		} else if (board.tiles[coordinate.diagonal][coordinate.column] == null) {
			showMessage("You must place the robber on a valid tile.");
		} else {
			var center = calculateHexClicked(x, y);
			drawBoard(3, false, center, false);
			onInput = function (x, y, response=RESPONSE + "" + coordinate.diagonal + " " + coordinate.column) {
				if (isValidSteal({x: x, y: y}, center)) {
					for (var i = 0; i < settlements.length; i++) {
						var settlement = settlements[i];
						var d = dist(x, y, settlement.x, settlement.y);
						if (d <= 20) {
							socket.send(response + " " + settlement.team);
							drawBoard();
							onInput = null;
							return;
						}
					}
				}
			};
		}
	};
}

function receiveInput(event) {
	var rect = $("#board")[0].getBoundingClientRect();
	
	lastClick.x = event.x - rect.left;
	lastClick.y = event.y - rect.top;
	
	if (onInput != null) {
		onInput(lastClick.x, lastClick.y);
	}
}

function calculateHexClicked(x, y) {
	for (var i = 0; i < hexes.length; i++) {
		for (var j = 0; j < hexes[i].length; j++) {
			var hex = hexes[i][j];
			if (hex != null) {
				if (hex.first[0] <= x && hex.first[1] <= y && hex.last[0] > x && hex.last[1] > y) {
					if (isPointInPolygon(x, y, hex.points)) {
						return hex;
					}
				}
			}
		}
	}
	return null;
}

function isPointInPolygon(x, y, polygon) {
	var count = 0;
	for (var i = 0; i < polygon.length - 1; i++) {
		count += doSegmentsIntersect([[x, y], [99999, 99999]], [polygon[i], polygon[i + 1]]);
	}
	count += doSegmentsIntersect([[x, y], [99999, 99999]], [polygon[5], polygon[0]]);
	return (count % 2 === 1);
}

function doSegmentsIntersect(a, b) {
	var x1 = a[0][0];
	var y1 = a[0][1];
	var x2 = a[1][0];
	var y2 = a[1][1];
	var x3 = b[0][0];
	var y3 = b[0][1];
	var x4 = b[1][0];
	var y4 = b[1][1];
	
	var m1 = (y1 - y2) / (x1 - x2);
	var m2 = (y3 - y4) / (x3 - x4);
	
	var x = (m1 * x1 - y1 - m2 * x3 + y3) / (m1 - m2);
	if ((x1 <= x && x <= x2) || (x2 <= x && x <= x1)) {
		if ((x3 <= x && x <= x4) || (x4 <= x && x <= x3)) {
			return 1;
		}
	}
	return 0;
}

function dist(x1, y1, x2, y2) {
	return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
}

function calculateSpaceClicked(x, y) {
	var hex = calculateHexClicked(x, y);
	if (hex != null) {
		var diag = hex.diagonal;
		var col = hex.column;
		
		for (var i = 0; i < hex.points.length; i++) {
			var point = hex.points[i];
			var d = dist(x, y, point[0], point[1]);
			if (d < 15) {
				return {hex: hex, space: i};
			}
		}
	}
	return null;
}

function calculateLinkClicked(x, y) {
	var hex = calculateHexClicked(x, y);
	if (hex != null) {
		for (var i = 1; i < hex.points.length; i++) {
			if (linePointDistance(x, y, hex.points[i - 1], hex.points[i]) < 10) {
				return {hex: hex, link: i};
			}
		}
		if (linePointDistance(x, y, hex.points[5], hex.points[0]) < 10) {
			return {hex: hex, link: 0};
		}
	}
	return null;
}

function linePointDistance(x, y, p1, p2) {
	var x1 = p1[0];
	var y1 = p1[1];
	var x2 = p2[0];
	var y2 = p2[1];
	
	return Math.abs(x * (y2 - y1) - y * (x2 - x1) + x2 * y1 - y2 * x1) / dist(x1, y1, x2, y2);
}

function countDevCards() {
	var monopoly = {playable: 0, unplayable: 0};
	var yop = {playable: 0, unplayable: 0};
	var rb = {playable: 0, unplayable: 0};
	var knight = {playable: 0, unplayable: 0};
	var vp = {playable: 0, unplayable: 0};
	
	for (var i = 0; i < devCards.length; i++) {
		var card = devCards[i];
		if (card.name === "Monopoly") {
			if (card.gainedThisTurn) {
				monopoly.unplayable++;
			} else {
				monopoly.playable++;
			}
		} else if (card.name === "Knight") {
			if (card.gainedThisTurn) {
				knight.unplayable++;
			} else {
				knight.playable++;
			}
		} else if (card.name === "Year of Plenty") {
			if (card.gainedThisTurn) {
				yop.unplayable++;
			} else {
				yop.playable++;
			}
		} else if (card.name === "Road Building") {
			if (card.gainedThisTurn) {
				rb.unplayable++;
			} else {
				rb.playable++;
			}
		} else {
			vp.playable++;
		}
	}
	return {monopoly: monopoly, yop: yop, rb: rb, knight: knight, vp: vp};
}

function updateDevCards() {
	var cards = countDevCards();
	
	$("#monopoly-amt").html(getCardString(cards.monopoly));
	$("#yop-amt").html(getCardString(cards.yop));
	$("#rb-amt").html(getCardString(cards.rb));
	$("#knight-amt").html(getCardString(cards.knight));
	$("#vp-amt").html(getCardString(cards.vp));
}

function getCardString(card) {
	var re = "" + card.playable;
	if (card.unplayable !== 0) {
		re += " (" + card.unplayable + ")";
	}
	return re;
}

function updateHand(hand) {
	$("#wood-amt").html(hand.wood);
	$("#sheep-amt").html(hand.sheep);
	$("#wheat-amt").html(hand.wheat);
	$("#stone-amt").html(hand.stone);
	$("#brick-amt").html(hand.brick);
}

function updatePlayers(players) {
	playerData = players;
	for (var i = 0; i < players.length; i++) {
		var player = players[i];
		var playerList = "";
		playerList += "<h4 class='card-title'>" + player.name + "</h4>";
		playerList += "<h6 class='card-subtitle mb-2 text-muted'>" + teams[player.team].color + "</h6>";
		playerList += "<div>Cards: " + player.cards + "</div>";
		playerList += "<div>Dev Cards: " + player.devCards + "</div>";
		playerList += "<div>Played Dev Cards: ";
		for (var j = 0; j < player.playedDevCards.length; j++) {
			if (j !== 0) playerList += ", ";
			playerList += player.playedDevCards[j].name;
		}
		playerList += "</div>";
		$("#player-" + i).html(playerList);
	}
}

function showMessage(message) {
	var consoleElement = $("#console");
	consoleElement.append("<div>" + message + "</div><br / >");
	consoleElement.stop().animate({scrollTop: consoleElement[0].scrollHeight}, 1000);
	console.log(message);
}