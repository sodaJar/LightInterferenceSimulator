extends PopupMenu

func _on_UselessButton_button_up():
	visible = !visible

var idCounter:int = 0

var idMap:Dictionary = {}

func addComponent(cNameUnique:String):
	add_item(cNameUnique.capitalize(),idCounter)
	set_item_icon(get_item_index(idCounter),Lis.GALLERY[Lis.components[cNameUnique].name].icon)
	idMap[cNameUnique] = idCounter
	idCounter += 1
func deleteComponent(cNameUnique:String):
	remove_item(get_item_index(idMap[cNameUnique]))
	idMap.erase(cNameUnique)

func _on_ComponentsPopupMenu_index_pressed(index):
	var cNameUnique = get_item_text(index).replace(" ","")
	Lis.getNode("inspector").inspect(cNameUnique)
	
