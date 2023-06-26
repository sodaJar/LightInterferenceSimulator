extends Sprite

onready var symbolIcon = $SymbolIcon

var component:Dictionary

var selected:bool = false

func _ready():
	symbolIcon.texture = Lis.GALLERY[component.name].icon

func _physics_process(_delta):
	self_modulate.a = 1 if selected else 0.3

func refresh():
	position = Vector2(Lis.deunifyTo(component.properties.positionX[0].substr(1).to_float(),"mm"),-Lis.deunifyTo(component.properties.positionY[0].substr(1).to_float(),"mm"))
	rotation = -component.properties.rotation[0].substr(1).to_float()

func deselect():
	selected = false
func select():
	get_parent().move_child(self,get_parent().get_child_count()-1)
	selected = true
