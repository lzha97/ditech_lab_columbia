import os, sys 
import pandas as pd
import math

if 'SUMO_HOME' in os.environ: 
    tools = os.path.join(os.environ['SUMO_HOME'], 'tools')
    sys.path.append(tools)
else: 
    sys.exit('please declare ENV variable "SUMO_HOME"')

sumoBinary = "/usr/local/opt/sumo/share/sumo/bin/sumo-gui"
sumoCmd = ['sumo-gui', "-c", "hollywood_highway.sumocfg"]

import traci
import sumolib

net = sumolib.net.readNet('hollywood_highway_simplify.net.xml')
folder = './data_fix_latlon/'
lane_data = []
for i in range(1,9):
    df = pd.read_csv(folder + 'lane' + str(i)+'.csv')
    lane_data.append(df)
traj = pd.concat(lane_data)

#sort by ascending frame_id
#traj = traj.sort_values(by='frame_id')
traj = traj.sort_values(by='t_diff')
print(traj.head())

traci.start(sumoCmd)

#keep track of timesteps that happened and vehicles that have already been seen
timesteps = dict()
seen_vehicles = []
frame_vehicles = []

for index, row in traj.iterrows(): 
    frame = row['frame_id']
    vehid = str(row['vehicle_id'])
    lat = row['latitude']
    long = row['longitude']
    lane = row['lane_id']
    time = row['date_time']
    timestep = row['t_diff']
    #angle = math.asin(row['local_y'])
    xx, yy = net.convertLonLat2XY(long, lat) 
    
    #if vehid not in seen_vehicles: seen_vehicles.add(vehid) # not necessary?
    
    if  timestep not in timesteps: 
        timesteps[timestep] = 1
        # remove the vehicle if it is not in the frame
        for vid in seen_vehicles:
            if vid not in frame_vehicles:
                traci.vehicle.remove(vid, reason=3)
                seen_vehicles.remove(vid)
        frame_vehicles = []

        # add the first vehicle on the frame 
        frame_vehicles.append(vehid)
        traci.simulationStep()
    else:
        frame_vehicles.append(vehid)


    print('timestep:', timestep, '\t frame:', frame, '\ttime:', time, '\tvehicle id:', vehid, '\tlane id:', lane)
    
    if vehid in seen_vehicles:
        traci.vehicle.moveToXY(vehid, edgeID ='', lane = 0, x = xx, y = yy,  keepRoute=2)
    else:
        traci.vehicle.add(vehID = vehid, routeID='', departLane = 0)
        traci.vehicle.moveToXY(vehid, edgeID ='', lane = 0, x = xx, y = yy, keepRoute=2)
        seen_vehicles.append(vehid)


    # try: 
    #     traci.vehicle.moveToXY(vehid, edgeID ='', lane = 0, x = xx, y = yy, keepRoute=2)
    # except: 
    #     traci.vehicle.add(vehID = vehid, routeID='', departLane = 0)
    #     traci.vehicle.moveToXY(vehid, edgeID ='', lane = 0, x = xx, y = yy, keepRoute=2)


traci.close()
