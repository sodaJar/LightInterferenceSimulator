

--------------------------------------------------------------
# Light Interference Simulator User Manual
IBDP Computer Science Internal Assessment

--------------------------------------------------------------

To start the GUI program, run Lis.exe

Guide for creating a setup and running a simulation:

1. Add components via the "+" button
2. Select a component by clicking on the "Components" menu and then on the unique name of the component you wish to select
3. Edit parameters in the inspector tab, select the unit using the dropdown menu if applicable
4. Delete a component by selecting it and pressing the "Delete component" button in the inspector tab
5. Click the "Launch" button to run the setup
6. Right click the calculated graphs to save as image

To save or load a setup (components plus global settings), click on the "Save" or "Load" button beneath the "Launch" button

--------------------------------------------------------------

List of global settings:

Light Wavelength - As the simulator supports only light of a single wavelength, it is adjusted via this global setting

Calculation density - The number of rays into which a scattering ray scatters onto a component exposed to the ray, the angle between the furthest scattered rays is almost PI

Collision test density - The number of segments used to check for obstacles between a component and a ray. A value of 30-50 is generally sufficient unless components vary greatly in size

Calculation thread count - Used for multithreading. Equals the number of threads created to share the workload of calculating points on the screen. The average workload for each thread is roughly the screen resolution divided by the thread count

--------------------------------------------------------------

If the graph appears incorrect, you can try to:

1. double check if each component has the correct parameters
2. increase the resolution of the screen
3. decrease or increase the screen width
4. increase the scattering density
5. increase the collistion test density

--------------------------------------------------------------

22.04.2023 - 26.06.2023
