import os, sys 


if 'SUMO_HOME' in os.environ: 
    tools = os.path.join(os.environ['SUMO_HOME'], 'tools')
    sys.path.append(tools)
else: 
    sys.exit('please declare ENV variable "SUMO_HOME"')

sumoBinary = "/usr/local/opt/sumo/share/sumo/bin/sumo-gui"
sumoCmd = [sumoBinary, "-c", "120_lasalle.sumocfg"]

import traci
traci.start(sumoCmd)
step = 0

while step < 100:
    traci.simulationStep()
    #vehicle 0 will slow down at simulation step 20
    if step == 20: traci.vehicle.setSpeed("0", 4)

    #vehicle 0 will speed up at simulation step 40
    if step == 40: traci.vehicle.setSpeed("0", 20)

    #vehicle 0 will slow down  at simulation step 55
    if step == 55: traci.vehicle.setSpeed("0",3)

    #vehicle 0 will speed up at simulation step 75 to max speed
    if step == 75: traci.vehicle.setSpeed("0", 30)
    step += 1

traci.close()
