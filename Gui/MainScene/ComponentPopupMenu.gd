extends PopupMenu

#the id of the next component to be added
var idCounter:int = 0
#map of the unique component name and its id
var idMap:Dictionary = {}

func addComponent(cNameUnique:String):
	#add new item to menu
	add_item(cNameUnique.capitalize(),idCounter)
	#set icon for the added item
	set_item_icon(get_item_index(idCounter),Lis.GALLERY[Lis.components[cNameUnique].name].icon)
	idMap[cNameUnique] = idCounter
	idCounter += 1
func deleteComponent(cNameUnique:String):
	remove_item(get_item_index(idMap[cNameUnique]))
	idMap.erase(cNameUnique)

func _on_ComponentsButton_button_up():
	visible = !visible
func _on_ComponentsPopupMenu_index_pressed(index):
	#the actual unique name (camelCase) should be the display name with all spaces removed
	var cNameUnique = get_item_text(index).replace(" ","")
	Lis.getNode("inspector").inspect(cNameUnique)
