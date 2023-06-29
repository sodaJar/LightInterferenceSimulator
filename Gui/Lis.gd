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
			"slabWidth":["f5:cm",{"min":"f1:cm"}]
		}
	},
	"BeamSplitter":{
		"icon":preload("res://ComponentAssets/BeamSplitter.png"),
		"description":"A beam splitter of negligible thickness\n-Slab width- is the length of the component",
		"properties":{
			"slabWidth":["f10:cm",{"min":"f1:cm"}]
		}
	},
	"SingleSlit":{
		"icon":preload("res://ComponentAssets/SingleSlit.png"),
		"description":"An obstacle with an open slit in the center where diffraction occurs. "+\
		"The edges of the obstacle do not diffract light. "+\
		"The obstacle has negligible thickness and absorbs all light. If -slit width- if greater than "+\
		"obstacle width, the component is ignored",
		"properties":{
			"obstacleWidth":["f1:m",{"min":"f10:cm"}],
			"slitWidth":["f5000:nm",{"min":"f1:nm"}]
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
			"screenWidth":["f10:cm",{"min":"f1:mm"}],
			"resolution":["i100:points",{"min":"i50:points"}]
		},
	},
	"Laser": {
		"icon": preload("ComponentAssets/Laser.png"),
		"description":"A laser emitting coherent light of wavelength specified globally. "+\
		"-Power- relates to the intensity observed and does not have a real unit"+\
		"-Beam width- is the width where the intensity is 1/e^2 (around 13.5%) of the maximum intensity",
		"properties": {
			"beamWidth":["f100:um",{"min":"f10:um","max":"f1:mm"}],
			"power":["f100:%",{"min":"f1:%"}]
		}
	}
}

#format data for physics server, convert all units to SI units (unify)
func getDataArray() -> Array:
	var r:Array = []
	r.append_array(["f"+str(wavelength),"i"+str(scatterDensity),
	"i"+str(testScatterCount),"i"+str(threadCount),"i"+str(instancePort)]) #the first few items are the global settings
	for key in components: #then, alternate between component name and properties
		var c = components[key]
		r.append(c.name)
		var props:String = ""
		for pName in c.properties:
			var p = c.properties[pName]
			var valArr:Array = value2array(p[0])
			var val = parseValue(valArr)
			#the format of properties: [property1Name] [value1];[property2Name] [value2];
			#properties are seperated by semi-colons. Name and value are seperated with a whitespace
			props += pName+" "+valArr[0]+( str(val) if (val is bool||val is int) else float2stringPrecise(unifyFrom(val,valArr[2])) )+";"
		r.append(props)
	return r

const unitMap = {
	"": 1,
	"%": 0.01,
	"radians": 1,
	"degrees": PI/180,
	"m": 1,
	"dm": 1e-1,
	"cm": 1e-2,
	"mm": 1e-3,
	"um": 1e-6,
	"nm": 1e-9,
}

func isUnitDefault(unit:String)->bool: return unit in ["m","dm","cm","mm","um","nm"]
func value2array(value:String) -> Array: #splits a string value in the standard format to an array
	var seperatorIndex:int = value.find(":") #colon seperates the numerical value and the unit
	assert(seperatorIndex>0)
	return [value.substr(0,1),value.substr(1,seperatorIndex-1),value.substr(seperatorIndex+1)]
func parseValue(valueArr:Array):
	match valueArr[0]:
		"f": return float(valueArr[1])
		"i": return int(valueArr[1])
		"b": return valueArr[1] == "True"
func getNode(group:String)->Node: return get_tree().get_nodes_in_group(group)[0]
func getNodes(group:String)->Array: return get_tree().get_nodes_in_group(group)
func deunifyTo(u:float,unit:String)->float: return u/unitMap[unit if unitMap.has(unit) else ""]
func unifyFrom(n:float,unit:String)->float: return n*unitMap[unit if unitMap.has(unit) else ""]
func changeUnit(n:float,oldUnit:String,newUnit:String)->float: return deunifyTo(unifyFrom(n,oldUnit),newUnit)
func float2stringPrecise(f:float)->String: return "%.64f" % f

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
		c.properties["positionX"] = ["f"+str(naturalOffset)+":cm",{}] #natural offset
		naturalOffset += 0.005
		c.properties["positionY"] = ["f0:cm",{}]
		c.properties["rotation"] = ["f0:degrees",{}]
		c.properties["quality"] = ["f100:%",{"min":"f10:%","floatStep":1}]
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
	if components.has("Screen"): getNode("inspector").inspect("Screen")

const SAVE_PATH:String = "user://data/"#"./data/"
const DEFAULT_FILE_NAME:String = "save.tres"

func saveDataAbs(data:Dictionary,path:String) -> int: #saves a dictionary to an absolute path
	var dir:Directory = Directory.new()
	var e = dir.open(".")
	assert(e == OK,"Failed to open executable directory: Code "+str(e)) #writes message to log file
	assert(dir.dir_exists(path.get_base_dir()),"Path input invalid")
	var file = Data.new()
	file.data = data
	return ResourceSaver.save(path,file) #returns an int error code
func loadDataAbs(path:String):
	var resource = ResourceLoader.load(path)
	if !is_instance_valid(resource): return null #if the file failed to load
	if !("data" in resource): return null #if the file loaded is not valid
	return resource.data
