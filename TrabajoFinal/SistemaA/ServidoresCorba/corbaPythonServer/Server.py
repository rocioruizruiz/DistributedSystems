import sys
import cv2
import os
import CosNaming
import PortableServer
import FilterApp
import FilterApp__POA
from omniORB import CORBA
from os import listdir
from os.path import isfile, join

n = 0

class FilterImpl (FilterApp__POA.Filter):
    def applyFilter(self, filtro, imgpath):
        allFilters = [f for f in listdir(
            "/mnt/clientPythonFilter/") if isfile(join("/mnt/clientPythonFilter/", f))]
        for f in allFilters:
            os.path.splitext(f)
            filterName = os.path.splitext(f)[0]
            if(filtro == filterName):
                exec(open("/mnt/clientPythonFilter/" + f).read(), globals(), locals())
                return destPath
        else:
            return "Error. Filter does not exist."


sys.argv.extend(("-ORBInitRef", "NameService=corbaname::localhost:1050"))

# create and initialize the ORB
orb = CORBA.ORB_init(sys.argv, CORBA.ORB_ID)

# get reference to rootpoa & activate the POAManager
rootpoa = orb.resolve_initial_references("RootPOA")
rootpoa._get_the_POAManager().activate()

# create servant and register it with the POA
filterImpl = FilterImpl()
servantId = rootpoa.activate_object(filterImpl)

# Publish the hello object reference to the Naming Service
ref = rootpoa.id_to_reference(servantId)

# Get the Naming Service's root naming context
objRef = orb.resolve_initial_references("NameService")
ncRef = objRef._narrow(CosNaming.NamingContext)

if ncRef is None:
    print("Failed to narrow the root naming context")
    sys.exit(1)

# bind the Object Reference in Naming
name = "Filter"
path = [CosNaming.NameComponent(name, "")]
try:
    ncRef.bind(path, ref)

except CosNaming.NamingContext.AlreadyBound, ex:
    print("Filter object already bound, rebinding new object")
    ncRef.rebind(path, ref)

print("Python Server ready and waiting...")
while True:
    orb.run()
