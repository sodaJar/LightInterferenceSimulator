extends Sprite

"""
A symbol refers to the icon displayed in the sandbox view (main scene)
it reflects the position and rotation of the component it represents
"""

onready var symbolIcon = $SymbolIcon

var component:Dictionary #the component it represents and is linked to

var selected:bool = false #if the component is being inspected in the inspector

#initialize the icon
func _ready(): symbolIcon.texture = Lis.GALLERY[component.name].icon

#update the transparency of the sprite based on whether the component is selected or not
func _physics_process(_delta):
	self_modulate.a = 1 if selected else 0.3

func refresh(): #updates position and rotation according to the component
	var posXArr = Lis.value2array(component.properties.positionX[0])
	var posYArr = Lis.value2array(component.properties.positionY[0])
	position = Vector2(Lis.changeUnit(Lis.parseValue(posXArr),posXArr[2],"mm"),\
	-Lis.changeUnit(Lis.parseValue(posYArr),posYArr[2],"mm"))
	rotation = deg2rad(-Lis.value2array(component.properties.rotation[0])[1].to_float())

func deselect():
	selected = false
func select():
	#display this symbol above all other symbols when selected
	get_parent().move_child(self,get_parent().get_child_count()-1)
	selected = true
