extends Node2D

#property name
onready var label:RichTextLabel = $PropertyNameLabel
#variant, could be lineEdit or checkbox
onready var valueInputNum:SpinBox = $ValueNum
onready var valueInputBool:CheckBox = $ValueBool
#display when unit == default only
onready var unitButton:OptionButton = $UnitOptionButton
#display when unit != default
onready var unitLabel:RichTextLabel = $UnitLabel

export(String,"float","int","bool") var type = "float"
var cNameUnique:String
var unit:String
var propertyName:String = "samplePropertyName"
var args:Dictionary = {
	"min": -INF,
	"max": INF,
	"floatStep": 0.01,
	"preferredDefaultUnit": "cm"
}

var ready:bool = false

const unitButtonIdxMap:Dictionary = {
	"m":0,
	"dm":1,
	"cm":2,
	"mm":3,
	"um":4,
	"nm":5
}
var lastUnitButtonUnit:String = ""

func _ready():
	label.set_bbcode(propertyName.capitalize())
	#configure unit
	if unit == "default":
		unitLabel.hide()
		unitButton.add_item("m",0)
		unitButton.add_item("dm",1)
		unitButton.add_item("cm",2)
		unitButton.add_item("mm",3)
		unitButton.add_item("μm",4)
		unitButton.add_item("nm",5)
		unitButton.show()
	else:
		unitButton.hide()
		unitLabel.set_bbcode(unit)
		unitLabel.show()
	
	if type in ["i","f"]:
		valueInputBool.hide()
		valueInputNum.show()
		if unit == "default":
			unitButton.select(unitButtonIdxMap[args.preferredDefaultUnit])
			var cache:Dictionary = Lis.getNode("inspector").propertyCache
			if cache.has(cNameUnique) && cache[cNameUnique].has(propertyName):
				unitButton.select(unitButtonIdxMap[cache[cNameUnique][propertyName]])
		
		if !is_inf(args.min):
			valueInputNum.min_value = Lis.deunifyTo(args.min,getUnit())
			valueInputNum.allow_lesser = false
		if !is_inf(args.max):
			valueInputNum.max_value = Lis.deunifyTo(args.max,getUnit())
			valueInputNum.allow_greater = false
	
	match type:
		"f":
			valueInputNum.step = args.floatStep
		"i":
			valueInputNum.step = 1
		"b":
			valueInputNum.hide()
			valueInputBool = $ValueBool
			valueInputBool.show()
			unitButton.hide()
			unitLabel.hide()
	ready = true

func receiveValue(value:String):
	assert(value.substr(0,1) == type,"Type mismatch for "+cNameUnique+": "+value+" is not of type "+type)
	match type:
		"f":
			valueInputNum.value = Lis.deunifyTo(value.substr(1).to_float(),getUnit())
		"i":
			valueInputNum.value = Lis.deunifyTo(value.substr(1).to_int(),getUnit())
		"b": valueInputBool.pressed = value.substr(1)=="true"

func getUnit() -> String:
	if unit == "degrees": return "deg"
	if unit == "%": return unit
	if unit != "default": return ""
	if unitButton.get_item_id(unitButton.selected)==unitButtonIdxMap["um"]: return "um"
	return unitButton.get_item_text(unitButton.selected)


func _on_UnitOptionButton_item_selected(index):
	var newUnit:String = getUnit()
	valueInputNum.min_value = Lis.deunifyTo(Lis.unifyFrom(valueInputNum.min_value,lastUnitButtonUnit),newUnit)
	valueInputNum.max_value = Lis.deunifyTo(Lis.unifyFrom(valueInputNum.max_value,lastUnitButtonUnit),newUnit)
	_on_ValueNum_value_changed(null)
	var cache:Dictionary = Lis.getNode("inspector").propertyCache
	cache[cNameUnique][propertyName] = newUnit
	lastUnitButtonUnit = newUnit


func _on_ValueNum_value_changed(value):
	if !ready: return
	match type:
		"f":
			Lis.components[cNameUnique].properties[propertyName][0] = "f"+Lis.float2stringPrecise(Lis.unifyFrom(valueInputNum.value,getUnit()))
			Lis.getNode("mainScene").updateSymbol(cNameUnique)
		"i":
			Lis.components[cNameUnique].properties[propertyName][0] = "i"+str(Lis.unifyFrom(valueInputNum.value,getUnit()))
			Lis.getNode("mainScene").updateSymbol(cNameUnique)
		"b":
			Lis.components[cNameUnique].properties[propertyName][0] = "b"+str(valueInputBool.pressed)
			Lis.getNode("mainScene").updateSymbol(cNameUnique)
