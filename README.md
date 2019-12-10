# flow_traffic_columbia
flow, sumo, west harlem testbed

### Files
- WestHarlem.ipynb (FLOW simulation file)
- osm.net.xml (network file)
- result4.rou.xml (routes file)
- vtypes.add.xml (vehicle types file)

## How to generate these files: 

1. WestHarlem.ipynb 
derived from FLOW tutorial 8 LuST scenario example. Requires other 3 files in order to work. Must give a dirname to be added to the path. Save all 3 files to this path and ALSO into /flow/flow/core/kernel/scenario/debug/cfg/. FlOW reads the routes and stuff from the path you give, but SUMO checks the second path to run. 

2. osm.net.xml 
Assumes sumo is installed. Navigate to sumo folder, and into the tools folder. 
run `python osmWebWizard.py` and select the area you are looking to model in the browser gui. Specify duration and types of vehicles if you want too and click generate scenario. SUMO-gui will open and you can run a simulation by clicking play. 
It will run for the specified duration and close. 

3. result4.rou.xml

After it has finished running. SUMO will create a folder in the `/tools` folder that contains lots of .xml and config files. In particular there should be a trips.xml file named `osm.passenger.trips.xml` if you simulated with cars. Trips are source, destination pairs. We use the DUArouter tool in sumo to generate proper route file needed by FLOW. 

In the sumo folder, run the command: 
`duarouter --route-files osm.passenger.trips.xml --net-file osm.net.xml --output-file result.rou.xml --departpos random --departspeed random`

The `departpos` and `departspeed` flags are needed by flow. THe `result.rou.xml` file will contain the routes file. 

Each vehicle should have an entry like this: 
```
<vehicle id="veh29" type="passenger" depart="85.92" departLane="best" departPos="random" departSpeed="random">
        <route edges="-5670226#4 195743150#1 195743150#2 195743150#3 195743150#4 195743150#5 -420590068 -420881463 -420590069#0 -420881462 -195743203#2 -195743203#1 -195743188#1"/>
    </vehicle>
```

4. vtypes.add.xml

Altered from the LuST scenario repo. Specifies the types of vehicles and their distributions. The type must mach the type in the `result.rou.xml` file. 