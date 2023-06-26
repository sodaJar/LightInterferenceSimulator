extends MenuButton

#onready var scene:Node2D = get_parent().get_parent()

onready var popup:PopupMenu = get_popup()

func _ready():
	popup.connect("index_pressed",self,"s_idxPressed")
	hint_tooltip = "Add a new component to the setup"
	#initialize all components
	for key in Lis.GALLERY:
		if key=="Screen": continue
		popup.add_item(key.capitalize())

func s_idxPressed(idx:int):
	Lis.addNewComponent(popup.get_item_text(idx).replace(" ",""))

