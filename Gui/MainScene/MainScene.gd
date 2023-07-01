extends Node2D

onready var camera:Camera2D = $Camera2D
onready var componentsPm:PopupMenu = $Ui/ComponentsPopupMenu
onready var inspectorMb:MenuButton = $Ui/InspectorMenuButton
onready var addComMb:MenuButton = $Ui/addComponentMenuButton
onready var runButton:Button = $Ui/RunButton

var dragging:bool = false

func _unhandled_input(event):
	if event is InputEventMouseButton:
		if event.button_index == BUTTON_LEFT: dragging = event.pressed
		elif event.button_index == BUTTON_WHEEL_UP:
			camera.zoom *= Vector2(0.95,0.95)
			if (camera.zoom.x < 0.1): camera.zoom = Vector2(0.1,0.1)
		elif event.button_index == BUTTON_WHEEL_DOWN:
			camera.zoom *= Vector2(1.05,1.05)
			if (camera.zoom.x > 10): camera.zoom = Vector2(10,10)
	elif event is InputEventMouseMotion: if dragging: camera.position -= event.relative*camera.zoom.x

func _ready():
	Lis.addNewComponent("Screen")
	Lis.getNode("inspector").inspect(Lis.components.keys()[0])

func _on_RunButton_pressed():
	runButton.disabled = true
	OS.execute(OS.get_executable_path().get_base_dir()+"/PhysicsEngine/LisPhE.exe",Lis.getDataArray(),false)
	yield(get_tree().create_timer(1),"timeout")
	runButton.disabled = false

var symbolMap:Dictionary = {}

func addSymbol(cNameUnique:String):
	var s:Sprite = preload("Symbol/Symbol.tscn").instance()
	var c:Dictionary = Lis.components[cNameUnique]
	s.component = c
	add_child(s)
	s.refresh()
	symbolMap[cNameUnique] = s
func deleteSymbol(cNameUnique):
	symbolMap[cNameUnique].queue_free()
	symbolMap.erase(cNameUnique)
func updateSymbol(cNameUnique:String):
	symbolMap[cNameUnique].refresh()
func selectSymbol(cNameUnique:String):
	for key in symbolMap:
		if key == cNameUnique: symbolMap[key].select()
		else: symbolMap[key].deselect()

