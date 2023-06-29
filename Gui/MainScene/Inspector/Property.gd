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
var valueArr:Array #reflects the actual string value in Lis
#var unit:String
var propertyName:String = "samplePropertyName"
var args:Dictionary = {
	"min": "",
	"max": "",
	"floatStep": 0.01
}

var ignoreInputValueChange:bool = true

const unitButtonIdMap:Dictionary = {
	"m":0,
	"dm":1,
	"cm":2,
	"mm":3,
	"um":4,
	"nm":5
}
#var lastUnitButtonUnit:String = ""

func _ready():
	label.set_bbcode(propertyName.capitalize())

func receiveValue(value:String):
	ignoreInputValueChange = true
	valueArr = Lis.value2array(value)
	var unitIsDefault:bool = Lis.isUnitDefault(valueArr[2])
	if unitIsDefault:
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
		unitLabel.set_bbcode(valueArr[2].capitalize())
		unitLabel.show()

	if valueArr[0] in ["i","f"]:
		valueInputBool.hide()
		valueInputNum.show()
		if unitIsDefault: unitButton.select(unitButton.get_item_index(unitButtonIdMap[valueArr[2]]))
		updateInputNumMin(valueArr[2])
		updateInputNumMax(valueArr[2])

	match valueArr[0]:
		"f":
			valueInputNum.step = args.floatStep
			valueInputNum.value = float(valueArr[1])
		"i":
			valueInputNum.step = 1
			valueInputNum.value = int(valueArr[1])
		"b":
			valueInputBool.pressed = valueArr[1] == "True"
			valueInputNum.hide()
			valueInputBool = $ValueBool
			valueInputBool.show()
			unitButton.hide()
			unitLabel.hide()
#	lastUnitButtonUnit = getUnit()
	ignoreInputValueChange = false

#updates min and max based on valueArr
func updateInputNumMin(newUnit:String):
	if args.min == "": return
	var minValArr:Array = Lis.value2array(args.min)
	var unified = Lis.unifyFrom(Lis.parseValue(minValArr),minValArr[2])
	valueInputNum.min_value = max(Lis.deunifyTo(unified,newUnit),args.floatStep)
	valueInputNum.allow_lesser = false
func updateInputNumMax(newUnit:String):
	if args.max == "": return
	var maxValArr = Lis.value2array(args.max)
	var unified = Lis.unifyFrom(Lis.parseValue(maxValArr),maxValArr[2])
	valueInputNum.max_value = Lis.deunifyTo(unified,newUnit)
	valueInputNum.allow_greater = false

func getUnit() -> String:
	if !Lis.isUnitDefault(valueArr[2]): return valueArr[2]
	if unitButton.get_item_id(unitButton.selected)==unitButtonIdMap["um"]: return "um"
	return unitButton.get_item_text(unitButton.selected)

func _on_UnitOptionButton_item_selected(index): #when the user changes the distance unit
	#update the min and max values to match the new unit
	updateInputNumMin(getUnit())
	updateInputNumMax(getUnit())
	_on_ValueNum_value_changed(null) #changing only the unit is equivalent a change in value, so call update

func _on_ValueNum_value_changed(value): #the value of the property is changed
	if ignoreInputValueChange: return
	match type: #update the global list of components and respective symbols in the sandbox view
		"f":
			Lis.components[cNameUnique].properties[propertyName][0] = "f"+Lis.float2stringPrecise(valueInputNum.value)+":"+getUnit()
			Lis.getNode("mainScene").updateSymbol(cNameUnique)
		"i":
			Lis.components[cNameUnique].properties[propertyName][0] = "i"+str(int(round(valueInputNum.value)))+":"+getUnit()
			Lis.getNode("mainScene").updateSymbol(cNameUnique)
		"b":
			Lis.components[cNameUnique].properties[propertyName][0] = "b"+str(valueInputBool.pressed)+":none"
			Lis.getNode("mainScene").updateSymbol(cNameUnique)
	valueArr = Lis.value2array(Lis.components[cNameUnique].properties[propertyName][0])
