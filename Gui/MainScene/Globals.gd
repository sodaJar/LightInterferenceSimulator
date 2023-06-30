extends Node2D

func _ready():
	_on_WavelengthSpinBox_value_changed($WavelengthSpinBox.value)
	_on_ScDensitySpinBox_value_changed($ScDensitySpinBox.value)
	_on_ColTestDensitySpinBox_value_changed($ColTestDensitySpinBox.value)
	_on_CalcThreadCountSpinBox_value_changed($CalcThreadCountSpinBox.value)

func updateSpinBoxes():
	$WavelengthSpinBox.set_value(Lis.wavelength)
	$ScDensitySpinBox.set_value(Lis.scatterDensity)
	$ColTestDensitySpinBox.set_value(Lis.testScatterCount)
	$CalcThreadCountSpinBox.set_value(Lis.threadCount)

func _on_WavelengthSpinBox_value_changed(value): Lis.wavelength = value
func _on_ScDensitySpinBox_value_changed(value): Lis.scatterDensity = value
func _on_ColTestDensitySpinBox_value_changed(value): Lis.testScatterCount = value
func _on_CalcThreadCountSpinBox_value_changed(value): Lis.threadCount = value
