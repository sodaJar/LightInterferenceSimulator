extends Resource
class_name Data

"""
The class used for storing data to a file. An instance of this is created to
hold the dictionary to be saved. This resource is then saved to a file with
the built-in ResourceSaver
"""

export(Dictionary) var data
