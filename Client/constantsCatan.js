const ROBBER = 1;
const INFORMATION = 2;
const TRADE = 3;
const TURN = 4;
const GAME = 5;
const RESPONSE = 6;
const PLACE = 7;

const ROBBER_DISCARD = 10;
const ROBBER_PLACE = 11;

const INFORMATION_CONSOLE = 20;
const INFORMATION_TEAM = 21;
const INFORMATION_USERNAME = 22;

const TRADE_BANK = 30;
const TRADE_PLAYERS = 31;

const TURN_SETTLEMENT = 40;
const TURN_CITY = 41;
const TURN_ROAD = 42;
const TURN_DEV_CARD = 43;
const TURN_KNIGHT = 44;
const TURN_MONOPOLY = 45;
const TURN_YOP = 46;
const TURN_RB = 47;

const GAME_START = 50;
const GAME_JOIN = 51;

const PLACE_SETTLEMENT = 70;
const PLACE_ROAD = 71;

const GAMESTATE_BOARD = 80;
const GAMESTATE_PLAYERS = 81;
const GAMESTATE_HAND = 82;
const GAMESTATE_DEV_CARDS = 83;
const GAMESTATE_LOBBY = 84;
const GAMESTATE_GAME_LIST = 85;

const READY = 90;

const SHEEP_COLOR = "#a8d242";
const WHEAT_COLOR = "#ffd300";
const BRICK_COLOR = "#d77b42";
const STONE_COLOR = "#797979";
const WOOD_COLOR = "#33a642";
const DESERT_COLOR = "#fffdb9";
const WATER_COLOR = "#3daad6";
const NORMAL_NUMBER_COLOR = "#000000";
const RED_NUMBER_COLOR = "#ff0000";
const NUMBER_BG_COLOR = "#fff";
const ALL_PORT_COLOR = "#bbb";
const ROBBER_COLOR = "#aaa";

const HEX_HEIGHT = 87;
const HEX_WIDTH = 100;
const HEX_SIDE = 50;
	
var teams = [
	{
		color: "White",
		hexColor: "#FDF6EE"
	},
	{
		color: "Red",
		hexColor: "#CC0000"
	},
	{
		color: "Blue",
		hexColor: "#2323B2"
	},
	{
		color: "Orange",
		hexColor: "#E59400"
	}
];
