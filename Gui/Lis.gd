extends Node

#value taken from Globals node on ready
var wavelength:float
var scatterDensity:int
var testScatterCount:int
var threadCount:int

#global dictionary of all available components and their information
const GALLERY:Dictionary = {
	"Lens":{
		"icon":preload("res://ComponentAssets/Lens.png"),
		"description":"An ideal convex lens following the lens equation\nIf -compensating- is true, "+\
		"parallel light rays reach the focal plane with no path difference",
		"properties":{
			"lensWidth":["f5:cm",{"min":"f1:mm"}],
			"focalLength":["f50:mm",{"min":"f1:mm"}],
			"compensating":["bTrue:bool",{}]
		}
	},
	"Mirror":{
		"icon":preload("res://ComponentAssets/Mirror.png"),
		"description":"A perfectly reflective mirror of negligible thickness\n-Slab width- is the length of the component",
		"properties":{
			"slabWidth":["f5:cm",{"min":"f1:mm"}]
		}
	},
	"BeamSplitter":{
		"icon":preload("res://ComponentAssets/BeamSplitter.png"),
		"description":"A beam splitter of negligible thickness\n-Slab width- is the length of the component",
		"properties":{
			"slabWidth":["f5:cm",{"min":"f1:mm"}]
		}
	},
	"SingleSlit":{
		"icon":preload("res://ComponentAssets/SingleSlit.png"),
		"description":"An obstacle with an open slit in the center where diffraction occurs. "+\
		"The edges of the obstacle do not diffract light. "+\
		"The obstacle has negligible thickness and absorbs all light. If -slit width- if greater than "+\
		"obstacle width, the component is ignored",
		"properties":{
			"obstacleWidth":["f1:m",{"min":"f1:cm"}],
			"slitWidth":["f5000:nm",{"min":"f0:nm"}]
		}
	},
	"Screen": {
		"icon": preload("ComponentAssets/Screen.png"),
		"description":"The one-sided screen that captures the intensity of incoming light. "+\
		"There can only be one screen in the setup\n-Resolution- is the number of points on the resulting graph. "+\
		"Increase this value if the result appears too rough or chaotic"+\
		"\n-Quality- of the screen does not matter",
		"properties": {
			"screenWidth":["f3:cm",{"min":"f1:um"}],
			"resolution":["i100:points",{"min":"i50:points"}]
		},
	},
	"Laser": {
		"icon": preload("ComponentAssets/Laser.png"),
		"description":"A laser emitting coherent light of wavelength specified globally.\n"+\
		"-Power- relates to the intensity observed and does not have a real unit\n"+\
		"-Beam width- is the width where the power is above around 13.5% of the maximum power. The actual "+\
		"width of the laser device that blocks passing rays is twice the -beam width-\n"+\
		"Decreasing the -scattering angle- causes the light beam to be more concentrated",
		"properties": {
			"beamWidth":["f100:um",{"min":"f100:nm","max":"f1:mm"}],
			"power":["f100:%",{"min":"f1:%"}],
			"scatteringAngle":["f30:degrees",{"min":"f1:degrees","max":"f180:degrees"}],
		}
	}
}

#format data for the physics engine, convert all units to SI units (unify)
func getDataArray() -> Array:
	var r:Array = []
	r.append_array(["f"+str(wavelength),"i"+str(scatterDensity),
	"i"+str(testScatterCount),"i"+str(threadCount)]) #the first few items are the global settings
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
func getDataArrayAsJavaCode() -> String:
	var r:String = "new String[]{"
	r += "\"f"+str(wavelength)+"\",\"i"+str(scatterDensity)+\
	"\",\"i"+str(testScatterCount)+"\",\"i"+str(threadCount)+"\"" #the first few items are the global settings
	for key in components: #then, alternate between component name and properties
		var c = components[key]
		r += ",\""+c.name+"\""
		var props:String = ""
		for pName in c.properties:
			var p = c.properties[pName]
			var valArr:Array = value2array(p[0])
			var val = parseValue(valArr)
			#the format of properties: [property1Name] [value1];[property2Name] [value2];
			#properties are seperated by semi-colons. Name and value are seperated with a whitespace
			props += pName+" "+valArr[0]+( str(val) if (val is bool||val is int) else float2stringPrecise(unifyFrom(val,valArr[2])) )+";"
		r += ",\""+props+"\""
	return r+"}"

#map of units used for conversion between them
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

#default units are units of distance
func isUnitDefault(unit:String)->bool: return unit in ["m","dm","cm","mm","um","nm"]
func value2array(value:String) -> Array: #splits a string value in the standard format to an array
	var seperatorIndex:int = value.find(":") #colon seperates the numerical value and the unit
	assert(seperatorIndex>0)
	return [value.substr(0,1),value.substr(1,seperatorIndex-1),value.substr(seperatorIndex+1)]
func parseValue(valueArr:Array): #gets the native value of the string value
	match valueArr[0]:
		"f": return float(valueArr[1])
		"i": return int(valueArr[1])
		"b": return valueArr[1] == "True"
func getNode(group:String)->Node: return get_tree().get_nodes_in_group(group)[0] #get node by group name
func getNodes(group:String)->Array: return get_tree().get_nodes_in_group(group) #get nodes by group name
#deunify means to convert the standard unit to another one
func deunifyTo(u:float,unit:String)->float: return u/unitMap[unit if unitMap.has(unit) else ""]
#unify means to convert the any unit to the standard one (e.g. meter, radian)
func unifyFrom(n:float,unit:String)->float: return n*unitMap[unit if unitMap.has(unit) else ""]
#unify then deunify
func changeUnit(n:float,oldUnit:String,newUnit:String)->float: return deunifyTo(unifyFrom(n,oldUnit),newUnit)
#custom float to string to overcome str() precision limitations
func float2stringPrecise(f:float)->String: return "%.64f" % f

#the global container of all added components in the setup
var components:Dictionary = {}
var naturalOffset:float = 0
#add a new component by duplicating and processing the component from GALLERY
func addNewComponent(cName:String, oldComponent:Dictionary={}) -> Dictionary:
	var cNameUnique
	if cName=="Screen": cNameUnique = cName #there can only be one screen
	#oldComponent is used for loading components from a file
	elif oldComponent.empty(): #creating a component from scratch, generate a unique name
		var suffix:int = 1
		var limit:int = 1000
		while Lis.components.has(cName + str(suffix)):
			if limit<=0: return {}
			suffix += 1
			limit -= 1
		cNameUnique = cName+str(suffix)
	else: cNameUnique = oldComponent.nameUnique
	components[cNameUnique] = Lis.GALLERY[cName].duplicate(true) #create a deep copy
	var c:Dictionary = components[cNameUnique]
	#remove unnecessary icon property
	c.erase("icon")
	#set properties common for all components (e.g. position and rotation)
	c["name"] = cName
	c["nameUnique"] = cNameUnique
	c.properties["positionX"] = ["f"+str(naturalOffset)+":cm",{}] #natural offset
	naturalOffset += 0.005
	c.properties["positionY"] = ["f0:cm",{}]
	c.properties["rotation"] = ["f0:degrees",{"floatStep":1e-5}]
	c.properties["quality"] = ["f100:%",{"min":"f10:%","floatStep":1}]
	#overwrite values according to oldComponent
	if !oldComponent.empty():
		for key in c.properties:
			if oldComponent.properties.has(key): c.properties[key][0] = oldComponent.properties[key][0]
			else: print("Emergent property: "+key)
#		for key in oldComponent.properties:
#			c.properties[key][0] = oldComponent.properties[key][0]
	#add to the list of components
	getNode("mainScene").componentsPm.addComponent(cNameUnique)
	#add to the sandbox view
	getNode("mainScene").addSymbol(cNameUnique)
	return components[cNameUnique]

func deleteComponent(cNameUnique:String):
	getNode("mainScene").componentsPm.deleteComponent(cNameUnique)
	getNode("mainScene").deleteSymbol(cNameUnique)
	components.erase(cNameUnique)
	if components.has("Screen"): getNode("inspector").inspect("Screen")

#saves a dictionary to a file with given path
func saveDataAbs(data:Dictionary,path:String) -> int:
	var dir:Directory = Directory.new()
	var e = dir.open(".")
	assert(e == OK,"Failed to open executable directory: Code "+str(e)) #write message to log file
	assert(dir.dir_exists(path.get_base_dir()),"Path input invalid")
	var file = Data.new()
	file.data = data
	return ResourceSaver.save(path,file) #return an int error code
#loads a dictionary from a file
func loadDataAbs(path:String):
	var resource = ResourceLoader.load(path)
	if !is_instance_valid(resource): return null #if the file failed to load
	if !("data" in resource): return null #if the file loaded is not valid
	return resource.data
