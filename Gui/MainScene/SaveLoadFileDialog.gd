extends FileDialog

func _physics_process(_delta):
	if rect_position.x < 0: rect_position.x = 0
	if rect_position.y < 0: rect_position.y = 0
	if rect_position.x+rect_size.x > get_viewport_rect().size.x: rect_position.x = get_viewport_rect().size.x-rect_size.x
	if rect_position.y+rect_size.y > get_viewport_rect().size.y: rect_position.y = get_viewport_rect().size.y-rect_size.y

func _on_SaveButton_button_up():
	mode=FileDialog.MODE_SAVE_FILE
	set_filters(PoolStringArray(["*.tres ; Resource File"]))
	window_title = "Save current setup to file"
#	set_current_dir(OS.get_executable_path().get_base_dir())
	show()
	invalidate()

func _on_LoadButton_button_up():
	mode=FileDialog.MODE_OPEN_FILE
	set_filters(PoolStringArray(["*.tres ; Resource Files"]))
	window_title = "Load saved setup from file"
#	set_current_dir(OS.get_executable_path().get_base_dir())
	show()
	invalidate()

func _on_SaveLoadFileDialog_file_selected(path):
	var camera:Camera2D = Lis.getNode("camera")
	if mode==MODE_SAVE_FILE:
		Lis.saveDataAbs({
			"camPos":camera.global_position,
			"camZoom":camera.zoom,
			"w":Lis.wavelength,
			"s":Lis.scatterDensity,
			"t":Lis.testScatterCount,
			"th":Lis.threadCount,
			"i":Lis.instancePort,
			"components":Lis.components
		},path)
	else:
		var data = Lis.loadDataAbs(path)
		if data == null: return
		camera.global_position = data.camPos
		camera.zoom = data.camZoom
		Lis.wavelength = data.w
		Lis.scatterDensity = data.s
		Lis.testScatterCount = data.t
		Lis.threadCount = data.th
		Lis.instancePort = data.i
		Lis.getNode("globals").updateSpinBoxes()
		for cNameUnique in Lis.components.keys(): Lis.deleteComponent(cNameUnique)
		for key in data.components:
			var c:Dictionary = data.components[key]
			Lis.addNewComponent(c.name,c)
		Lis.getNode("inspector").inspect("Screen")
