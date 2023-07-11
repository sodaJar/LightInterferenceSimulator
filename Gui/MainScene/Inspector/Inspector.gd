extends Node2D

onready var titleLabel:RichTextLabel = $TitleLabel
onready var control:Control = $ScrollContainer/Control
onready var deleteButton:Button = $DeleteButton
onready var descLabel:RichTextLabel = $DescriptionLabel

var component:Dictionary

func inspect(cNameUnique:String):
	var c:Dictionary = Lis.components[cNameUnique]
	deleteButton.disabled = cNameUnique == "Screen"
	Lis.getNode("mainScene").selectSymbol(cNameUnique)
	titleLabel.set_bbcode("[center]Inspector ("+cNameUnique.capitalize()+")")
	descLabel.set_bbcode(c.description)
	#delete previous properties
	for prop in control.get_children(): prop.queue_free()
	var posY = 0
	#create all properties
	for key in c.properties:
		var p:Node2D
		var val = c.properties[key]
		p = preload("Property.tscn").instance()
		p.cNameUnique = cNameUnique
		p.propertyName = key
		#load in args
		for k in val[1]:
			assert(p.args.has(k))
			p.args[k] = val[1][k]
		control.add_child(p)
		p.receiveValue(val[0])
		p.position.y = posY
		posY += 50
	control.rect_min_size.y = posY
	component = c


func _on_DeleteButton_button_up():
	if deleteButton.get_global_rect().has_point(get_global_mouse_position()):
		Lis.deleteComponent(component.nameUnique)
