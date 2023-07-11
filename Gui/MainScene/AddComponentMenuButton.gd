extends MenuButton

onready var popup:PopupMenu = get_popup()

func _ready():
	popup.connect("index_pressed",self,"s_idxPressed") #set up signal handler
	#initialize all components
	for key in Lis.GALLERY:
		if key=="Screen": continue
		popup.add_item(key.capitalize())

func s_idxPressed(idx:int): #called when one component is selected from the list
	#turn the display name into a component name by removing all spaces
	Lis.addNewComponent(popup.get_item_text(idx).replace(" ",""))

