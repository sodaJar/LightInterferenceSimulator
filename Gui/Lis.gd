extends Node

#value taken from Globals node on ready
var wavelength:float
var scatterDensity:int
var testScatterCount:int
var threadCount:int
var instancePort:int

const GALLERY:Dictionary = {
	"Mirror":{
		"icon":preload("res://ComponentAssets/Mirror.png"),
		"description":"A perfectly reflective mirror of negligible thickness\n-Slab width- is the length of the component",
		"properties":{
			"slabWidth":["f0.05","default",{"min":0.01}]
		}
	},
	"BeamSplitter":{
		"icon":preload("res://ComponentAssets/BeamSplitter.png"),
		"description":"A beam splitter of negligible thickness\n-Slab width- is the length of the component",
		"properties":{
			"slabWidth":["f0.1","default",{"min":0.01}]
		}
	},
	"SingleSlit":{
		"icon":preload("res://ComponentAssets/SingleSlit.png"),
		"description":"An obstacle with an open slit in the center where diffraction occurs. "+\
		"The edges of the obstacle do not diffract light. "+\
		"The obstacle has negligible thickness and absorbs all light. If -slit width- if greater than "+\
		"obstacle width, the component is ignored",
		"properties":{
			"obstacleWidth":["f1","default",{"min":0.1}],
			"slitWidth":["f5000e-9","default",{"min":1e-9,"preferredDefaultUnit":"nm"}]
		}
	},
	"Screen": {
		"icon": preload("ComponentAssets/Screen.png"),
		"description":"The one-sided photosensitive screen of negligible thickness that detects the intensity of incoming light rays. "+\
		"The output graph will be generated from what this screen observes. "+\
		"There can only be one screen\n-Resolution- is the number of points on the resulting graph. "+\
		"Increase this value if the result appears too rough or chaotic"+\
		"\n-Quality- of the screen does not matter",
		"properties": {
			"screenWidth":["f0.3","default"], #units always normalized in Lis.gd
			"resolution":["i100","points",{"min":50}]
		},
	},
	"Laser": {
		"icon": preload("ComponentAssets/Laser.png"),
		"description":"A laser emitting coherent light of wavelength specified globally. "+\
		"-Power- relates to the intensity observed and does not have a real unit"+\
		"-Beam width- is the width where the intensity is 1/e^2 (around 13.5%) of the maximum intensity",
		"properties": {
			"beamWidth":["f0.002","default",{"min":10e-6,"max":0.001}],
			"power":["f1","%",{"min":0.01}]
		}
	}
}

const unitMap = {
	"": 1,
	"%": 0.01,
	"rad": 1,
	"deg": PI/180,
	"m": 1,
	"dm": 1e-1,
	"cm": 1e-2,
	"mm": 1e-3,
	"um": 1e-6,
	"nm": 1e-9,
}

func getDataArray() -> Array:
	var r:Array = []
	r.append_array(["f"+str(wavelength),"i"+str(scatterDensity),
	"i"+str(testScatterCount),"i"+str(threadCount),"i"+str(instancePort)])
	for key in components:
		var c = components[key]
		r.append(c.name)
		var props:String = ""
		for pName in c.properties:
			var p = c.properties[pName]
			props += pName+" "+p[0]+";"
		r.append(props)
	return r

func getNode(group:String)->Node: return get_tree().get_nodes_in_group(group)[0]
func getNodes(group:String)->Array: return get_tree().get_nodes_in_group(group)
func deunifyTo(u:float,unit:String)->float: return u/unitMap[unit]
func unifyFrom(n:float,unit:String)->float: return n*unitMap[unit]
func float2stringPrecise(f:float)->String: return "%.32f" % f

var components:Dictionary = {}
var naturalOffset:float = 0
func addNewComponent(cName:String, oldComponent:Dictionary={}) -> Dictionary:
	var cNameUnique
	if cName=="Screen": cNameUnique = cName #there can only be one screen
	elif oldComponent.empty():
		var suffix:int = 1
		var limit:int = 1000
		while Lis.components.has(cName + str(suffix)):
			if limit<=0: return {}
			suffix += 1
			limit -= 1
		cNameUnique = cName+str(suffix)
	else: cNameUnique = oldComponent.nameUnique
	components[cNameUnique] = Lis.GALLERY[cName].duplicate(true)
	var c:Dictionary = components[cNameUnique]
	c.erase("icon")
	c["name"] = cName
	c["nameUnique"] = cNameUnique
	if oldComponent.empty():
		c.properties["positionX"] = ["f"+str(naturalOffset),"default"] #natural offset
		naturalOffset += 0.005
		c.properties["positionY"] = ["f0","default"]
		c.properties["rotation"] = ["f0","degrees"]
		c.properties["quality"] = ["f1","%",{"floatStep":1}]
	else:
		for key in oldComponent.properties:
			c.properties[key] = oldComponent.properties[key].duplicate()
	#add to the list of components
	getNode("mainScene").componentsPm.addComponent(cNameUnique)
	#adds to the sandbox view
	getNode("mainScene").addSymbol(cNameUnique)
	return components[cNameUnique]

func deleteComponent(cNameUnique:String):
	getNode("mainScene").componentsPm.deleteComponent(cNameUnique)
	getNode("mainScene").deleteSymbol(cNameUnique)
	components.erase(cNameUnique)
	assert(components.has("Screen"))
	getNode("inspector").inspect("Screen")
	getNode("inspector").propertyCache.erase(cNameUnique)

const SAVE_PATH:String = "user://data/"#"./data/"
const DEFAULT_FILE_NAME:String = "save.tres"

func saveDataAbs(data:Dictionary,path:String) -> int:
	var dir = Directory.new()#create new Directory and assign to dir
	#if the SAVE_PATH doesn't exist, create path
	var e = dir.open(path.get_base_dir())
	assert(e == OK,"Failed to open executable directory: Code "+str(e))
	if !dir.dir_exists(path): assert("Path input invalid")
	var file = Data.new()
	file.data = data
	return ResourceSaver.save(path,file)
func loadDataAbs(path:String):
	var resource = ResourceLoader.load(path)
	if !is_instance_valid(resource): return null
	return resource.data
