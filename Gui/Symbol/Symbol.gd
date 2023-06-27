extends Sprite

onready var symbolIcon = $SymbolIcon

var component:Dictionary

var selected:bool = false

func _ready():
	symbolIcon.texture = Lis.GALLERY[component.name].icon

func _physics_process(_delta):
	self_modulate.a = 1 if selected else 0.3

func refresh():
	var posXArr = Lis.value2array(component.properties.positionX[0])
	var posYArr = Lis.value2array(component.properties.positionY[0])
	position = Vector2(Lis.changeUnit(Lis.parseValue(posXArr),posXArr[2],"mm"),\
	-Lis.changeUnit(Lis.parseValue(posYArr),posYArr[2],"mm"))
	rotation = deg2rad(-Lis.value2array(component.properties.rotation[0])[1].to_float())

func deselect():
	selected = false
func select():
	get_parent().move_child(self,get_parent().get_child_count()-1)
	selected = true
